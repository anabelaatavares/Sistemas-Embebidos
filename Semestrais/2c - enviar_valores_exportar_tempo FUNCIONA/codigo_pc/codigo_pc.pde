import processing.serial.*;     // import the Processing serial library
import java.nio.ByteBuffer;
import java.text.DecimalFormat;  

Serial myPort;
String pacote, produtoEmString;
float decimal, produto, ProdutoRecebido;
int intbit = 0, inFloat = 0, inTempo = 0;
PrintWriter fileOutput;
String[] valores;
byte[] bytesTemp;
byte[] bytesRecebidos = new byte[14];
int j=0;
int estado;
static final int espera32 = 0;
static final int recebeDados = 1;
int contador;

void setup() {
  myPort = new Serial(this, Serial.list()[0], 115200);
  myPort.bufferUntil(32);
  estado = espera32;
  fileOutput = createWriter("C:\\fileoutput\\tempos.csv");
  fileOutput.println("baud rate 115200");
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
        if (contador == 14) {          
          estado = espera32;
          intbit = (bytesRecebidos[3]<<24)|(bytesRecebidos[2]<<16)|(bytesRecebidos[1]<<8)|bytesRecebidos[0];
          decimal = Float.intBitsToFloat(intbit);
          int inte = bytesRecebidos[4] + (bytesRecebidos[5] << 8);
          produto = decimal * inte;
          produtoEmString = Float.toString(produto);
          myPort.write(float2ByteArray(produto));
          //myPort.write(produtoEmString);
          myPort.write(32);
          //Produto Recebido
          inFloat = (bytesRecebidos[6]<<24)|((bytesRecebidos[7] & 0xff)<<16)|((bytesRecebidos[8]& 0xff)<<8)|(bytesRecebidos[9]& 0xff);
          //inFloat = (bytesRecebidos[9]<<24)|((bytesRecebidos[8] & 0xff)<<16)|((bytesRecebidos[7]& 0xff)<<8)|(bytesRecebidos[6]& 0xff);
          ProdutoRecebido = Float.intBitsToFloat(inFloat);
          //Tempo
          inTempo = bytesRecebidos[10] + (bytesRecebidos[11]<<8) + (bytesRecebidos[12]<<16) + (bytesRecebidos[13]<<24);       
          //println(bytesRecebidos);
          println(decimal + " " + inte + " " + ProdutoRecebido  + " " + inTempo);
          fileOutput.println(inTempo);
          fileOutput.flush();
        }
        break;
      }
    }
}

public static byte [] float2ByteArray (float value)
{  
  return ByteBuffer.allocate(4).putFloat(value).array();
}