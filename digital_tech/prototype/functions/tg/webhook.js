/**
 * Cloudflare Pages Function: вебхук бота «єМобільність».
 * Відповідає на повідомлення кнопкою, що відкриває Mini App. Секрети BOT_TOKEN і WEBHOOK_SECRET
 * задаються в налаштуваннях проєкту Pages, у коді їх немає. Токен і текст повідомлень не логуються.
 */
const APP_URL = 'https://emobility.pages.dev';
const SCREENS = new Set(['s1', 's2', 's3', 's4', 's5']);

const TEXT = 'Вітаємо в єМобільності! Знайдіть електросамокат або електровелосипед поруч, порівняйте ціни й заряд і поїдьте за кілька дотиків. Натисніть «Відкрити».\n\nНавчальний проєкт, дані операторів імітовані.';

/** Адреса Mini App за параметром /start (дозволені лише s1..s5); екран передається як ?startapp=, бо hash займає Telegram. */
export function appUrl(text) {
  const m = /^\/start(?:@\w+)?(?:\s+(\S+))?/.exec(text || '');
  const p = m && m[1];
  return p && SCREENS.has(p) ? `${APP_URL}/?startapp=${p}` : APP_URL;
}

export async function onRequestPost({ request, env, waitUntil }) {
  if (!env.WEBHOOK_SECRET || request.headers.get('X-Telegram-Bot-Api-Secret-Token') !== env.WEBHOOK_SECRET) {
    return new Response('unauthorized', { status: 401 });
  }
  try {
    const update = await request.json();
    const msg = update && update.message;
    if (msg && msg.chat && msg.chat.type === 'private' && typeof msg.text === 'string' && env.BOT_TOKEN) {
      // відповідь Telegram одразу, повідомлення користувачу надсилається у фоні
      const send = fetch(`https://api.telegram.org/bot${env.BOT_TOKEN}/sendMessage`, {
        method: 'POST',
        headers: { 'content-type': 'application/json' },
        body: JSON.stringify({
          chat_id: msg.chat.id,
          text: TEXT,
          reply_markup: { inline_keyboard: [[{ text: 'Відкрити', web_app: { url: appUrl(msg.text) } }]] },
        }),
      }).catch(() => {});
      if (typeof waitUntil === 'function') waitUntil(send);
    }
  } catch { /* Telegram повторює запити при помилках, тож завжди відповідаємо 200 */ }
  return new Response('ok', { status: 200 });
}

export function onRequestGet() {
  return new Response('method not allowed', { status: 405, headers: { Allow: 'POST' } });
}
