// ЛР №6. Два шлагбауми: сервоприводи, кнопки, світлодіоди
#include <Servo.h>

const int SERVO_PIN[2] = {9, 10};   // шлагбаум 1 і 2
const int BTN_DOWN[2] = {5, 3};     // кнопки 1 і 3: опустити
const int BTN_UP[2] = {4, 2};       // кнопки 2 і 4: підняти
const int LED_GREEN = 7;            // обидва підняті
const int LED_RED = 6;              // обидва опущені
const int UP = 90;                  // кут піднятого шлагбаума
const int DOWN = 0;                 // кут опущеного
const int STEP_MS = 20;             // пауза між кроками, мс

Servo gate[2];
int pos[2] = {UP, UP};              // поточний кут кожного
int ledState = -1;                  // що показано, для Serial

// Зелений горить лише коли обидва підняті, червоний лише коли
// обидва опущені, в усіх інших станах обидва вимкнені
void showLeds() {
  bool allUp = pos[0] == UP && pos[1] == UP;
  bool allDown = pos[0] == DOWN && pos[1] == DOWN;
  digitalWrite(LED_GREEN, allUp);
  digitalWrite(LED_RED, allDown);
  int state = allUp ? 1 : (allDown ? 2 : 0);   // 0 вимкнені
  if (state != ledState) {          // у Serial лише зміни
    ledState = state;
    Serial.println(state == 1 ? "LED green"
                   : (state == 2 ? "LED red" : "LED off"));
  }
}

// Плавно рухає шлагбаум i до кута target, по 1 градусу
void moveGate(int i, int target) {
  if (pos[i] == target) return;     // уже там
  Serial.print("Gate ");
  Serial.print(i + 1);
  Serial.println(target == UP ? " up" : " down");
  int step = (target > pos[i]) ? 1 : -1;
  int steps = abs(target - pos[i]);
  for (int k = 0; k < steps; k++) {
    pos[i] += step;
    gate[i].write(pos[i]);
    showLeds();                     // у русі обидва вимкнені
    delay(STEP_MS);
  }
  Serial.print("Gate ");
  Serial.print(i + 1);
  Serial.print(" at ");
  Serial.println(pos[i]);
}

void setup() {
  for (int i = 0; i < 2; i++) {
    pinMode(BTN_DOWN[i], INPUT_PULLUP);
    pinMode(BTN_UP[i], INPUT_PULLUP);
    gate[i].attach(SERVO_PIN[i]);
    gate[i].write(UP);              // старт: обидва підняті
  }
  pinMode(LED_GREEN, OUTPUT);
  pinMode(LED_RED, OUTPUT);
  Serial.begin(9600);
  delay(700);                       // дати рогам дійти до 90
  showLeds();                       // потім зелений
}

void loop() {
  for (int i = 0; i < 2; i++) {
    if (digitalRead(BTN_DOWN[i]) == LOW) moveGate(i, DOWN);
    else if (digitalRead(BTN_UP[i]) == LOW) moveGate(i, UP);
  }
}
