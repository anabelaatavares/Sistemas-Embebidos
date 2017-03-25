float flutuante = 6.9;
int inteiro = 69;
char sincro = ';';
boolean recebido = false;
float produto;
byte produtoEmBytes[10];

long previousMillis = 0;

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis > 100) {
    previousMillis = currentMillis;
      Serial.print(flutuante);
      Serial.print(sincro);
      Serial.print(inteiro);
      if (!recebido) Serial.println();
      else {
        Serial.print(sincro);
        Serial.println(produto);
      }
  }

  if (Serial.available() > 0) {    
    produtoEmBytes[0] = Serial.read();
    produtoEmBytes[1] = Serial.read();
    produtoEmBytes[2] = Serial.read();
    produtoEmBytes[3] = Serial.read();         
    produto = *((float*)(produtoEmBytes));   
    recebido = true;
  }
}
