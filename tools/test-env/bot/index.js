import mineflayer from 'mineflayer';
import readline from 'node:readline';

const options = {
  host: process.env.MC_HOST || '127.0.0.1',
  port: Number(process.env.MC_PORT || 25565),
  username: process.env.MC_USERNAME || 'NexAuthBot',
  auth: 'offline',
  version: process.env.MC_VERSION || '1.21.11',
  physicsEnabled: process.env.MC_PHYSICS === 'true',
};

if (options.version === 'auto') {
  delete options.version;
}

let bot;
let reconnect = false;
let suppressMovement = false;
const debugPackets = process.env.BOT_DEBUG_PACKETS === 'true';

function log(message) {
  console.log(`[bot] ${message}`);
}

function connect() {
  log(`connecting to ${options.host}:${options.port} as ${options.username}`);
  suppressMovement = false;
  bot = mineflayer.createBot(options);
  const writePacket = bot._client.write.bind(bot._client);
  bot._client.write = (name, params) => {
    if (suppressMovement && ['position', 'position_look', 'look', 'flying'].includes(name)) return;
    if (debugPackets) {
      console.log(`[packet->] ${name} ${JSON.stringify(params ?? {}, (_key, value) =>
        typeof value === 'bigint' ? value.toString() : value)}`);
    }
    return writePacket(name, params);
  };

  bot.once('login', () => log(`login accepted as ${bot.username}`));
  bot.once('spawn', () => {
    suppressMovement = false;
    bot.physicsEnabled = false;
    const pos = bot.entity?.position;
    log(`spawned${pos ? ` at ${pos.x.toFixed(1)}, ${pos.y.toFixed(1)}, ${pos.z.toFixed(1)}` : ''}`);
  });
  bot._client.on('start_configuration', () => {
    suppressMovement = true;
    bot.setSettings?.({});
  });
  bot.on('message', (message) => console.log(`[chat] ${message.toString()}`));
  bot.on('kicked', (reason) => log(`kicked: ${formatReason(reason)}`));
  bot.on('end', () => {
    log('connection ended');
    if (reconnect) setTimeout(connect, 3000);
  });
  bot.on('error', (error) => log(`error: ${error.message}`));
}

function formatReason(reason) {
  if (typeof reason === 'string') return reason;
  try {
    return JSON.stringify(reason);
  } catch {
    return String(reason);
  }
}

function sendChat(text) {
  if (!bot) {
    log('not connected');
    return;
  }
  bot.chat(text);
}

function printStatus() {
  if (!bot || !bot.player) {
    log('not connected');
    return;
  }
  const pos = bot.entity?.position;
  const players = Object.keys(bot.players).join(', ') || 'none';
  log(`connected=${bot.player.username} health=${bot.health} food=${bot.food} pos=${pos ? `${pos.x.toFixed(1)},${pos.y.toFixed(1)},${pos.z.toFixed(1)}` : 'unknown'} players=${players}`);
}

const rl = readline.createInterface({
  input: process.stdin,
  output: process.stdout,
  prompt: 'bot> ',
});

rl.on('line', (line) => {
  const input = line.trim();
  if (!input) {
    rl.prompt();
    return;
  }

  const [command, ...rest] = input.split(/\s+/);
  const arg = rest.join(' ');

  switch (command) {
    case '.help':
      log('commands: .help, .status, .players, .register <password>, .login <password>, .chat <text>, .reconnect, .quit; any other line is sent as chat/command');
      break;
    case '.status':
    case '.players':
      printStatus();
      break;
    case '.register':
      if (!arg) log('usage: .register <password>');
      else sendChat(`/register ${arg} ${arg}`);
      break;
    case '.login':
      if (!arg) log('usage: .login <password>');
      else sendChat(`/login ${arg}`);
      break;
    case '.chat':
      if (!arg) log('usage: .chat <text>');
      else sendChat(arg);
      break;
    case '.reconnect':
      reconnect = true;
      bot?.end();
      break;
    case '.quit':
      reconnect = false;
      bot?.quit();
      rl.close();
      return;
    default:
      sendChat(input);
  }

  rl.prompt();
});

rl.on('close', () => {
  reconnect = false;
  bot?.quit();
  process.exit(0);
});

connect();
rl.prompt();
