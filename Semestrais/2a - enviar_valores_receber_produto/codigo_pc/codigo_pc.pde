import processing.serial.*;     // import the Processing serial library
import java.nio.ByteBuffer;

Serial myPort;
String pacote, produtoEmString;
float decimal, produto;
int inteiro;
PrintWriter fileOutput;
String[] valores;
byte[] bytesTemp;
byte[] bytesRecebidos = new byte[11];
int j=0;
int estado;
static final int espera32 = 0;
static final int recebeDados = 1;
int contador;

void setup() {
  myPort = new Serial(this, Serial.list()[0], 115200);
  myPort.bufferUntil(32);
  estado = espera32;
}

void draw() {
}

void serialEvent(Serial p) {
  bytesTemp = p.readBytes();

  if (bytesTemp != null)      

    for (int i = 0; i< bytesTemp.length; i++) {

      switch(estado) {
      case espera32:
        if (bytesTemp[i] == 32) {
          estado = recebeDados;
          contador = 0;
        }
        break;
      case recebeDados:
        bytesRecebidos[contador++] = bytesTemp[i];
        if (contador == 6) {
          estado = espera32;
          int intbit = 0;
          intbit = (bytesRecebidos[3]<<24)|(bytesRecebidos[2]<<16)|(bytesRecebidos[1]<<8)|bytesRecebidos[0];
          float f = Float.intBitsToFloat(intbit);
          int inte = bytesRecebidos[4] + (bytesRecebidos[5] << 8);
          float produto = f * inte;
          produtoEmString = Float.toString(produto);
          myPort.write(produtoEmString);
        }
        break;
      }
    }
}