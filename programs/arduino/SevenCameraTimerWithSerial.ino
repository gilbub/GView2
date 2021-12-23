#include <Wire.h>
#include <Adafruit_MCP4725.h>

Adafruit_MCP4725 dac;

int pin11=11;
int pin8=8;
int pin9=9;
int j=0;
int voltage=0;

//Arduino script for the SPEED microscope. This either powers on and off the brightfield LED (for focusing), or sends a series of pulses that can be used to drive the 7 cameras through trigger cables. 
//Serial communication is only used to start a routine - e.g. a train of 500 pulses. Routines are therefore short as there are no interupts or serial checks during acquisition to ensure accurate timing.
//The code has a limited number of options that reflect the experiments performed on zebrafish hearts; these options can be changed for different preparations by editing this script and uploading it using 
//the arduino IDE.


//sets up pins 8,9, and 11, as well as the dac (for controlling the thorlabs motorized stage)
//the Thorlabs motorized stage takes a voltage input - note that in practice it was often controlled manually so the dac is not essential
void setup() {
  pinMode(pin11,OUTPUT);
  pinMode(pin8,OUTPUT);
  pinMode(pin9,OUTPUT);
  pinMode(LED_BUILTIN, OUTPUT);
  Serial.begin(9600);
  dac.begin(0x62); 
}

//simple test to confirm pc-arduino serial communication - send the appropriate serial command and see if the led blinks. 
void blinkonboardled(){
 for (j=0;j<5;j++){
   digitalWrite(LED_BUILTIN, HIGH);   // turn the LED on 
   delay(500);                        // wait for half a second
   digitalWrite(LED_BUILTIN, LOW);    // turn the LED off 
   delay(500);   
 }
}

//The following routines (fourpulses and fourpulses_t) generate square wave pulses with different duty cycles, which is usefull 
//if the user wants to manually switch between driving the cameras at two speeds by switching pins
void fourpulses(){
//2ms total duration 
//pin8 has a period of 1 ms, pin9 has a period of 2ms
  digitalWrite(pin8,HIGH);
  digitalWrite(pin9,HIGH);
  delayMicroseconds(500);
  digitalWrite(pin8,LOW);
  delayMicroseconds(500);
  digitalWrite(pin9,LOW);
  digitalWrite(pin8,HIGH);
  delayMicroseconds(500);
  digitalWrite(pin8,LOW);
  delayMicroseconds(500);
}

void fourpulses_t(int micro_d){
//micro_d * 4 total duration
//pin8 has a period of 2*micro_d us, pin9 has a period of 4 * micro_d us
  digitalWrite(pin8,HIGH);
  digitalWrite(pin9,HIGH);
  delayMicroseconds(micro_d);
  digitalWrite(pin8,LOW);
  delayMicroseconds(micro_d);
  digitalWrite(pin9,LOW);
  digitalWrite(pin8,HIGH);
  delayMicroseconds(micro_d);
  digitalWrite(pin8,LOW);
  delayMicroseconds(micro_d); 
}

//loop is not used - the program instead listens for commands from the computer and then runs a for loop to drive the cameras for a fixed number of iterations
void loop() {  
}


//the serial code doesn't have to be efficient as all it is doing is starting a loop that runs without interuptions for a short (10s) period of time.
void serialEvent(){
  while (Serial.available()){
    char ic=(char)Serial.read();
    //check for serial communication by blinking the led
    if (ic=='b') blinkonboardled();
    //switch the red brightfield LED 'on'
    if (ic=='l') digitalWrite(pin11,HIGH);
    //switch the red brightfield LED 'off'
    if (ic=='o') digitalWrite(pin11,LOW);
    //various modes, some used for testing purposes with different duty cycles for driving the 7 cameras in synchrony (they are all attached to pin 8 or 9 in parallel)
    if (ic=='0'){
     delay(500);
     for (j=0;j<50;j++){
      for (int i=0;i<50;i++) fourpulses();//100ms
      digitalWrite(pin11,HIGH);
      for (int i=0;i<50;i++) fourpulses();//100ms
      digitalWrite(pin11,LOW);   
      }
    }
    if (ic=='1'){
      delay(500);
       //period = 1ms
       for (j=0;j<2000;j++) fourpulses_t(500);
    }
    if (ic=='2'){
      delay(500);
      //period = 2ms
      for (j=0;j<1000;j++) fourpulses_t(1000);
    }

    if (ic=='3'){
     //also causes the LED to blink 
     delay(500);
     for (j=0;j<50;j++){
      for (int i=0;i<50;i++) fourpulses_t(500);//100ms
      digitalWrite(pin11,HIGH);
      for (int i=0;i<50;i++) fourpulses_t(500);//100ms
      digitalWrite(pin11,LOW);   
      }
    }

    if (ic=='4'){
     //also causes the LED to blink 
     delay(500);
     for (j=0;j<50;j++){
      for (int i=0;i<50;i++) fourpulses_t(1000);//200ms
      digitalWrite(pin11,HIGH);
      for (int i=0;i<50;i++) fourpulses_t(1000);//200ms
      digitalWrite(pin11,LOW);   
      }
    }

   if (ic=='5'){
     delay(500);
     //period = 5ms
     for (j=0;j<1010;j++) fourpulses_t(2500);
   }

   if (ic=='6'){
     delay(500);
     //period = 10ms
     for (j=0;j<1010;j++) fourpulses_t(5000);
   }

   if (ic=='7'){
     delay(500);
     //period = 20ms
     for (j=0;j<1010;j++) fourpulses_t(10000);
   }
   if (ic=='8'){
     delay(500);
     //period = 30ms
     for (j=0;j<1010;j++) fourpulses_t(15000);

   }
 
  //The following options are used to control the thorlabs digital stage (for conventional lightsheet scanning with one camera)
  //Note that in practice the stage can also be operated manually - these options are not essential for the operation of the SPEED microscope.
   if (ic=='z') dac.setVoltage(0,false);
   if (ic=='m') dac.setVoltage(2024,false);
   if (ic=='u') {
      if (voltage < 3968) voltage = voltage + 128;
      dac.setVoltage(voltage,false);
    }
   if (ic=='d'){
      if (voltage >= 128) voltage = voltage -128;
      dac.setVoltage(voltage ,false);
    }
    
   }//while
} 


