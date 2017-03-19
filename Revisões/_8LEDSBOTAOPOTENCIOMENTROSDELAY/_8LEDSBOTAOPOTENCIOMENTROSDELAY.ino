int led = 2;
int botao = 13;
int estadoBotao;
long intervalo;
long tempo0 = 0;
unsigned long conta = 0;
int xpto = 0;
long previousMillis2 = 0;

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

  if (estadoBotao != digitalRead(botao)) {
    tempo0 = 0;
    estadoBotao = !estadoBotao;
  }

  if (tempoa - tempo0 > intervalo) {
    tempo0 = tempoa;
    digitalWrite(led, LOW);

    if (estadoBotao == HIGH) {
      if (led == 2) {
        led = 9;
      } else {
        led --;
      }
    } else {
      if (led == 9) {
        led = 2;
      } else {
        led ++;
      }
    }
     digitalWrite(led, HIGH);   
  }
 

  conta ++;
  if (tempoa - previousMillis2 >= 1000) {
    previousMillis2 = tempoa; //Guardar Tempo atual na variavel
    Serial.println(conta);
    conta = 0;
  }
}

