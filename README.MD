# Part 1
1. Install RaghavAndroidApp and open in Anroid Studio.

2. Run on any Android device to measure heart rate, respiratory rate and current location coordinates.

3. Click 'Upload Symptoms' to create a local database.


# Part 2
1. Dependencies
	- Install nginx server and PHP for Windows.
	- pip install sqlalchemy

2. Make sure PHP is added to the environment variables.

3. Add paths to the nginx folder and PHP folder in the PHP configuration (php.ini) file.

4. Add 'indexLoc.html' and 'indexLoc.htm' filename in the nginx configuration file.

5. Add the following files in the \html folder of the nginx directory
	- LifeMap_GS[1-11]
	- indexLoc.html
	- process.php
	- readDB.js
	- readDB.py

5. Run PHP using the following command in the terminal
	cd C:\php-7.4.12-nts-Win32-vc15-x64
	php-cgi.exe -b 127.0.0.1:9000

6. Start the nginx server 

7. Type 'localhost' in your browser to navigate to the webpage.

8. Enter the User ID and date to download a copy of Contact Trace adjacency matrix in .txt format.
