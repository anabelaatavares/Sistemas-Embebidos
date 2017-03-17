int led = 2;
int botao = 13;
long intervalo;

void setup() {

  for (int i = 2; i <= 9; i++) {
    pinMode(i, OUTPUT);
  }
  pinMode(botao, INPUT);
}

void loop() {

  digitalWrite(led, HIGH);
  long atraso = analogRead(A0);
  intervalo = (atraso * 2950 / 1023) + 50;
  delay(intervalo);
  digitalWrite(led, LOW);

  if (digitalRead(botao) == HIGH) {
    if (led == 2) {
      led = 9;
    } else {
      led --;
    }
  }
  else {
    if (led == 9) {
      led = 2;
    } else {
      led++;
    }
  }
}

