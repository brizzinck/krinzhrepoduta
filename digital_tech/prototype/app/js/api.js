/**
 * Єдиний шар даних, який викликає інтерфейс. У прототипі працює над localStorage й імітованими
 * операторами; у MVP тіла функцій замінюються на fetch до серверної частини. Усі функції повертають Promise.
 */
import { CFG, OPERATORS, TYPES, TILES, opByCode } from './config.js';
import * as store from './store.js';
import * as fleetLib from './fleet.js';
import * as land from './landcheck.js';
import { feeds } from './gbfs.js';
import { money, ridePrice, rideMinutes, kyivDate, kyivDateCompact, walkMin } from './format.js';

/** Помилка бізнес-правила з кодом і текстом для користувача. */
export class ApiError extends Error {
  constructor(code, message) { super(message); this.code = code; }
}

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

/** Поточна вартість поїздки за станом на момент now. */
export function runningOf(ride, now = Date.now()) {
  const minutes = rideMinutes(now - Date.parse(ride.startedAt), ride.timeFactor);
  return { minutes, price: ridePrice(ride.unlock, ride.perMin, minutes) };
}

/* ---------- автопарк ---------- */

let genPromise = null;

/** Гарантувати автопарк для позиції: створити або переформувати за правилами (не під час поїздки). */
export async function ensureFleet(pos) {
  const s = store.get();
  const date = kyivDate();
  if (!fleetLib.needsRegen(s.fleet, pos, date) || s.activeRide) return s.fleet;
  if (genPromise) return genPromise;
  genPromise = (async () => {
    let checked = false;
    try {
      const r = await land.prefetch(pos, CFG.RADIUS_M);
      checked = r.loaded > 0;
    } catch { /* без перевірки за плитками */ }
    const fleet = fleetLib.generate({
      center: pos, tgId: store.get().user.tgId, date, centerSource: pos.source,
      isLand: checked ? land.isLand : () => true, tries: TILES.TRIES_PER_POINT,
    });
    store.update((st) => { st.fleet = fleet; });
    store.logEvent('FLEET_GENERATED', { vehicles: fleet.vehicles.length, source: pos.source, tileCheck: checked });
    return fleet;
  })().finally(() => { genPromise = null; });
  return genPromise;
}

/** Пропозиції для карти й списку: лише з каналів GBFS, відстань за гаверсинусом. */
export async function getOffers({ pos, type = 'ALL', sort = 'price' }) {
  const fleet = await ensureFleet(pos);
  const out = [];
  for (const f of feeds(fleet)) {
    const plans = new Map(f.system_pricing_plans.plans.map((p) => [p.plan_id, p]));
    for (const v of f.vehicle_status.vehicles) {
      if (v.is_reserved || v.is_disabled) continue;
      if (type !== 'ALL' && v.vehicle_type_id !== type) continue;
      const plan = plans.get(v.pricing_plan_id);
      const unlock = Math.round(plan.price * 100), perMin = Math.round(plan.per_min_pricing[0].rate * 100);
      const d = fleetLib.haversine(pos, { lat: v.lat, lng: v.lon });
      out.push({
        id: v.vehicle_id, op: f.op, opName: opByCode(f.op).name, colorVar: opByCode(f.op).colorVar,
        type: v.vehicle_type_id, typeName: TYPES[v.vehicle_type_id].name,
        lat: v.lat, lng: v.lon, battery: Math.round(v.current_fuel_percent * 100),
        unlock, perMin, est10: ridePrice(unlock, perMin, 10),
        distance: d, walk: walkMin(d),
      });
    }
  }
  const cmp = {
    price: (a, b) => a.est10 - b.est10 || a.distance - b.distance,
    distance: (a, b) => a.distance - b.distance,
    battery: (a, b) => b.battery - a.battery || a.distance - b.distance,
  }[sort] || (() => 0);
  out.sort(cmp);
  // транспорт із зарядом нижче порогу відмови (оператор не розблокує) йде в кінець списку
  const ok = out.filter((o) => o.battery >= CFG.BATTERY_REFUSE), low = out.filter((o) => o.battery < CFG.BATTERY_REFUSE);
  return ok.concat(low);
}

