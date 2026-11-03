// ПЗ №3. Вуличне освітлення зі сповіщенням про сутінки
const int LDR_PIN = A0;     // середня точка дільника
const int RED     = 10;     // денний режим
const int GREEN   = 9;      // нічний режим
const int BUZZER  = 8;
const int THRESHOLD = 500;  // нижче цього значення ніч

bool night = false;

void dayMode() {
  digitalWrite(RED, HIGH);
  digitalWrite(GREEN, LOW);
  noTone(BUZZER);
}

void nightMode() {
  digitalWrite(RED, LOW);
  digitalWrite(GREEN, HIGH);
  for (int i = 0; i < 3; i++) {  // три сигнали один раз
    tone(BUZZER, 1000);
    delay(200);
    noTone(BUZZER);
    delay(100);
  }
}

void setup() {
  pinMode(RED, OUTPUT);
  pinMode(GREEN, OUTPUT);
  pinMode(BUZZER, OUTPUT);
  Serial.begin(9600);
  dayMode();
}

void loop() {
  int light = analogRead(LDR_PIN);
  bool nowNight = light < THRESHOLD;

  if (nowNight && !night) {        // настали сутінки
    night = true;
    nightMode();
  } else if (!nowNight && night) { // знову день
    night = false;
    dayMode();
  }

  Serial.print("Svitlo = ");
  Serial.println(light);
  delay(500);
}
