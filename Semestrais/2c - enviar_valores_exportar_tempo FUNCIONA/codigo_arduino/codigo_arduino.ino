float flutuante = 2.3, produto = 0.0;
int inteiro = 23, i = 0;
char sincro = ';';
long tempoAtual, tempoInicio, tempoFim, tempoTotal = 0;
bool podeEnviar = true;
byte mByte;
byte bytesRecebidos[20];
byte inicio = 32;

void setup() {
  Serial.begin(115200);
}

void loop() {
  tempoAtual = millis();

  if (tempoAtual - tempoInicio > 100 && podeEnviar) {
    tempoInicio = millis();

    Serial.write(inicio);
    Serial.write((char*) &flutuante, 4);
    Serial.write((char*) &inteiro, 2);
    Serial.write((char*) &produto, 4);
    Serial.write((char*) &tempoTotal, 4);
    podeEnviar = false;
    

  }

  if (Serial.available() > 0) {
    mByte = Serial.read();

    if (mByte != 32) {
      bytesRecebidos[i] = mByte;
      i++;
    } else {
      //produto = atof((char*)(bytesRecebidos));
      produto = *((float*)(bytesRecebidos));
      bytesRecebidos[i] = 0;
      i = 0;

      tempoFim = millis();
      tempoTotal = tempoFim - tempoInicio;
      podeEnviar = true;
    }
  }
}
