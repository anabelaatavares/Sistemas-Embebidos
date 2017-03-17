// definir LED a controlar
int periodo = 1000;
int intervalo = periodo / 2;

// no início
void setup() {
  // definir modo dos pinos 5 a 13 como output
  for(int led = 5; led < 14; led++) {   
  	pinMode(led, OUTPUT);
  }
}

// repetidamente
void loop() {
  
  for(int led = 5; led < 14; led++) {   
    digitalWrite(led, HIGH);   // ligar LED
    delay(intervalo);          // esperar intervalo
  	digitalWrite(led, LOW);    // desligar LED  	             
  }
  
}