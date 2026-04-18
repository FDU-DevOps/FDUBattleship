# About the Project

FDU DEVOPS Class Battleship Game

Requirements: We support Java 25, 21, and 17 and the Windows 11 Command Prompt

In order to run this, download the latest successful workflow under Actions. Download the jar file under artifacts.
Then, extract the zip file and navigate to the directory containing the jar file.
Then either open command prompt in that directory directly, or navigate to that directory from command prompt using the cd command.

Example Path: "C:\Users\<username>\Downloads\battleship-jar

Then, run the command java -jar FDUBattleship-1.0-SNAPSHOT.jar

Finally, open a browser and go to either localhost:8090 or 127.0.0.1:8090

# Update Log
* Version 1.0-DEVOPS-553
  * Updated number of guesses for Battleship
* Version 1.0-DEVOPS-516
     * Added README File
* Version 1.0-DEVOPS-496
  * Migrated to new path structure on servers
    * e.g. test.fdugames.org/battleship
    * test.fdugames.org/wordle
    * test.fdugames.org/jenkins
  * Will also support without application change production server as well
  * Note: once merged, local access will also required explicit path
    * e.g. http://localhost:8081/battleship
* Version 1.0-DEVOPS-374
  * Added basic player vs computer gameplay mode
  * Player’s ships are automatically pre-placed randomly at the start of the game
  * Computer now makes simple/dummy moves in response to the player’s guesses
  * After each human guess, the computer immediately performs its own guess on the player’s board

# Current Known Bugs
* DEVOPS-509 - Manual build fails to start new build (old build continues to run on server)
