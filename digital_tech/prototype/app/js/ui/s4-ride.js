/** S4. Поїздка: таймер, поточна вартість, залишок, попередження, завершення. */
import { CFG, TYPES, opByCode } from '../config.js';
import * as store from '../store.js';
import * as api from '../api.js';
import * as geo from '../geo.js';
import * as tg from '../tg.js';
import { money, clock, esc, timeOnly, minWord } from '../format.js';
import { icon, markerSvg, openTopUp } from './components.js';

export function mount(root, ctx) {
  const r0 = store.get().activeRide;
  if (!r0) { ctx.nav('#s3', { toast: 'Активної поїздки немає' }); return () => {}; }
  const op = opByCode(r0.op);
  const typeName = TYPES[r0.type].name;

  root.innerHTML = `<div class="screen s4">
    <h1 class="sr-only">Поїздка</h1>
    <div class="ride-head"><span class="badge info live"><span class="pulse" aria-hidden="true"></span><span>Поїздка триває</span></span></div>
    <section class="card ride-card" aria-label="Поїздка">
      <div class="ride-id">
        <span class="o-mark big">${markerSvg({ op: r0.op, type: r0.type, battery: null, size: 56 })}</span>
        <div><div class="strong lg">${esc(op.name)}</div><div class="muted">${typeName}, ${esc(r0.vehicleId)}</div><div class="muted sm">Початок о ${timeOnly(r0.startedAt)}</div></div>
      </div>
      <div class="timer num" id="timer" role="timer" aria-label="Тривалість поїздки">00:00</div>
      <div class="rows">
        <div class="kv"><span class="muted">Вартість зараз</span><span class="strong num" id="cost"></span></div>
        <div class="kv"><span class="muted">Тариф</span><span class="num">${money(r0.unlock, { compact: true })} + ${money(r0.perMin)}/хв</span></div>
        <div class="kv"><span class="muted">Баланс гаманця</span><span class="strong num" id="bal"></span></div>
      </div>
    </section>
    <div id="lowbox"></div>
    <section class="card calc" aria-label="Розрахунок вартості">
      <h2>Розрахунок</h2>
      <div class="kv"><span class="muted">Розблокування</span><span class="num">${money(r0.unlock)}</span></div>
      <div class="kv"><span class="muted" id="calcmin"></span><span class="num" id="calcsum"></span></div>
      <div class="kv tot"><span>Разом зараз</span><span class="num" id="calctot"></span></div>
      <div class="kv"><span class="muted">Залишиться після поїздки</span><span class="strong num" id="left"></span></div>
    </section>
    <p class="muted sm note">Сума спишеться з гаманця після завершення. Хвилини рахуються вгору, мінімум одна.</p>
  </div>`;
  const $ = (id) => root.querySelector(`#${id}`);

  let bannerLevel = null;
  function tick() {
    const r = store.get().activeRide;
    if (!r) return;
    const elapsed = (Date.now() - Date.parse(r.startedAt)) * r.timeFactor;
    const run = api.runningOf(r);
    const left = store.get().wallet.balance - run.price;
    $('timer').textContent = clock(elapsed);
    $('cost').textContent = money(run.price);
    $('calcmin').textContent = `${run.minutes} ${minWord(run.minutes)} за ${money(r0.perMin)}`;
    $('calcsum').textContent = money(run.minutes * r0.perMin);
    $('calctot').textContent = money(run.price);
    $('bal').textContent = money(store.get().wallet.balance);
    $('left').textContent = money(left);
    $('left').classList.toggle('danger-t', left < 0);
    const level = left < 0 ? 'danger' : left <= CFG.LOW_LEFT ? 'warning' : 'none';
    if (level !== bannerLevel) {   // банер перемальовується лише при зміні стану, щоб кнопка й фокус не зникали
      bannerLevel = level;
      $('lowbox').innerHTML = level === 'none' ? '' : `<div class="banner ${level}" role="alert">${icon('alert-triangle')}<div><div class="strong">${level === 'danger' ? 'Баланс вичерпано' : 'Залишилося 20 грн або менше'}</div><div class="sm">Поїздка триває. Недостача стане боргом, який спишуть під час наступного поповнення.</div><button type="button" class="btn ghost small" id="lowtop">Поповнити</button></div></div>`;
    }
  }

  async function finish() {
    const r = store.get().activeRide;
    if (!r) return;
    const run = api.runningOf(r);
    const choice = await tg.popup({
      title: 'Завершити поїздку?',
      message: `${run.minutes} ${minWord(run.minutes)}, до сплати ${money(run.price)}. Суму буде списано з гаманця.`,
      buttons: [{ id: 'fin', type: 'destructive', text: 'Завершити' }, { id: 'cancel', type: 'cancel', text: 'Продовжити поїздку' }],
    });
    if (choice !== 'fin') return;
    tg.main.busy(true);
    try {
      const here = geo.current();
      const ride = await api.finishRide(here && here.source !== 'default' ? here : null);
      tg.haptic.ok();
      ctx.replace(`#receipt/${ride.id}`);
    } catch (e) { tg.main.busy(false); tg.haptic.err(); ctx.toast(e.message); }
  }

  root.addEventListener('click', async (e) => { if (e.target.closest('#lowtop')) { await openTopUp({ amount: CFG.TOPUPS[0] }); tick(); } });
  const timer = setInterval(tick, 1000);
  const off = store.subscribe(tick);
  tick();
  tg.back.set(null);
  tg.main.set({ text: 'Завершити поїздку', active: true, visible: true, onClick: finish });
  geo.watch();
  return () => { clearInterval(timer); off(); geo.unwatch(); };
}
