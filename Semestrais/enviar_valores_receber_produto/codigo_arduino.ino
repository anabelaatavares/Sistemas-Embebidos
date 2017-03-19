float flutuante = 6.9;
int inteiro = 69;
char sincro = ';';
byte lerValor;

long previousMillis = 0;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis > 100) {
    previousMillis = currentMillis;
      Serial.print(flutuante);
      Serial.print(sincro);
      Serial.println(inteiro);
  }

  if (Serial.available() > 0) {
    lerValor = Serial.read();
  }
}
