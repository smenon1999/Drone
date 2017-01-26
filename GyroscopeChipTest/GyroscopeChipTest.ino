#include <Wire.h>
#include <SPI.h>
#include <Adafruit_LSM9DS0.h>
#include <Adafruit_Sensor.h> 

//Adafruit_LSM9DS0 lsm = Adafruit_LSM9DS0(13, 12, 11, 10, 9);
Adafruit_LSM9DS0 lsm = Adafruit_LSM9DS0();
float pos[3];
float dT=1/100;
float zeroOffset[3];

float timeStamp=0;

void setup() {
  Serial.begin(9600);

  for (int i=0;i<3;i++) {
    pos[i]=0;
    zeroOffset[i]=0;
  }
  if (!lsm.begin())
  {
    Serial.println("No Sensor detected!");
  }
  lsm.setupGyro(lsm.LSM9DS0_GYROSCALE_2000DPS);
  // put your setup code here, to run once:

}

bool firstRun=true;
void loop() {

  // put your main code here, to run repeatedly:
  if (timeStamp==0)
    timeStamp=millis();
  else
    timeStamp=millis()-timeStamp;

  float dT=timeStamp;
  sensors_event_t accel, gyro, mag, temp;
  lsm.getEvent(&accel, &gyro, &mag, &temp);
  float ratex = (gyro.gyro.x/1.0000000000f)-zeroOffset[0];
  float ratey = (gyro.gyro.y/1.0000000000f)-zeroOffset[1];
  float ratez = (gyro.gyro.z/1.0000000000f)-zeroOffset[2];
  if (firstRun) {
    zeroOffset[0]=ratex;
    zeroOffset[1]=ratey;
    zeroOffset[2]=ratez;
    firstRun=false;
  }

  pos[0]+=ratex*dT;
  pos[1]+=ratey*dT;
  pos[2]+=ratez*dT;


  Serial.print("X: ");Serial.println(pos[0]);
  Serial.print("Y: ");Serial.println(pos[1]);
  Serial.print("Z: ");Serial.println(pos[2]);
  delay(1000);


}
