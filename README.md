# SmartBank - Simple Banking Application

A desktop banking application built with Java and JavaFX that simulates essential banking features.

## Features
- Account management (open new accounts)
- Transaction processing (deposits and withdrawals)
- Account details display
- Transaction history tracking
- Multiple account types (Savings, Credit)

## Technical Implementation
- JavaFX for the user interface
- Object-oriented design with inheritance and polymorphism
- Exception handling for input validation
- Data storage using ArrayLists
- Static variables for account number generation

## Project Structure
- `src/` - Source code directory
  - `com.smartbank` - Main package
    - `model/` - Data models and business logic
    - `controller/` - Application controllers
    - `view/` - FXML and CSS files
    - `util/` - Utility classes
  - `SmartBankApp.java` - Main application class

## Prerequisites
- Java JDK 11 or higher
- Gradle 7.0 or higher (or use the included Gradle wrapper)

## Building the Application
The project uses Gradle as its build system. You can build the application using the following commands:

### Using Gradle Wrapper (recommended)
```bash
# On macOS/Linux
./gradlew build

# On Windows
gradlew.bat build
```

### Using Gradle (if installed globally)
```bash
gradle build
```

## Running the Application
You can run the application directly using Gradle:

### Using Gradle Wrapper
```bash
# On macOS/Linux
./gradlew run

# On Windows
gradlew.bat run
```

### Using Gradle (if installed globally)
```bash
gradle run
```

### Using JAR file
After building, you can run the generated JAR file:
```bash
java -jar build/libs/simple-banking-app.jar
```

## Development
- The application uses JavaFX 21.0.2
- Source compatibility is set to Java 11
- The main class is `com.smartbank.SmartBankApp`

## Troubleshooting
- If you encounter JavaFX related errors, ensure your Java installation includes JavaFX or that the JavaFX modules are correctly specified in the build.gradle file
- For logging output, the application uses Java's built-in logging with a custom format
- Check the console output for any error messages if the application fails to start
