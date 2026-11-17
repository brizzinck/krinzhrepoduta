// ЛР №5. Потенціометр, RGB-світлодіод і піезо
const int POT = A0;
const int PIN_R = 10;
const int PIN_G = 6;
const int PIN_B = 9;
const int BUZ = 4;

const int HUE_MIN = 100;  // цільовий діапазон відтінку,
const int HUE_MAX = 140;  // градуси (зелений)
const int TONE_HZ = 1000;

bool wasInside = false;   // стан у минулому циклі
int lastVal = -1;

// Відтінок 0...359 у канали 0...255 (S = V = 1)
void hueToRgb(int hue, int &r, int &g, int &b) {
  int rise = (hue % 60) * 255 / 60;  // канал, що зростає
  int fall = 255 - rise;             // канал, що спадає
  switch (hue / 60) {                // сектор по 60 градусів
    case 0: r = 255;  g = rise; b = 0;    break;
    case 1: r = fall; g = 255;  b = 0;    break;
    case 2: r = 0;    g = 255;  b = rise; break;
    case 3: r = 0;    g = fall; b = 255;  break;
    case 4: r = rise; g = 0;    b = 255;  break;
    default: r = 255; g = 0;    b = fall; break;
  }
}

void setup() {
  pinMode(PIN_R, OUTPUT);
  pinMode(PIN_G, OUTPUT);
  pinMode(PIN_B, OUTPUT);
  pinMode(BUZ, OUTPUT);
  Serial.begin(9600);
}

void loop() {
  int val = analogRead(POT);            // 0...1023
  int hue = map(val, 0, 1023, 0, 300);  // 0...300 градусів
  int r, g, b;
  hueToRgb(hue, r, g, b);
  analogWrite(PIN_R, r);
  analogWrite(PIN_G, g);
  analogWrite(PIN_B, b);

  bool inside = (hue >= HUE_MIN && hue <= HUE_MAX);
  if (inside != wasInside) {   // звук лише при зміні стану
    if (inside) {
      tone(BUZ, TONE_HZ);
    } else {
      noTone(BUZ);
    }
    wasInside = inside;
  }

  if (val != lastVal) {        // рядок у Serial при зміні val
    char line[48];
    sprintf(line, "A0 = %d hue = %d R = %d G = %d B = %d %s",
            val, hue, r, g, b, inside ? "IN" : "OUT");
    Serial.println(line);
    lastVal = val;
  }
  delay(50);
}
