// ПЗ №4. RGB-світлодіод і чотири кнопки
const int PIN_R = 11;   // канали на ШІМ-пінах
const int PIN_B = 10;
const int PIN_G = 9;
const int BTN[4] = {8, 7, 6, 5};   // натиснуто = LOW

// червоний, зелений, синій для кожної кнопки (0...255)
const int COLOR[4][3] = {
  {255, 255, 0},    // жовтий
  {0, 255, 255},    // блакитний
  {255, 0, 255},    // пурпуровий
  {255, 90, 0}      // помаранчевий
};
const char *NAME[4] = {"Yellow", "Cyan", "Magenta", "Orange"};

void setup() {
  pinMode(PIN_R, OUTPUT);
  pinMode(PIN_G, OUTPUT);
  pinMode(PIN_B, OUTPUT);
  for (int i = 0; i < 4; i++) {
    pinMode(BTN[i], INPUT_PULLUP);
  }
  Serial.begin(9600);
}

void loop() {
  for (int i = 0; i < 4; i++) {
    if (digitalRead(BTN[i]) == LOW) {
      analogWrite(PIN_R, COLOR[i][0]);
      analogWrite(PIN_G, COLOR[i][1]);
      analogWrite(PIN_B, COLOR[i][2]);
      Serial.println(NAME[i]);
      // чекаємо відпускання кнопки
      while (digitalRead(BTN[i]) == LOW) {}
    }
  }
}
