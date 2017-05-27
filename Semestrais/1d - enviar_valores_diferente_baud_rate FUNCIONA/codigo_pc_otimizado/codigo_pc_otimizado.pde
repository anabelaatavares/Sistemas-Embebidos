import processing.serial.*;     // import the Processing serial library
import java.nio.ByteBuffer;

Serial myPort;
String pacote, produtoEmString;
float decimal, produto;
int inteiro;
PrintWriter fileOutput;
String[] valores;
byte[] bytesTemp;
byte[] bytesRecebidos = new byte[20];
int j=0;

void setup() {
  myPort = new Serial(this, Serial.list()[0], 115200);
  myPort.bufferUntil('\n');
  fileOutput = createWriter("C:\\fileoutput\\tempos.csv");
  fileOutput.println("baud rate 115200");
}

void draw() {
  
}

void serialEvent(Serial p) { 

  bytesTemp = p.readBytes();

  if (bytesTemp != null)  

    for (int i = 0; i< bytesTemp.length; i++) {

      if (bytesTemp[i] == '\n') {               

        pacote = new String(bytesRecebidos);         
        valores = split(pacote, ';');         
        decimal = Float.parseFloat(valores[0]);        
        inteiro = int(valores[1]);
        produto = decimal * inteiro;
        produtoEmString =  Float.toString(produto);        
        myPort.write(produtoEmString);        
        myPort.write('\n');
		
        if(valores[3] != null) {
          fileOutput.println(valores[3]);
          fileOutput.flush();
        }
                
        j = 0;                
        bytesRecebidos[j] = 0;
        
		println(valores);        
        
      } else {        
        bytesRecebidos[j] = bytesTemp[i];
        j++;                       
      }
    }
} 