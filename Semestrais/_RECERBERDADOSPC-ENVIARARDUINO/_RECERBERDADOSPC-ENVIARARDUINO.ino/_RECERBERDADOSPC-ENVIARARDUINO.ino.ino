float flutuante = 2.3;
int inteiro = 23;
char sincro = ';';
int inByte = 0;
bool recebido = false;
long tempoInicio;
long tempoFim;
long tempoTotal;
long previousMillis = 0;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
//  establishContact();
}

void loop() {
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis > 100) {
    previousMillis = currentMillis;
    tempoInicio = millis();
    Serial.print(flutuante);
    Serial.print(sincro);
    Serial.print(inteiro);
    if (recebido == false) Serial.println();
    else {
      Serial.print(sincro);
      Serial.print(inByte);
      Serial.print(sincro);
      Serial.println(tempoTotal);
    }
  }
  if (Serial.available() > 0) {
    inByte = Serial.read();
    tempoFim = millis();
    tempoTotal = tempoFim - tempoInicio;
    recebido = true;
  }
}

