//Acender e apagar 1 LED com frequência = 1Hz, f = 1/período <=> 1 = 1 / 1, portanto o período é 1s, ou 1000ms

// definir LED a controlar
int led = 13;
int periodo = 1000;
int intervalo = periodo / 2;

// no início
void setup() {
  // definir modo do pino como output
  pinMode(led, OUTPUT);
}

// repetidamente
void loop() {
  digitalWrite(led, HIGH);   // ligar LED
  delay(intervalo);               // esperar intervalo
  digitalWrite(led, LOW);    // desligar LED
  delay(intervalo);               // esperar intervalo
}