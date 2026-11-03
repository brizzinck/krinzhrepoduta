// ЛР №1. Гірлянда: 6 світлодіодів вмикаються по черзі
const int LED_PINS[] = {7, 6, 5, 4, 3, 2};  // зліва направо
const int LED_COUNT = 6;

const unsigned long T_ON  = 500;  // мс: світлодіод горить
const unsigned long T_GAP = 100;  // мс: пауза між ними

void setup() {
  for (int i = 0; i < LED_COUNT; i++) {
    pinMode(LED_PINS[i], OUTPUT);
  }
}

void loop() {
  for (int i = 0; i < LED_COUNT; i++) {
    digitalWrite(LED_PINS[i], HIGH);  // вмикаємо i-й
    delay(T_ON);
    digitalWrite(LED_PINS[i], LOW);   // вимикаємо
    delay(T_GAP);
  }
  // після шостого loop() починає знову з першого
}
