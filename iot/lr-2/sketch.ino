// ЛР №2. Мелодія на зумері: грає, поки утримується кнопка
const int BUTTON = 2;   // INPUT_PULLUP: натиснуто = LOW
const int BUZZER = 8;

// Ода до радості (початок): частоти, Гц, і тривалості, мс
const int MELODY[] = {330, 330, 349, 392, 392, 349, 330, 294,
                      262, 262, 294, 330, 330, 294, 294};
const int DUR[]    = {300, 300, 300, 300, 300, 300, 300, 300,
                      300, 300, 300, 300, 450, 150, 600};
const int N = 15;

bool playing = false;

bool held() { return digitalRead(BUTTON) == LOW; }

// Чекає ms; повертає false, якщо кнопку відпустили раніше
bool waitHeld(unsigned long ms) {
  unsigned long t = millis();
  while (millis() - t < ms) {
    if (!held()) return false;
    delay(5);
  }
  return true;
}

void setup() {
  pinMode(BUTTON, INPUT_PULLUP);
  pinMode(BUZZER, OUTPUT);
  Serial.begin(9600);
}

void loop() {
  if (!held()) {
    noTone(BUZZER);        // відпущено: тиша
    if (playing) {
      playing = false;
      Serial.println("STOP");
    }
    return;
  }
  if (!playing) {
    playing = true;
    Serial.println("START");
  }
  for (int i = 0; i < N; i++) {
    tone(BUZZER, MELODY[i]);
    if (!waitHeld(DUR[i])) return;  // відпустили посеред ноти
    noTone(BUZZER);
    if (!waitHeld(40)) return;      // пауза між нотами
  }
  // кнопка ще натиснута: loop() почне мелодію спочатку
}
