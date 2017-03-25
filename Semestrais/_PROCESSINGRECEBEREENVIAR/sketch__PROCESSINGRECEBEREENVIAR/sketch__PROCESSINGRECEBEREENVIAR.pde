import processing.serial.*;     // import the Processing serial library
import java.nio.ByteBuffer;

Serial myPort; 
String sensors[];
float cal;
PrintWriter output;

void setup() {
  myPort = new Serial(this, Serial.list()[0], 9600);
  myPort.bufferUntil('\n');
  output = createWriter("D:\\Users\\Anabela\\Desktop\\positions.csv");
}

void draw() {

  String myString = myPort.readStringUntil('\n');
  myString = trim(myString);
  if (myString != null) {
    sensors = split(myString, ';');
    float decimal = Float.parseFloat(sensors[0]);
    int inteiro = int(sensors[1]);
    cal = decimal * inteiro;
    String calculo =  Float.toString(cal);
    myPort.write(calculo);
  }

  if (myString != null) {
    if (sensors.length >=4) {
      println("Produto: " + sensors[2] + " Tempo Total: " + sensors[3]);
      //println(myString);
      output.println(sensors[3]);
      output.flush();
    }
  }
}
public static byte[] convertToByteArray(float value) {
  byte[] bytes = new byte[8];
  ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
  buffer.putDouble(value);
  return buffer.array();
}