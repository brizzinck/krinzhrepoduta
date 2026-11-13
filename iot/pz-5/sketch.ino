// ПЗ №5. Потенціометр керує п'ятьма світлодіодами
const int POT = A0;
const int LED[5] = {9, 8, 7, 6, 5};
const int STEP = 171;   // 6 * 171 = 1026: рівень 0...5

void setup() {
  for (int i = 0; i < 5; i++) {
    pinMode(LED[i], OUTPUT);
  }
  Serial.begin(9600);
}

void loop() {
  int val = analogRead(POT);    // 0...1023
  int level = val / STEP;       // скільки світлодіодів горить

  for (int i = 0; i < 5; i++) {
    digitalWrite(LED[i], i < level ? HIGH : LOW);
  }

  Serial.print("A0 = ");
  Serial.print(val);
  Serial.print("  level = ");
  Serial.println(level);
  delay(100);
}
