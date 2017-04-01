import processing.serial.*;     // import the Processing serial library
import java.nio.ByteBuffer;

Serial myPort;
String pacote;
String[] valores;
float decimal;
int inteiro;

void setup() {
  myPort = new Serial(this, Serial.list()[0], 9600);
  myPort.bufferUntil('\n');
}

void draw() {

  pacote = myPort.readStringUntil('\n');
  pacote = trim(pacote);

  if (pacote != null) {
    valores = split(pacote, ';');
    decimal = Float.parseFloat(valores[0]);
    inteiro = int(valores[1]);

    println(pacote);

  }
}