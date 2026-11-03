// ЛР №3. Індикатор затемнення: 6 світлодіодів накопичувально
const int LDR_PIN = A0;
// червоний, червоний, жовтий, жовтий, зелений, зелений
const int LED_PINS[6] = {7, 6, 5, 4, 3, 2};

// Пороги analogRead, підібрані за калібруванням у симуляторі:
// менше значення означає більше затемнення
const int THR[6] = {950, 900, 800, 700, 500, 200};

void setup() {
  for (int i = 0; i < 6; i++) {
    pinMode(LED_PINS[i], OUTPUT);
  }
  Serial.begin(9600);
}

void loop() {
  int v = analogRead(LDR_PIN);

  // level: скільки порогів значення вже перетнуло
  int level = 0;
  for (int i = 0; i < 6; i++) {
    if (v < THR[i]) level = i + 1;
  }

  // перші level світлодіодів горять, решта вимкнені
  for (int i = 0; i < 6; i++) {
    digitalWrite(LED_PINS[i], i < level ? HIGH : LOW);
  }

  Serial.print("A0 = ");
  Serial.print(v);
  Serial.print("  level = ");
  Serial.println(level);
  delay(200);
}