/* ---------- особа, згода, гаманець ---------- */

export async function acceptTerms() {
  if (store.get().user.consentAt) return store.get().user.consentAt;
  const at = new Date().toISOString();
  store.update((s) => { s.user.consentAt = at; });
  store.logEvent('CONSENT', { terms: true, privacy: true });
  return at;
}

/** Імітація Дія / BankID: короткий спінер, далі підтверджено. */
export async function confirmIdentity(method) {
  if (method !== 'DIIA' && method !== 'BANKID') throw new ApiError('BAD_METHOD', 'Невідомий спосіб підтвердження');
  await sleep(CFG.IDENTITY_MS);
  const at = new Date().toISOString();
  store.update((s) => { s.user.identity = { method, at }; });
  store.logEvent('ID_VERIFIED', { method });
  return store.get().user.identity;
}

/** Імітація платіжного шлюзу: поповнення гаманця на суму з дозволеного списку. */
export async function topUp(amountKop) {
  if (!CFG.TOPUPS.includes(amountKop)) throw new ApiError('BAD_AMOUNT', 'Оберіть суму 100, 200 або 500 грн');
  await sleep(CFG.GATEWAY_MS);
  const at = new Date().toISOString();
  store.update((s) => {
    s.wallet.balance += amountKop;
    s.wallet.tx.push({ id: store.newId('tx'), type: 'TOPUP', amount: amountKop, at, rideId: null, balanceAfter: s.wallet.balance });
  });
  store.logEvent('TOPUP', { amount: amountKop });
  return store.get().wallet;
}

/* ---------- поїздка ---------- */

let unlocking = false;   // не дозволяє двом розблокуванням іти одночасно

/** Розблокування. Перевірки по порядку, кожна з власним повідомленням. */
export async function startRide(vehicleId) {
  if (unlocking) throw new ApiError('RIDE_ACTIVE', 'Розблокування вже триває');
  unlocking = true;
  try { return await doStartRide(vehicleId); } finally { unlocking = false; }
}

async function doStartRide(vehicleId) {
  const s = store.get();
  const fail = (code, msg) => { store.logEvent('UNLOCK_REFUSED', { code, vehicleId }); throw new ApiError(code, msg); };
  if (!s.user.consentAt) fail('NO_CONSENT', 'Спершу погодьтеся з Умовами використання й Політикою конфіденційності');
  if (!s.user.identity) fail('NO_IDENTITY', 'Спершу підтвердіть особу через Дію або BankID');
  if (s.wallet.balance < 0) fail('DEBT', `Погасіть борг ${money(-s.wallet.balance)}`);
  if (s.wallet.balance < CFG.MIN_BALANCE) fail('LOW_BALANCE', `Для розблокування потрібно щонайменше ${money(CFG.MIN_BALANCE, { compact: true })} на балансі`);
  if (s.activeRide) fail('RIDE_ACTIVE', 'У вас уже є активна поїздка');
  const v = s.fleet && s.fleet.vehicles.find((x) => x.id === vehicleId);
  if (!v || v.status !== 'AVAILABLE') fail('TAKEN', 'Цей транспорт уже недоступний. Оберіть інший');
  if (s.settings.failNext === 'TAKEN') {
    store.update((st) => { st.settings.failNext = null; st.fleet.vehicles = st.fleet.vehicles.filter((x) => x.id !== vehicleId); });
    fail('TAKEN', 'Транспорт щойно забрав інший користувач. Оберіть інший');
  }
  await sleep(CFG.UNLOCK_MS); // відповідь імітованого оператора
  if (store.get().activeRide || v.status !== 'AVAILABLE') fail('RIDE_ACTIVE', 'У вас уже є активна поїздка');
  if (v.battery < CFG.BATTERY_REFUSE || s.settings.failNext === 'REFUSED') {
    store.update((st) => { st.settings.failNext = null; });
    fail('REFUSED', 'Оператор відмовив у розблокуванні. Оберіть інший транспорт');
  }
  const plan = opByCode(v.op).types[v.type];
  const ride = {
    id: store.newId('ride'), vehicleId, op: v.op, type: v.type,
    unlock: plan.unlock, perMin: plan.perMin,
    startedAt: new Date().toISOString(), timeFactor: s.settings.timeFactor, warned: false,
  };
  store.update((st) => { st.activeRide = ride; st.fleet.vehicles.find((x) => x.id === vehicleId).status = 'IN_RIDE'; });
  store.logEvent('UNLOCK', { vehicleId, op: v.op });
  return ride;
}

