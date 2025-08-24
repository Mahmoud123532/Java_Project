Soccer League Management Project

This project is a simple soccer league management system built in Java with SQLite as the database. It allows adding leagues, matches, players, and teams, and recalling past data.

⚙️ Prerequisites

Before running the project, make sure you have the following installed:

Java JDK (17 or later recommended)

Download Java

IntelliJ IDEA Community Edition

Download IntelliJ

SQLite Studio (for exploring and editing the database easily)

Download SQLite Studio

SQLite JDBC Driver (JAR file)

Download the latest JDBC driver JAR from:
SQLite JDBC Driver


🛠️ Setting up IntelliJ IDEA

Open IntelliJ and create/import this project.

Add the SQLite JDBC JAR file:

Go to File → Project Structure → Modules → Dependencies.

Click + → JARs or Directories... and select the downloaded sqlite-jdbc-x.x.x.jar.

Apply changes.


📂 Database Setup

The project comes with a database file called soccer.db.

To view or edit it:

Open SQLite Studio.

Go to Database → Add a database.

Browse and select soccer.db.

Once added, you can expand the database tree and see the following tables:

teams

players

matches

events

To view the actual stored data:

Click on a table (e.g., players).

Open the Data tab to see rows (records).



▶️ Running the Program

In IntelliJ, open Run → Edit Configurations.

Add a new Application configuration:

Main Class: your main program class (e.g., Main or SoccerApp).

Program Arguments: (optional) You can set program arguments if needed.

Click Run ▶️ or Shift + F10 to start the program.


⚙️ Setup

If this warning appeared : WARNING: java.lang.System::load has been called by org.sqlite.SQLiteJDBCLoader in an unnamed module (file:/C:/Users/Baba/Downloads/sqlite-jdbc-3.20.1.jar) WARNING: Use --enable-native-access=ALL-UNNAMED to avoid a warning for callers in this module WARNING: Restricted methods will be blocked in a future release unless native access is enabled

Configure Run options to remove warning:

Go to the top menu → Run > Edit Configurations....

Click + Add New Configuration → choose Application .

Under Modify options, check Add VM options.

In VM options, paste:

--enable-native-access=ALL-UNNAMED


Apply and save.