float flutuante = 2.3;
int inteiro = 23;
char sincro = ';';
long tempoAtual, tempoInicio;

void setup() {
  Serial.begin(9600);
}

void loop() {
  tempoAtual = millis();

  if (tempoAtual - tempoInicio > 100) {
    tempoInicio = millis();
    Serial.print(flutuante);
    Serial.print(sincro);
    Serial.println(inteiro);    
  }

}
