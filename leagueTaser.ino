int taserPin = 6;

void setup() {
  // put your setup code here, to run once:
pinMode(taserPin, OUTPUT);
Serial.begin(9600);
}

void loop() {
  // put your main code here, to run repeatedly:
String myString = String(Serial.read());
if(myString == "49"){
    digitalWrite(taserPin, HIGH);
    delay(100);
  }else{
    digitalWrite(taserPin, LOW);
  }
}
