// ПЗ №6. Сервопривід, три світлодіоди і п'єзо
#include <Servo.h>

const int SERVO_PIN = 7;
const int LED_RED = 6;
const int LED_BLUE = 5;
const int LED_GREEN = 4;
const int BUZ = 2;
const int HOLD_MS = 2000;   // стоїть нерухомо в положенні
const int BEEP_HZ = 1000;   // частота короткого сигналу
const int BEEP_MS = 150;    // тривалість сигналу
const int MS_PER_DEG = 10;  // мс на градус, із запасом

// Положення: кут, світлодіод і його назва для Serial
const int ANGLES[5] = {0, 45, 90, 135, 180};
const int LEDS[5] = {LED_RED, LED_BLUE, LED_GREEN,
                     LED_BLUE, LED_RED};
const char* NAMES[5] = {"red", "blue", "green", "blue", "red"};

Servo servo;
int cycle = 0;
int prevAngle = 90;         // після attach ріг у середині

// Світиться лише вказаний світлодіод, два інші вимкнені
void showLed(int pin) {
  digitalWrite(LED_RED, pin == LED_RED);
  digitalWrite(LED_BLUE, pin == LED_BLUE);
  digitalWrite(LED_GREEN, pin == LED_GREEN);
}

void setup() {
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_BLUE, OUTPUT);
  pinMode(LED_GREEN, OUTPUT);
  pinMode(BUZ, OUTPUT);
  servo.attach(SERVO_PIN);
  Serial.begin(9600);
}

void loop() {
  cycle++;
  Serial.print("Cycle ");
  Serial.println(cycle);
  for (int i = 0; i < 5; i++) {
    servo.write(ANGLES[i]);   // поворот у нове положення
    showLed(LEDS[i]);         // світлодіод цього положення
    Serial.print("Angle ");
    Serial.print(ANGLES[i]);
    Serial.print(" LED ");
    Serial.println(NAMES[i]);
    int travel = abs(ANGLES[i] - prevAngle) * MS_PER_DEG;
    prevAngle = ANGLES[i];
    tone(BUZ, BEEP_HZ);       // короткий сигнал
    delay(BEEP_MS);
    noTone(BUZ);
    // ріг доходить до кута (не менше, ніж триває сигнал)
    delay(max(travel, BEEP_MS) - BEEP_MS);
    delay(HOLD_MS);           // потім 2 с нерухомо
  }
}
