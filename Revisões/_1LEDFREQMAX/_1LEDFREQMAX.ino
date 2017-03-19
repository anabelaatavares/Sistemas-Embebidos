int led = 12;

unsigned long currentMillis1 = millis(); 
// the setup routine runs once when you press reset:
void setup() {
  // initialize the digital pin as an output.
  pinMode(led, OUTPUT);
}

// the loop routine runs over and over again forever:
void loop() {

  digitalWrite(led, HIGH);   // turn the LED on (HIGH is the voltage level)
  delay(15);               // wait for a second
  digitalWrite(led, LOW); 
  delay(15);// wait for a second
}
