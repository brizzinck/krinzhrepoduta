/**
 * Автопарк у вигляді каналів у підмножині GBFS v3 (по одному на оператора):
 * vehicle_status і system_pricing_plans. Тільки ці канали читає api.js, тож
 * заміна на справжні канали операторів у MVP не зачепить екрани.
 */
import { OPERATORS } from './config.js';

const planId = (op, type) => `${op}-${type.toLowerCase()}`;

/** Канали одного оператора. */
export function feedFor(op, fleet) {
  const o = OPERATORS.find((x) => x.code === op);
  const plans = Object.entries(o.types).map(([type, t]) => ({
    plan_id: planId(op, type),
    currency: 'UAH',
    price: t.unlock / 100,
    per_min_pricing: [{ start: 0, rate: t.perMin / 100, interval: 1 }],
  }));
  const vehicles = fleet.vehicles.filter((v) => v.op === op && v.status !== 'IN_RIDE').map((v) => ({
    vehicle_id: v.id,
    lat: v.lat,
    lon: v.lng,
    vehicle_type_id: v.type,
    is_reserved: v.status === 'RESERVED',
    is_disabled: false,
    current_fuel_percent: v.battery / 100,
    pricing_plan_id: planId(op, v.type),
  }));
  return { system_pricing_plans: { plans }, vehicle_status: { vehicles } };
}

/** Канали всіх операторів. */
export const feeds = (fleet) => OPERATORS.map((o) => ({ op: o.code, ...feedFor(o.code, fleet) }));
