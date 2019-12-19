# Course Project
## Android side
- The "Android" file is an Android Studio project, and the main functionality is implemented in two files:
<ol>
<li>`.\Android\app\src\main\java\com\courseproject\raspberryconc\BluetoothChatService.java`</li>
<li>`.\Android\app\src\main\java\com\courseproject\raspberryconc\MainActivity.java`</li>
</ol>
- The first one is a package for bluetooth communication. The second one is the Activity for the GUI control.

## Raspberry Pi side
- All the functionalities are implemented in `.\RaspberryPi\pi_file.py`.
- Run `sudo python .\RaspberryPi\pi_file.py` to start the program in Raspberry Pi.

### Dependencies:
- Python 2.7
- pybluez
- picamera 
- Adafruit_DHT

