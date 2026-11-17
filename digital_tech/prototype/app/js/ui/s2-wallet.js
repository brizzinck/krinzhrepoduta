/** S2. Гаманець: баланс або борг, поповнення, список операцій. */
import { CFG } from '../config.js';
import * as store from '../store.js';
import * as tg from '../tg.js';
import { money, dateTime, esc } from '../format.js';
import { icon, openTopUp } from './components.js';

const TX = {
  BONUS: { label: 'Стартовий бонус', icon: 'plus' },
  TOPUP: { label: 'Поповнення', icon: 'credit-card' },
  CHARGE: { label: 'Поїздка', icon: 'scooter-electric' },
  ADJUST: { label: 'Демо-коригування', icon: 'refresh' },
};

export function mount(root, ctx) {
  let amount = CFG.TOPUPS[0];

  const render = () => {
    const w = store.get().wallet;
    const debt = w.balance < 0;
    const tx = [...w.tx].reverse();
    root.innerHTML = `<div class="screen s2">
      <h1>Гаманець</h1>
      <section class="card balance ${debt ? 'is-debt' : ''}" aria-label="Баланс">
        <div class="muted sm">${debt ? 'Заборгованість' : 'Баланс'}</div>
        <div class="amt num">${debt ? `Борг ${money(-w.balance)}` : money(w.balance)}</div>
        <p class="sm ${debt ? 'danger-t' : 'muted'}">${debt ? `${icon('alert-triangle')}<span>Нова поїздка заблокована, доки є борг. Його буде погашено першим поповненням.</span>` : `Для розблокування потрібно щонайменше ${money(CFG.MIN_BALANCE, { compact: true })}.`}</p>
      </section>
      <h2 class="sec">Поповнення</h2>
      <div class="chips" role="radiogroup" aria-label="Сума поповнення">${CFG.TOPUPS.map((a) => `<button type="button" role="radio" aria-checked="${a === amount}" class="chip${a === amount ? ' on' : ''}" data-a="${a}">${money(a, { compact: true })}</button>`).join('')}</div>
      <div class="card cardrow">${icon('credit-card')}<div><div class="strong">Картка ${CFG.CARD}</div><div class="muted sm">Імітація платіжного шлюзу</div></div></div>
      <h2 class="sec">Операції</h2>
      <ul class="list tx" aria-label="Операції">${tx.map((t) => {
        const meta = TX[t.type] || { label: t.type, icon: 'receipt' };
        const neg = t.amount < 0;
        return `<li><span class="txic ${neg ? 'neg' : 'pos'}">${icon(meta.icon)}</span>
          <div class="grow"><div class="strong">${meta.label}</div><div class="muted sm">${dateTime(t.at)}</div></div>
          <div class="right"><div class="strong num ${neg ? '' : 'pos-t'}">${money(t.amount, { sign: true })}</div><div class="muted sm num">Залишок ${money(t.balanceAfter)}</div></div></li>`;
      }).join('')}</ul>
    </div>`;
    setMain();
  };

  function setMain() {
    tg.main.set({ text: `Поповнити на ${money(amount, { compact: true })}`, active: true, visible: true, onClick: pay });
  }
  async function pay() {
    await openTopUp({ amount, auto: true });
    render();
  }

  root.addEventListener('click', (e) => {
    const c = e.target.closest('.chip');
    if (!c) return;
    amount = Number(c.dataset.a);
    tg.haptic.select();
    render();
    const n = root.querySelector(`.chip[data-a="${amount}"]`); if (n) n.focus();
  });
  const off = store.subscribe(() => { if (!document.querySelector('.modal')) render(); });
  render();
  tg.back.set(null);
  return off;
}
