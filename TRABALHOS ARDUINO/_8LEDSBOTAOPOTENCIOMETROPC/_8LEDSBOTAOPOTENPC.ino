int led = 2;
int botao = 13;
int estadoBotao;
long intervalo;
long tempo0 = 0;
int xpto = 0;
long previousMillis2 = 0;
byte byteRead;
int tempoFi = 0;
int tempoFinal = 0;
int tempoInicial = 0;

unsigned long currentMillis1 = millis();
void setup() {
  for (int i = 2; i <= 9; i++) {
    pinMode(i, OUTPUT);
  }
  pinMode(botao, INPUT);
  Serial.begin(9600);
}


void loop() {
  unsigned long tempoa = millis();
  long atraso = analogRead(A0);
  intervalo = (atraso * 2950 / 1023) + 50;


  if (Serial.available()) {
    tempo0 = 0;
    byteRead = Serial.read();
  }

  if (tempoa - tempo0 > intervalo) {
    tempo0 = tempoa;
    digitalWrite(led, LOW);
    tempoFinal = millis();
    tempoFi = tempoFinal - tempoInicial;
    Serial.println(tempoFi);
    tempoFinal = 0;
    //Serial.println(tempoFinal);
 
    if (byteRead == 'c') {
      if (led == 2) {
        led = 9;        
      } else {
        led --;
      }
    } else if (byteRead == 'b') {
      if (led == 9) {
        led = 2;
      } else {
        led ++;
      } 
    }

    tempoInicial = 0;
    tempoInicial = millis();
    digitalWrite(led, HIGH);
  }

}

