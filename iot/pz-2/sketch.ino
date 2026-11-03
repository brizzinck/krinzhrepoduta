// ПЗ №2. Три кнопки і зумер: кожна кнопка грає свій сигнал
const int BTN1 = 4, BTN2 = 3, BTN3 = 2;  // натиснуто = LOW
const int BUZZER = 8;   // зумер
const int LED = 7;      // індикатор звуку

int lastBtn = 0;        // щоб друкувати лише зміну стану

bool pressed(int pin) { return digitalRead(pin) == LOW; }

void note(int freq, int ms) {
  digitalWrite(LED, HIGH);
  tone(BUZZER, freq);
  delay(ms);
  noTone(BUZZER);
  digitalWrite(LED, LOW);
  delay(50);
}

void setup() {
  pinMode(BTN1, INPUT_PULLUP);
  pinMode(BTN2, INPUT_PULLUP);
  pinMode(BTN3, INPUT_PULLUP);
  pinMode(BUZZER, OUTPUT);
  pinMode(LED, OUTPUT);
  Serial.begin(9600);
}

void loop() {
  int btn = 0;
  if (pressed(BTN1)) btn = 1;
  else if (pressed(BTN2)) btn = 2;
  else if (pressed(BTN3)) btn = 3;

  if (btn != lastBtn) {
    if (btn) {
      Serial.print("Button ");
      Serial.println(btn);
    } else {
      Serial.println("Released");
    }
    lastBtn = btn;
  }

  if (btn == 1) {         // три ноти вгору
    note(262, 200); note(330, 200); note(392, 200);
  } else if (btn == 2) {  // сирена, два тони
    note(880, 150); note(440, 150);
    note(880, 150); note(440, 150);
  } else if (btn == 3) {  // три короткі високі сигнали
    note(1000, 100); note(1000, 100); note(1000, 100);
    delay(300);
  }
}
