/** Форматування грошей, часу й відстаней (українська локаль, час за Києвом). */
import { CFG } from './config.js';

const nf = new Intl.NumberFormat('uk-UA', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const nf0 = new Intl.NumberFormat('uk-UA', { maximumFractionDigits: 0 });

/** Копійки в «181,60 грн»; зайві нулі прибираються лише в цілих сумах, якщо compact. */
export function money(kop, { compact = false, sign = false } = {}) {
  const neg = kop < 0;
  const abs = Math.abs(kop);
  let s = (abs % 100 === 0 && compact) ? nf0.format(abs / 100) : nf.format(abs / 100);
  s = s.replace(/ /g, ' ');
  const mark = neg ? '−' : (sign && kop > 0 ? '+' : '');
  return `${mark}${s} грн`;
}

/** Ціна поїздки в копійках: розблокування плюс хвилини за тарифом. */
export const ridePrice = (unlock, perMin, minutes) => unlock + minutes * perMin;

/** Хвилини до оплати: початок щонайменше одна хвилина, округлення вгору, з урахуванням прискорення демо. */
export function rideMinutes(elapsedMs, factor = 1) {
  return Math.max(1, Math.ceil((elapsedMs * factor) / 60000));
}

const dtf = new Intl.DateTimeFormat('uk-UA', { timeZone: CFG.TIME_ZONE, day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit', hour12: false });
const tf = new Intl.DateTimeFormat('uk-UA', { timeZone: CFG.TIME_ZONE, hour: '2-digit', minute: '2-digit', hour12: false });
const df = new Intl.DateTimeFormat('uk-UA', { timeZone: CFG.TIME_ZONE, day: '2-digit', month: '2-digit', year: 'numeric' });
const full = new Intl.DateTimeFormat('uk-UA', { timeZone: CFG.TIME_ZONE, day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit', hour12: false });
const ymd = new Intl.DateTimeFormat('en-CA', { timeZone: CFG.TIME_ZONE, year: 'numeric', month: '2-digit', day: '2-digit' });

export const dateTime = (iso) => dtf.format(new Date(iso)).replace(',', '');
export const timeOnly = (iso) => tf.format(new Date(iso));
export const dateOnly = (iso) => df.format(new Date(iso));
export const fullTime = (iso) => full.format(new Date(iso)).replace(',', '');
/** Дата за Києвом у вигляді 2026-11-19. */
export const kyivDate = (d = new Date()) => ymd.format(d);
export const kyivDateCompact = (d = new Date()) => kyivDate(d).replace(/-/g, '');

/** Тривалість у вигляді 12:03 або 1:02:03. */
export function clock(ms) {
  const t = Math.max(0, Math.floor(ms / 1000));
  const h = Math.floor(t / 3600), m = Math.floor((t % 3600) / 60), s = t % 60;
  const p = (n) => String(n).padStart(2, '0');
  return h ? `${h}:${p(m)}:${p(s)}` : `${p(m)}:${p(s)}`;
}

/** Відстань: «420 м» або «1,2 км». */
export function dist(m) {
  if (m < 950) return `${Math.round(m / 10) * 10} м`;
  return `${(m / 1000).toLocaleString('uk-UA', { maximumFractionDigits: 1, minimumFractionDigits: 1 })} км`;
}

/** Пішки: хвилини, округлені вгору, за відстанню по прямій із запасом на вигини. */
export const walkMin = (m) => Math.max(1, Math.ceil((m * CFG.WALK_FACTOR) / CFG.WALK_M_PER_MIN));

/** Екранування для вставки тексту в HTML. */
export const esc = (s) => String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

/** Хвилин: правильна форма слова. */
export function minWord(n) {
  const a = n % 10, b = n % 100;
  if (a === 1 && b !== 11) return 'хвилина';
  if (a >= 2 && a <= 4 && (b < 12 || b > 14)) return 'хвилини';
  return 'хвилин';
}
