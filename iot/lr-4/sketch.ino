// ЛР №4. RGB-світлодіод і шість світлодіодів за кнопкою
const int RGB_R = 12;
const int RGB_B = 11;
const int RGB_G = 10;
const int GREEN_LED[2] = {9, 8};
const int BLUE_LED[2]  = {7, 6};
const int RED_LED[2]   = {5, 4};
const int BUTTON = 2;   // натиснуто = LOW

// RGB вмикається одним кольором, потім гасне,
// далі по черзі спалахують два світлодіоди цього кольору
void stage(int rgbPin, const int leds[2], const char *name) {
  Serial.println(name);
  digitalWrite(rgbPin, HIGH);
  delay(700);
  digitalWrite(rgbPin, LOW);
  delay(300);
  for (int i = 0; i < 2; i++) {
    digitalWrite(leds[i], HIGH);
    delay(700);
    digitalWrite(leds[i], LOW);
    delay(300);
  }
}

void setup() {
  pinMode(RGB_R, OUTPUT);
  pinMode(RGB_G, OUTPUT);
  pinMode(RGB_B, OUTPUT);
  for (int i = 0; i < 2; i++) {
    pinMode(GREEN_LED[i], OUTPUT);
    pinMode(BLUE_LED[i], OUTPUT);
    pinMode(RED_LED[i], OUTPUT);
  }
  pinMode(BUTTON, INPUT_PULLUP);
  Serial.begin(9600);
}

void loop() {
  if (digitalRead(BUTTON) == LOW) {
    Serial.println("Start");
    stage(RGB_G, GREEN_LED, "Green");
    stage(RGB_B, BLUE_LED, "Blue");
    stage(RGB_R, RED_LED, "Red");
    Serial.println("Done");
    // чекаємо, поки кнопку відпустять
    while (digitalRead(BUTTON) == LOW) {}
  }
}
