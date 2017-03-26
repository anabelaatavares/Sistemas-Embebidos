float flutuante = 2.3, produto = 0;
int inteiro = 23, i = 0;
char sincro = ';';
long tempoAtual, tempoInicio, tempoFim, tempoTotal;
bool podeEnviar;
byte mByte;
byte bytesRecebidos[20];

void setup() {
  Serial.begin(9600);
}

void loop() {
  tempoAtual = millis();

  if (tempoAtual - tempoInicio > 100 && podeEnviar) {
    tempoInicio = millis();

    Serial.print(flutuante);
    Serial.print(sincro);
    Serial.print(inteiro);
    Serial.print(sincro);
    Serial.print(produto);
    Serial.print(sincro);
    Serial.print(tempoTotal);
    podeEnviar = false;
  }

  if (Serial.available() > 0) {
    mByte = Serial.read();

    if (mByte != '\n') {
      bytesRecebidos[i] = mByte;
      i++;
    } else {
      bytesRecebidos[i] = 0;
      produto = atof((char*)bytesRecebidos);
      i = 0;

      tempoFim = millis();
      tempoTotal = tempoFim - tempoInicio;
      podeEnviar = true;
    }

}
