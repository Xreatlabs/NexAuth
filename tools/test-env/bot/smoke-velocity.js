import mineflayer from 'mineflayer';

/**
 * End-to-end smoke through Velocity (127.0.0.1:25565) against the embedded
 * NexAuth limbo. Register auto-authorizes, so the transfer to the backend can
 * happen before mineflayer ever emits the limbo spawn; the reliable client-side
 * markers for "moved to backend" are (a) a configuration re-entry after the
 * first play state and (b) a spawn whose dimension/gamemode differ from the
 * limbo's the_end/adventure. Server-side confirmation is grepped from the
 * Velocity/Paper logs by the caller.
 *
 * Stages (each with its own deadline):
 *   S1 connected + held in limbo play state, not kicked
 *   S2 >= 1 keepalive received BEFORE any transfer begins
 *   S3 /register -> plugin "Registered!" reply
 *   S4 authorization confirmed (register auto-auth or /login reply)
 *   S5 transfer: config re-entry observed + backend-marker spawn
 *
 * Exit codes: 0 pass, 1 kick/error/end, 2 stage timeout.
 */

const version = process.argv[2] || '1.21.11';
const username = process.argv[3] || `S${Date.now().toString(36).slice(-8)}`;
const password = process.env.MC_TEST_PASSWORD || 'TestPass123!';
const timeoutMs = Number(process.env.MC_TEST_TIMEOUT || 60000);

const options = {
  host: process.env.MC_HOST || '127.0.0.1',
  port: Number(process.env.MC_PORT || 25565),
  username,
  auth: 'offline',
  physicsEnabled: false,
  version,
};
if (options.version === 'auto') delete options.version;

const bot = mineflayer.createBot(options);

const stage = {
  playReached: false,
  keepalivesBeforeTransfer: 0,
  registered: false,
  authorized: false,
  configReentry: false,
  backendSpawn: false,
  limboSpawnSeen: false,
};
const messages = [];
let finished = false;
const failures = [];
const t0 = Date.now();
const ts = () => `t+${((Date.now() - t0) / 1000).toFixed(1)}s`;

function finish(code, headline) {
  if (finished) return;
  finished = true;
  Object.values(watchers).forEach((t) => clearTimeout(t));
  if (headline) console.log(headline);
  console.log(`STAGES ${JSON.stringify(stage)}`);
  if (failures.length) console.log(`ASSERTION_FAILURES ${JSON.stringify(failures)}`);
  console.log('PLUGIN_MESSAGES:');
  messages.forEach((m) => console.log(`  ${m}`));
  try { bot.quit(); } catch { /* shutdown races */ }
  setTimeout(() => process.exit(code), 400);
}

// Movement suppression while unconfigured; re-enabled on every spawn.
let suppressMovement = false;
const writePacket = bot._client.write.bind(bot._client);
bot._client.write = (name, params) => {
  if (suppressMovement && ['position', 'position_look', 'look', 'flying'].includes(name)) return;
  return writePacket(name, params);
};
bot._client.on('start_configuration', () => {
  suppressMovement = true;
  bot.setSettings?.({});
  if (stage.playReached && !stage.configReentry) {
    stage.configReentry = true;
    console.log(`${ts()} TRANSFER began: client re-entered configuration phase`);
  }
});

bot.on('spawn', () => {
  suppressMovement = false;
  const p = bot.entity?.position;
  const dim = bot.game?.dimension;
  const gm = bot.game?.gameMode;
  const isLimboMarker = String(dim).includes('the_end') && gm === 2;
  console.log(`${ts()} SPAWN pos=${p ? `${p.x.toFixed(1)},${p.y.toFixed(1)},${p.z.toFixed(1)}` : '?'} dim=${dim} gamemode=${gm}${gm === 2 || isLimboMarker ? ' (limbo markers)' : ' (backend markers)'}`);
  if (isLimboMarker && !stage.limboSpawnSeen) {
    stage.limboSpawnSeen = true;
  } else if (stage.configReentry || (dim && !String(dim).includes('the_end'))) {
    stage.backendSpawn = true;
  } else if (!stage.limboSpawnSeen) {
    // First spawn without explicit markers: accept as the initial hold.
    stage.limboSpawnSeen = true;
  }
});

bot._client.on('keep_alive', () => {
  if (!stage.configReentry) stage.keepalivesBeforeTransfer++;
});

bot.on('login', () => {
  stage.playReached = true;
  console.log(`${ts()} client reached play state`);
});

let sentRegister = false;
let sentLogin = false;
let chatBlockedUntil = 0;
function safeChat(text) {
  if (Date.now() < chatBlockedUntil) {
    setTimeout(() => safeChat(text), 1500);
    return;
  }
  bot.chat(text);
}

bot.on('message', (message) => {
  const text = message.toString();
  messages.push(`${ts()} ${text}`);
  console.log(`${ts()} [chat] ${text}`);
  if (/commands too fast/i.test(text)) {
    chatBlockedUntil = Date.now() + 4000;
    return;
  }
  if (/Please register/i.test(text) && !sentRegister && stage.playReached) {
    sentRegister = true;
    setTimeout(() => {
      console.log(`${ts()} -> /register`);
      safeChat(`/register ${password} ${password}`);
    }, 1200);
    return;
  }
  if (/^Registered/i.test(text)) {
    stage.registered = true;
    setTimeout(() => {
      if (sentLogin) return;
      sentLogin = true;
      console.log(`${ts()} -> /login`);
      safeChat(`/login ${password}`);
    }, 2000);
    return;
  }
  if (/already authorized/i.test(text) || /^Logged in/i.test(text) || /successfully logged in/i.test(text)) {
    stage.authorized = true;
  }
});

bot.on('kicked', (reason) => {
  finish(1, `${ts()} KICK ${formatReason(reason)}\nMESSAGES:\n${messages.join('\n')}`);
});
bot.on('error', (error) => {
  finish(1, `${ts()} ERROR ${error.stack || error.message}`);
});
bot.on('end', () => {
  if (!finished) finish(1, `${ts()} END before all stages passed ${JSON.stringify(stage)}`);
});

function formatReason(reason) {
  if (typeof reason === 'string') return reason;
  try { return JSON.stringify(reason); } catch { return String(reason); }
}

// ---- stage watchers ----
const watchers = {};
function watch(name, ms, check) {
  const interval = setInterval(() => {
    if (finished) { clearInterval(interval); return; }
    if (check()) { clearInterval(interval); console.log(`${ts()} OK ${name}`); }
  }, 100);
  watchers[name] = setTimeout(() => {
    clearInterval(interval);
    if (!finished && !check()) {
      failures.push(`${name} not satisfied within ${ms}ms`);
      finish(2, `${ts()} TIMEOUT at ${name}`);
    }
  }, ms);
}

watch('S1-limbo-hold', 20000, () => stage.playReached, 'reached play state, held without kick');
watch('S2-keepalive-in-limbo', 25000, () => stage.keepalivesBeforeTransfer >= 1, 'keepalive while held pre-auth');
watch('S3-register-reply', 30000, () => stage.registered, '"Registered!" reply');
watch('S4-authorized', 45000, () => stage.authorized, 'authorization confirmed');
watch('S5-backend-transfer', timeoutMs, () => stage.configReentry && stage.backendSpawn,
  'config re-entry + backend-marker spawn');
