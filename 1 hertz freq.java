int ledPin = 10; // sets pin 10 to output the same value at the ledPin output
int blinkPin = 9; // set pin 9 to output the same value as the output timer which is the blinkPin

void TimerOne_setPeriod(long OCRValue)

{
 TCCR1B = _BV(WGM12)|_BV(CS12)|_BV(CS10);   // CTC mode - 1024 prescale
                                            //  TCCR1B = _BV(WGM12)|_BV(CS12)|_BV(CS11)|_BV(CS10); is the entire command but since CS11=0 its left out
                                            //  manipulates prescale (one of the parameters we can manipulate**we want to use prescales that yeields high ORC values for more reliable signals
 TCCR1A = _BV(COM1A0);                      //  or use TCCR1A = 0x40;            // Toggle mode 0C1A  
                                            //  This is used to toggle on pin 11 ** pin 11 = 0 x 1 so COM(COM1A1) = 0 and is not in code
                                            //  **Determines pin behavor
 OCR1A = OCRValue;                          //  set the counter
}

void setup()
{
 pinMode(ledPin, OUTPUT);                   //  Makes ledPin is the output
 pinMode(blinkPin, OUTPUT);                  // turn on the timer ouput pin and it also the input;
 TimerOne_setPeriod(15625);                 // set up and start Timer1 to blink at the same rate as the blink sketch
                                           /* Insert the follwing values into the TimerOne_setPeriod() here for the following frequencies:
                                            .12 Hz: 65104.16667
                                            .5 Hz: 15625
                                            1 Hz: 7812.5
                                            5 Hz: 1562.5
                                            10 Hz: 781.25
                                            */
                                           
                                       
}
                                         
                                           

void loop()
{
 digitalWrite(ledPin, LOW);                 // set the LED on
 delay(1000);                               // wait for a second
 digitalWrite(ledPin, HIGH);                // set the LED off
 delay(1000);                               // wait for a second
}