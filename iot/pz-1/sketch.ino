// ПЗ №1. Світлофор: червоний, жовтий, зелений, жовтий
const int PIN_RED    = 8;   // червоний світлодіод
const int PIN_YELLOW = 9;   // жовтий світлодіод
const int PIN_GREEN  = 10;  // зелений світлодіод

const unsigned long T_RED    = 4000;  // мс
const unsigned long T_YELLOW = 1500;  // мс
const unsigned long T_GREEN  = 4000;  // мс

void setLights(int red, int yellow, int green) {
  digitalWrite(PIN_RED, red);
  digitalWrite(PIN_YELLOW, yellow);
  digitalWrite(PIN_GREEN, green);
}

void setup() {
  pinMode(PIN_RED, OUTPUT);
  pinMode(PIN_YELLOW, OUTPUT);
  pinMode(PIN_GREEN, OUTPUT);
}

void loop() {
  setLights(HIGH, LOW, LOW);  delay(T_RED);     // червоний
  setLights(LOW, HIGH, LOW);  delay(T_YELLOW);  // жовтий
  setLights(LOW, LOW, HIGH);  delay(T_GREEN);   // зелений
  setLights(LOW, HIGH, LOW);  delay(T_YELLOW);  // жовтий
}