/** Позначити, що попередження про залишок уже показано. */
export function markWarned() {
  store.update((s) => { if (s.activeRide) s.activeRide.warned = true; });
  const r = store.get().activeRide;
  store.logEvent('LOW_BALANCE', { left: store.get().wallet.balance - (r ? runningOf(r).price : 0) });
}

/** Завершення: списання з гаманця (мінус дозволений, це борг), чек створюється один раз. */
export async function finishRide(pos) {
  const s = store.get();
  const r = s.activeRide;
  if (!r) throw new ApiError('NO_RIDE', 'Активної поїздки немає');
  // у демо-часі кінець поїздки відраховується від початку з тим самим прискоренням, тож чек узгоджений
  const realElapsed = Date.now() - Date.parse(r.startedAt);
  const endedAt = new Date(Date.parse(r.startedAt) + realElapsed * r.timeFactor);
  const minutes = rideMinutes(realElapsed, r.timeFactor);
  const price = ridePrice(r.unlock, r.perMin, minutes);
  const commission = Math.round(price * CFG.COMMISSION); // для адмінпанелі MVP, користувачу не показується
  const n = s.rides.length + 1;
  const ride = {
    id: r.id, receiptNo: `${CFG.RECEIPT_PREFIX}-${kyivDateCompact(endedAt)}-${String(n).padStart(4, '0')}`,
    vehicleId: r.vehicleId, op: r.op, type: r.type, unlock: r.unlock, perMin: r.perMin,
    startedAt: r.startedAt, endedAt: endedAt.toISOString(), issuedAt: new Date().toISOString(), minutes, price, commission, balanceAfter: 0,
  };
  store.update((st) => {
    st.wallet.balance -= price;
    ride.balanceAfter = st.wallet.balance;
    st.rides.push(ride);
    st.wallet.tx.push({ id: store.newId('tx'), type: 'CHARGE', amount: -price, at: ride.endedAt, rideId: ride.id, balanceAfter: st.wallet.balance });
    if (st.fleet) fleetLib.afterRide(st.fleet, { vehicleId: r.vehicleId, minutes, pos });
    st.activeRide = null;
  });
  store.logEvent('RIDE_FINISH', { rideId: ride.id, minutes });
  store.logEvent('CHARGE', { rideId: ride.id, amount: price });
  return ride;
}

export async function getHistory() { return [...store.get().rides].reverse(); }
export async function getReceipt(rideId) {
  const r = store.get().rides.find((x) => x.id === rideId);
  if (!r) throw new ApiError('NOT_FOUND', 'Чек не знайдено');
  return r;
}

/* ---------- службове (Профіль, демо-інструменти) ---------- */

export function setTimeFactor(f) { store.update((s) => { s.settings.timeFactor = f === 60 ? 60 : 1; }); }
export function setFailNext(v) { store.update((s) => { s.settings.failNext = v; }); }
/** Демо-інструмент: виставити баланс; різницю видно в списку операцій окремим записом. */
export function setBalance(kop) {
  store.update((s) => {
    const delta = kop - s.wallet.balance;
    s.wallet.balance = kop;
    if (delta) s.wallet.tx.push({ id: store.newId('tx'), type: 'ADJUST', amount: delta, at: new Date().toISOString(), rideId: null, balanceAfter: kop });
  });
}

export function resetAll(user) {
  land.clearCache();
  return store.reset(user);
}

export { OPERATORS };
