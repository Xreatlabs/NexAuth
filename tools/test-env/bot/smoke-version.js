import mineflayer from 'mineflayer';

const version = process.argv[2] || 'auto';
const username = process.argv[3] || `T${Date.now().toString(36).slice(-8)}`;
const password = process.env.MC_TEST_PASSWORD || 'TestPass123!';
const timeoutMs = Number(process.env.MC_TEST_TIMEOUT || 30000);

let suppressMovement = false;
let registered = false;
let finished = false;

const options = {
  host: process.env.MC_HOST || '127.0.0.1',
  port: Number(process.env.MC_PORT || 25565),
  username,
  auth: 'offline',
  physicsEnabled: false,
  version,
};

if (options.version === 'auto') {
  delete options.version;
}

const bot = mineflayer.createBot(options);
const writePacket = bot._client.write.bind(bot._client);

bot._client.write = (name, params) => {
  if (suppressMovement && ['position', 'position_look', 'look', 'flying'].includes(name)) return;
  return writePacket(name, params);
};

function finish(code, message) {
  if (finished) return;
  finished = true;
  clearTimeout(timeout);
  if (message) console.log(message);
  try {
    bot.quit();
  } catch {
    // Ignore shutdown races in failed handshakes.
  }
  setTimeout(() => process.exit(code), 200);
}

const timeout = setTimeout(() => {
  finish(2, `TIMEOUT ${version} ${username}`);
}, timeoutMs);

bot._client.on('start_configuration', () => {
  suppressMovement = true;
  bot.setSettings?.({});
});

bot.once('login', () => {
  console.log(`LOGIN ${version} ${username}`);
});

bot.once('spawn', () => {
  suppressMovement = false;
  const pos = bot.entity?.position;
  finish(0, `SPAWN ${version} ${username} ${pos ? `${pos.x.toFixed(1)},${pos.y.toFixed(1)},${pos.z.toFixed(1)}` : 'unknown'}`);
});

bot.on('message', (message) => {
  const text = message.toString();
  if (!registered && /register/i.test(text)) {
    registered = true;
    bot.chat(`/register ${password} ${password}`);
  }
});

bot.on('kicked', (reason) => {
  finish(1, `KICK ${version} ${username} ${formatReason(reason)}`);
});

bot.on('error', (error) => {
  finish(1, `ERROR ${version} ${username} ${error.stack || error.message}`);
});

bot.on('end', () => {
  finish(1, `END ${version} ${username}`);
});

function formatReason(reason) {
  if (typeof reason === 'string') return reason;
  try {
    return JSON.stringify(reason);
  } catch {
    return String(reason);
  }
}
