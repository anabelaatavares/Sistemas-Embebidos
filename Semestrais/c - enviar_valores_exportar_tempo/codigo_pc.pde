import processing.serial.*;     // import the Processing serial library
import java.nio.ByteBuffer;

Serial myPort;
String pacote, valores, produtoEmString;
float decimal, produto;
int inteiro;
PrintWriter fileOutput;

void setup() {
  myPort = new Serial(this, Serial.list()[0], 9600);
  myPort.bufferUntil('\n');
  fileOutput = createWriter("C:\\fileoutput\\tempos.csv");
  fileOutput.println("baud rate 9600");
}

void draw() {

  pacote = myPort.readStringUntil('\n');
  pacote = trim(myString);

  if (pacote != null) {
    valores = split(pacote, ';');
    decimal = Float.parseFloat(valores[0]);
    inteiro = int(valores[1]);
    produto = decimal * inteiro;
    produtoEmString =  Float.toString(produto);
    myPort.write(produtoEmString);
    myPort.write('\n');
    output.println(valores[3]);
    output.flush();

    println(pacote);

  }

}
