
int analogIn; // hold pot value
byte squareOut = 2; // output pin, PORTD bit 2 on an Uno
unsigned long halfPeriod; // 32 bit data to hold frequency = halfPeriod x 2
unsigned long currentMillis; // 32 bit value for time comparison
unsigned long elapsedMillis;  // 32 bit value for time comparison
unsigned long previousMillis; // 32 bit value for time comparison

void setup(){
pinMode (squareOut, OUTPUT);
// Serial.begin(115200); // send messages to serial monitor for debugging
}

void loop(){
// read the pot, which has one outer leg wired to +5, the other to Gnd, the wiper to A0
analogIn = analogRead(A0);  // number from 0 to 1023
// 100 Hz = 10mS period, change level every 5mS, or 5,000uS
// 1Hz = 1000mS period, change level every 500mS, or 500,000uS
// everything else is in between
if (analogIn <=10){
analogIn = 5; // max speed
}
if (analogIn >=1000){
halfPeriod = 500; // min speed
}
if ( (analogIn >10) & analogIn<1000){
halfPeriod = analogIn/2; // everything else
}

// see if time to change output level
currentMillis = millis(); // capture the current 'time'
elapsedMillis = currentMillis - previousMillis; // how much 'time' has passed since last capture
if (elapsedMillis >= halfPeriod){ // enough has passed: do something
previousMillis = previousMillis + halfPeriod; // set up for next level change
PIND = 0b00000100; // flips D2 from Hi to Lo, or Lo to Hi
}
// do other stuff while waiting for next half period
} // end loop
