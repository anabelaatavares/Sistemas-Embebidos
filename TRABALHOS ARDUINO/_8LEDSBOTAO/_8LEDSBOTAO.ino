
int led;
int botao = 13;

void setup() {

  for (int i = 2; i <= 9; i++) {
    pinMode(i, OUTPUT);
  }
  pinMode(botao, INPUT);
}

void loop() {

  digitalWrite(led, HIGH);
  delay(500);
  digitalWrite(led, LOW);

  if (digitalRead(botao) == LOW) {
    if (led < 2) {
      led = 9;
    } else {
      led --;
    }
  }
  else {
    if (led > 9) {
      led = 2;
    } else {
      led++;
    }
  }
}

