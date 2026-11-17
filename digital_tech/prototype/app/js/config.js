/**
 * Єдине місце з назвами операторів, типами транспорту, тарифами, кількостями й константами.
 * Гроші скрізь у копійках (цілі числа). Дані операторів імітовані.
 */

/** Оператори: назва, колір (змінна CSS), типи транспорту з тарифами й кількістю в автопарку. */
export const OPERATORS = [
  { code: 'bolt', name: 'Bolt', colorVar: '--op-a', types: {
    E_SCOOTER: { unlock: 1000, perMin: 520, count: 10 },
    E_BIKE: { unlock: 1000, perMin: 400, count: 4 } } },
  { code: 'jet', name: 'Jet', colorVar: '--op-b', types: {
    E_SCOOTER: { unlock: 1000, perMin: 525, count: 12 } } },
  { code: 'zelektra', name: 'Zelektra', colorVar: '--op-c', types: {
    E_SCOOTER: { unlock: 1000, perMin: 535, count: 9 },
    E_BIKE: { unlock: 1000, perMin: 420, count: 3 } } },
];

/** Типи транспорту: форма маркера й піктограма (колір задає оператор). */
export const TYPES = {
  E_SCOOTER: { name: 'Електросамокат', shape: 'circle', icon: 'scooter-electric' },
  E_BIKE: { name: 'Електровелосипед', shape: 'square', icon: 'bike' },
  SCOOTER: { name: 'Скутер', shape: 'hexagon', icon: 'scooter-electric' }, // у коді підтримується, не використовується
};

export const CFG = {
  BONUS: 20000,            // стартовий бонус, коп
  MIN_BALANCE: 10000,      // мінімум для розблокування, коп
  LOW_LEFT: 2000,          // попередження, коли лишається не більше, коп
  COMMISSION: 0.10,        // комісію сплачує оператор, користувач її не бачить
  TOPUPS: [10000, 20000, 50000],
  CARD: '**** 4242',
  FLEET_TOTAL: 38,
  FLEET_RESERVED: 3,       // позначені як is_reserved, на карті не малюються
  RESERVE_POINTS: 12,
  RADIUS_M: 800,
  NEAR_M: 250,
  NEAR_COUNT: 4,
  MIN_SPACING_M: 25,
  REGEN_M: 600,            // автопарк переформовується, якщо користувач далі від центру
  CELL_DEG: 0.005,
  WALK_FACTOR: 1.3,
  WALK_M_PER_MIN: 80,
  BATTERY_REFUSE: 10,      // менше цього відсотка оператор відмовляє
  BATTERY_WARN: 20,
  UNLOCK_MS: 800,
  IDENTITY_MS: 1500,
  GATEWAY_MS: 1200,
  EVENTS_CAP: 300,
  DEFAULT_POS: { lat: 50.4286, lng: 30.4765 }, // ДУІКТ
  DEFAULT_NAME: 'ДУІКТ',
  PROD_ORIGIN: 'https://emobility.pages.dev',
  TIME_ZONE: 'Europe/Kyiv',
  MAP_ZOOM: 16,
  RECEIPT_PREFIX: 'EM',
};

/** Плитки OpenStreetMap: адреса, перевірка точок (zoom 17), атрибуція. */
export const TILES = {
  URL: 'https://tile.openstreetmap.org/{z}/{x}/{y}.png',
  CHECK_ZOOM: 17,
  CHECK_MAX_TILES: 64,
  CHECK_CONCURRENCY: 8,
  CHECK_TIMEOUT_MS: 6000,
  CHECK_BUDGET_MS: 12000,
  CHECK_SAMPLE_PX: 3,
  TRIES_PER_POINT: 24,
  ATTRIBUTION: '<a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noopener">© OpenStreetMap contributors</a>',
};

export const DISCLAIMER = 'Навчальний проєкт. Дані операторів імітовані, проєкт не пов\'язаний з Bolt, Jet і Zelektra.';
export const FIX_MAP_URL = 'https://www.openstreetmap.org/fixthemap';

/** Імена подій журналу. */
export const EVENTS = ['CONSENT', 'ID_VERIFIED', 'BONUS', 'TOPUP', 'UNLOCK', 'UNLOCK_REFUSED', 'LOW_BALANCE', 'RIDE_FINISH', 'CHARGE', 'FLEET_GENERATED', 'RESET'];

export const opByCode = (code) => OPERATORS.find((o) => o.code === code);
