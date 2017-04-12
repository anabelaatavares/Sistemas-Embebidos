float flutuante = 2.3, produto = 0;
int inteiro = 23, i = 0;
char sincro = ';';
long tempoAtual, tempoInicio, tempoFim, tempoTotal;
bool podeEnviar = true;
byte mByte;
byte bytesRecebidos[20];
byte inicio = 32;

void setup() {
  Serial.begin(115200);
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
