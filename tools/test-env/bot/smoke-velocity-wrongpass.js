import mineflayer from 'mineflayer';

/**
 * Wrong-password path: register, then reconnect and send a wrong /login.
 * Expect the plugin's wrong-password error message and NO crash/kick of the
 * connection (player stays in limbo, can retry).
 */

const version = process.argv[2] || '1.21.11';
const username = process.argv[3] || `W${Date.now().toString(36).slice(-8)}`;
const password = process.env.MC_TEST_PASSWORD || 'TestPass123!';
const wrongPassword = process.env.MC_TEST_WRONG_PASSWORD || 'WrongPass456!';
const timeoutMs = Number(process.env.MC_TEST_TIMEOUT || 90000);

const options = {
  host: process.env.MC_HOST || '127.0.0.1',
  port: Number(process.env.MC_PORT || 25565),
  username,
  auth: 'offline',
  physicsEnabled: false,
  version,
};

let finished = false;
let phase = 'register';
let registerSent = false;
let wrongSent = false;
let sawWrongPasswordError = false;
let chatBlockedUntil = 0;
const messages = [];

function finish(code, message) {
  if (finished) return;
  finished = true;
  clearTimeout(timeout);
  if (message) console.log(message);
  console.log(`SawWrongPasswordError=${sawWrongPasswordError}`);
  try { bot.quit(); } catch { /* races */ }
  setTimeout(() => process.exit(code), 300);
}

const timeout = setTimeout(() => {
  finish(2, `TIMEOUT ${version} ${username} phase=${phase}\nMESSAGES:\n${messages.join('\n')}`);
}, timeoutMs);

const bot = mineflayer.createBot(options);
let suppressMovement = false;
const writePacket = bot._client.write.bind(bot._client);
bot._client.write = (name, params) => {
  if (suppressMovement && ['position', 'position_look', 'look', 'flying'].includes(name)) return;
  return writePacket(name, params);
};
bot._client.on('start_configuration', () => {
  suppressMovement = true;
  bot.setSettings?.({});
});

function safeChat(text) {
  if (Date.now() < chatBlockedUntil) {
    setTimeout(() => safeChat(text), 1000);
    return;
  }
  bot.chat(text);
}

bot.once('login', () => console.log(`LOGIN ${version} ${username}`));

bot.on('spawn', () => { suppressMovement = false; });

bot.on('message', (message) => {
  const text = message.toString();
  messages.push(text);
  console.log(`[chat] ${text}`);

  if (/commands too fast/i.test(text)) {
    chatBlockedUntil = Date.now() + 3000;
    return;
  }

  if (phase === 'register') {
    if (/Please register/i.test(text) && !registerSent) {
      registerSent = true;
      safeChat(`/register ${password} ${password}`);
      return;
    }
    if (/^Registered/i.test(text)) {
      // Registered. Close and reconnect to get a fresh unauthenticated session.
      setTimeout(() => {
        phase = 'reconnect';
        bot.quit();
      }, 1500);
      return;
    }
    return;
  }

  if (phase === 'wrong-login') {
    if (/Please login/i.test(text) && !wrongSent) {
      wrongSent = true;
      setTimeout(() => safeChat(`/login ${wrongPassword}`), 2500);
      return;
    }
    if (/incorrect|wrong|invalid/i.test(text) && /password/i.test(text)) {
      sawWrongPasswordError = true;
      console.log('WRONG_PASSWORD_ERROR_RECEIVED — still connected, no crash');
      // Give it a moment to prove the connection survives, then pass.
      setTimeout(() => finish(0, `PASS wrong-password error delivered, connection held`), 4000);
      return;
    }
    if (/kicked|banned/i.test(text)) {
      finish(1, `UNEXPECTED_KICK ${text}`);
    }
  }
});

bot.on('kicked', (reason) => {
  if (phase === 'reconnect') return;
  finish(1, `KICK ${formatReason(reason)}\nMESSAGES:\n${messages.join('\n')}`);
});
bot.on('error', (error) => {
  if (phase === 'reconnect') return;
  finish(1, `ERROR ${error.stack || error.message}`);
});
bot.on('end', () => {
  if (finished) return;
  if (phase === 'reconnect') {
    phase = 'wrong-login';
    setTimeout(() => reconnect(), 3000);
    return;
  }
  finish(1, `END phase=${phase}`);
});

function reconnect() {
  const fresh = mineflayer.createBot({ ...options });
  fresh.once('login', () => console.log(`RELOGIN ${version} ${username}`));
  fresh.on('message', (m) => bot.emit('message', m));
  fresh.on('kicked', (r) => bot.emit('kicked', r));
  fresh.on('error', (e) => bot.emit('error', e));
  fresh.on('end', () => bot.emit('end'));
  fresh._client.on('start_configuration', () => {
    fresh.setSettings?.({});
  });
  // rebind chat helper to the fresh bot
  bot.chat = (t) => fresh.chat(t);
  Object.defineProperty(bot, '_client', { value: fresh._client, configurable: true });
  console.log('reconnected for wrong-password phase');
}

function formatReason(reason) {
  if (typeof reason === 'string') return reason;
  try { return JSON.stringify(reason); } catch { return String(reason); }
}
