float flutuante = 2.3;
int inteiro = 23;
byte inicio = 32;
long tempoAtual, tempoInicio;

void setup() {
  Serial.begin(9600);
}

void loop() {
  tempoAtual = millis();

  if (tempoAtual - tempoInicio > 100) {
    tempoInicio = millis();
    Serial.write(inicio);
    Serial.write((char*) &flutuante, 4);    
    Serial.write((char*) &inteiro, 2);
  }

}
