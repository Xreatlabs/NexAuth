import mineflayer from 'mineflayer';

const version = process.argv[2] || 'auto';
const username = process.argv[3] || `P${Date.now().toString(36).slice(-8)}`;
const password = process.env.MC_TEST_PASSWORD || 'TestPass123!';
const timeoutMs = Number(process.env.MC_TEST_TIMEOUT || 45000);

let spawned = false;
let registerSent = false;
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

function finish(code, message) {
  if (finished) return;
  finished = true;
  clearTimeout(timeout);
  if (message) console.log(message);
  try {
    bot.quit();
  } catch {
    // Ignore shutdown races.
  }
  setTimeout(() => process.exit(code), 200);
}

function maybeFinish() {
  if (spawned && registered) {
    const pos = bot.entity?.position;
    finish(0, `PASS ${version} ${username} ${pos ? `${pos.x.toFixed(1)},${pos.y.toFixed(1)},${pos.z.toFixed(1)}` : 'unknown'}`);
  }
}

const timeout = setTimeout(() => {
  finish(2, `TIMEOUT ${version} ${username} spawned=${spawned} registered=${registered}`);
}, timeoutMs);

bot.once('login', () => {
  console.log(`LOGIN ${version} ${username}`);
});

bot.once('spawn', () => {
  spawned = true;
  maybeFinish();
});

bot.on('message', (message) => {
  const text = message.toString();
  if (!registerSent && /register/i.test(text)) {
    registerSent = true;
    bot.chat(`/register ${password} ${password}`);
  }
  if (/registered|success/i.test(text)) {
    registered = true;
    maybeFinish();
  }
});

bot.on('kicked', (reason) => {
  finish(1, `KICK ${version} ${username} ${formatReason(reason)}`);
});

bot.on('error', (error) => {
  finish(1, `ERROR ${version} ${username} ${error.stack || error.message}`);
});

bot.on('end', () => {
  finish(1, `END ${version} ${username} spawned=${spawned} registered=${registered}`);
});

function formatReason(reason) {
  if (typeof reason === 'string') return reason;
  try {
    return JSON.stringify(reason);
  } catch {
    return String(reason);
  }
}
