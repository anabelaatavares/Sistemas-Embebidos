import processing.serial.*;     // import the Processing serial library
import java.nio.ByteBuffer;

Serial myPort;
String pacote, produtoEmString;
float decimal, produto;
int inteiro;
PrintWriter fileOutput;
String[] valores;

void setup() {
  myPort = new Serial(this, Serial.list()[0], 115200);
  myPort.bufferUntil('\n');
  fileOutput = createWriter("C:\\fileoutput\\temposv.csv");
  fileOutput.println("baud rate 115200");
}

void draw() {

  pacote = myPort.readStringUntil('\n');
  pacote = trim(pacote);

  if (pacote != null) {
    valores = split(pacote, ';');
    decimal = Float.parseFloat(valores[0]);
    inteiro = int(valores[1]);
    produto = decimal * inteiro;
    produtoEmString =  Float.toString(produto);
    myPort.write(produtoEmString);
    myPort.write('\n');
    fileOutput.println(valores[3]);
    fileOutput.flush();

    println(pacote);

  }

}