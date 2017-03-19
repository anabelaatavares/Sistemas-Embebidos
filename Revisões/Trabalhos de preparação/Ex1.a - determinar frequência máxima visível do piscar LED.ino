//Frequência máxima a que pode acender e apagar o led de forma a que consiga ver a sua cintilação

// definir LED a controlar
int led = 13;
int periodo = 100;
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

//se periodo = 0.1s, f = 1/periodo, f = 10 Hz