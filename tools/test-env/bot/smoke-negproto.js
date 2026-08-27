import net from 'node:net';

/**
 * Negative-protocol check against the embedded limbo port.
 *
 * The limbo supports protocols in [MIN_PROTOCOL=4 .. packetevents-latest].
 * Protocol 4 (1.7.2) is INSIDE the range, so it gets the normal MODERN
 * forwarding login-plugin-request flow. To hit the "Unsupported client
 * version" disconnect we must come from outside the range:
 *   - default NEG_PROTOCOL=2 (below the 1.7.2 floor)
 *   - NEG_PROTOCOL=99999 also works (above the ceiling)
 *
 * Sends handshake(protocol, intent=2 login) + login start, then expects a
 * login-state Disconnect (packet id 0x00) carrying the unsupported-version
 * message, followed by connection close.
 */

const CANDIDATES = [
  { host: '127.0.0.1', port: Number(process.env.NEG_PORT || 30000), label: 'limbo' },
  { host: '127.0.0.1', port: 25565, label: 'velocity' },
];
const PROTOCOL = Number(process.env.NEG_PROTOCOL ?? 2);
const USERNAME = process.env.NEG_USER || 'NegProtoBot';
const TIMEOUT_MS = Number(process.env.NEG_TIMEOUT || 10000);

function varint(n) {
  const out = [];
  while (true) {
    if ((n & ~0x7f) === 0) { out.push(n); return Buffer.from(out); }
    out.push((n & 0x7f) | 0x80);
    n >>>= 7;
  }
}
function str(s) {
  const b = Buffer.from(s, 'utf8');
  return Buffer.concat([varint(b.length), b]);
}
function frame(body) { return Buffer.concat([varint(body.length), body]); }

function readVarInt(b, o) {
  let r = 0, s = 0, n = 0;
  while (true) {
    if (o + n >= b.length) return null;
    const x = b[o + n]; n++;
    r |= (x & 0x7f) << s;
    if ((x & 0x80) === 0) return { v: r >>> 0, n };
    s += 7;
    if (n > 5) return null;
  }
}

function attempt(target) {
  return new Promise((resolve) => {
    const handshake = frame(Buffer.concat([
      varint(0x00), varint(PROTOCOL), str(target.host),
      Buffer.from([target.port >> 8, target.port & 0xff]), varint(2),
    ]));
    const loginStart = frame(Buffer.concat([varint(0x00), str(USERNAME)]));

    const sock = net.connect(target.port, target.host);
    sock.setTimeout(TIMEOUT_MS);
    let buf = Buffer.alloc(0);
    const t0 = Date.now();
    const result = { target, packets: [], closed: false, timedOut: false, error: null };

    sock.on('connect', () => {
      console.log(`[${target.label}] connected ${target.host}:${target.port} protocol=${PROTOCOL}`);
      sock.write(handshake);
      setTimeout(() => sock.write(loginStart), 50);
    });
    sock.on('data', (c) => {
      buf = Buffer.concat([buf, c]);
      console.log(`[${target.label}] t+${Date.now() - t0}ms recv ${c.length}B ${c.toString('hex').slice(0, 300)}`);
    });
    sock.on('close', () => {
      result.closed = true;
      console.log(`[${target.label}] closed t+${Date.now() - t0}ms, total ${buf.length}B`);
      parsePackets(buf, result);
      resolve(result);
    });
    sock.on('timeout', () => {
      result.timedOut = true;
      console.log(`[${target.label}] timeout after ${TIMEOUT_MS}ms with ${buf.length}B`);
      parsePackets(buf, result);
      sock.destroy();
      resolve(result);
    });
    sock.on('error', (e) => {
      result.error = e.message;
      console.log(`[${target.label}] error: ${e.message}`);
      resolve(result);
    });
  });
}

function parsePackets(b, result) {
  let off = 0;
  while (off < b.length) {
    const l = readVarInt(b, off);
    if (!l) break;
    const total = l.n + l.v;
    if (total > b.length - off) break;
    const body = b.subarray(off + l.n, total);
    const idv = readVarInt(body, 0);
    if (!idv) break;
    const entry = { id: idv.v, length: l.v };
    if (idv.v === 0x00) {
      // Login disconnect: id 0x00 + string reason (JSON text component).
      const sl = readVarInt(body, idv.n);
      if (sl && idv.n + sl.n + sl.v <= body.length) {
        entry.text = body.subarray(idv.n + sl.n, idv.n + sl.n + sl.v).toString('utf8');
      }
    }
    result.packets.push(entry);
    off = total;
  }
}

async function main() {
  for (const target of CANDIDATES) {
    const r = await attempt(target);
    const disconnect = r.packets.find((p) => p.id === 0x00 && p.text);
    if (disconnect) {
      const text = disconnect.text;
      let plain = text;
      try { plain = JSON.parse(text).text ?? text; } catch { /* not json */ }
      const ok = /unsupported/i.test(plain);
      console.log(`\nLOGIN_DISCONNECT on ${r.target.label}: "${plain}"`);
      console.log(`unsupported-version message present: ${ok}`);
      process.exit(ok ? 0 : 4);
    }
    console.log(`\n[${r.target.label}] no login disconnect parsed (closed=${r.closed} timeout=${r.timedOut} error=${r.error} packets=${r.packets.length})`);
  }
  console.log('\nFAIL: no login disconnect received on any target');
  process.exit(3);
}

main();
