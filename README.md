# Conni
Tiny desktop and server app to test the connection to the Internet or any HTTP service

## Usage

### Prerequisites
Having Java 8 installed on your machine.

### Download and execute
Just download the JAR: [conni-all-0.1-SNAPSHOT.jar](libs/conni-all-0.1-SNAPSHOT.jar) 

### Run and use Conni
Double-click on the downloaded JAR file.

An icon appears in the system tray to indicate the Internet connection status ![Tray icon](images/current-icon.png "Tray icon")
The color depends on the connection status.

- Green: Connection is up (put the mouse on the try icon to get details about the check speed)
- Red: Connection is down
- Blue: Connection status is still unknown, often during startup while the ping service is called for the first time.

### Shutdown Conni
Click on the tray icon to open the menu and click on the "Quit" item to quit the application.


## Sprints
### Sprint 1: Minimum viable product (MVP) :white_check_mark:
- Create a service that can call a remote server to check every 20 seconds if the machine is connected to the Internet or not.
- Reflect the connection status on a system tray icon.
- Reduce the check period to 5 seconds in case of connection down.
- Create a JAR containing all the necessary classes and files for the execution.

### Sprint 2: Logs, connection speed visualization and internationalization
- Save the results of all the calls in a local database.
- Use shades of colors between orange and green to specify the current connection speed compared to the  average of the last 72 hours.
- Make the texts internationalized.
- Add the ability to pause the checks.

### Sprint 3: CI
- Create a continuous integration environment to test after each commit and build the app nightly.

### Sprint 4: Settings
- Create a tiny web interface to administrate the settings:
  - Check period
  - Customize the request for the check
  - Addition of custom response processors (provide access to the menu and tray icon)
  - Enable / Disable response processors
  - Location of the local data folder
  - Internet browser to use to edit the settings
  - Delete all the local files

### Sprint 5: Reports
- Display web reports with charts to display the connection status logs (failures and response time).

### Sprint 5: Packaging
- Create packages for executable for Windows, Mac and Linux (deb and rpm).

### Sprint 6: Multiple checks
- Run multiple checks on different HTTP services and assign different chains of response processors.
- Create a new system tray icon to reflect the overall status and the number of checks in failure.
- Attach a popup menu to that new system tray icon to display the connectivity of each check.

### Sprint 7: Scripting
- Ability to create response processors with Groovy scripts.

