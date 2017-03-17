int LED = 9;
int entrada;
int intensidade = 0;
long previousMillis = 0;

void setup() {
  Serial.begin(9600);
  pinMode(LED, OUTPUT);
  Serial.println("Digite A para aumentar a intensidade. D para reduzir. R para apagar:");

}

void loop() {
  if (Serial.available() > 0) {
    entrada = Serial.read();
    unsigned long currentMillis = millis();
    if (currentMillis - previousMillis > 50) {
      previousMillis = currentMillis;
      if (entrada == 'A' || entrada == 'a') {
        intensidade += 10;
        if (intensidade > 255)
          intensidade = 255;
      }
      else if (entrada == 'D' || entrada == 'd') {
        intensidade -= 10;
        if (intensidade < 0)
          intensidade = 0;
      }
      else if (entrada == 'R' || entrada == 'r') {
        intensidade = 0;
      }
      else {
        Serial.println("Por favor, 'A', 'D' ou 'R'!");
      }
      analogWrite(LED, intensidade);
    }
  }
}
