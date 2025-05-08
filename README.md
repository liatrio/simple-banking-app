# SmartBank - Simple Banking Application

A comprehensive desktop banking application built with Java and JavaFX that simulates a full-featured banking experience.

## Features
- Account management (create, view, and manage multiple account types)
- Transaction processing (deposits, withdrawals, and transfers)
- Credit account management with credit limit controls
- Investment account tracking
- Recurring transaction scheduling
- Budget planning and tracking
- Transaction categorization and search
- Statement generation
- Interactive financial visualizations and charts
- Accessibility features (screen reader support, keyboard navigation, themes)
- User authentication and session management
- User profile management

## Technical Implementation
- JavaFX for the user interface with FXML-based views
- Object-oriented design with inheritance and polymorphism
- JPA for database persistence and management
- Repository pattern for data access
- Service-oriented architecture
- Event-driven components for notifications
- Theme support with CSS styling
- Comprehensive exception handling

## Project Structure
- `src/` - Source code directory
  - `com.smartbank` - Main package
    - `SmartBankApp.java` - Main application class
    - `model/` - Data models and business logic
    - `controller/` - Application controllers  
    - `repository/` - Data access layer
    - `service/` - Business logic services
      - `accessibility/` - Accessibility support services
      - `budgeting/` - Budget planning services
      - `category/` - Transaction categorization 
      - `credit/` - Credit management
      - `interest/` - Interest calculation  
      - `recurring/` - Recurring transaction management
      - `reporting/` - Financial reporting services
      - `search/` - Transaction search functionality
      - `statement/` - Statement generation
      - `theme/` - Theme management services
      - `transfer/` - Fund transfer services
      - `visualization/` - Data visualization
    - `auth/` - Authentication and session management
    - `view/` - FXML views and CSS styling
      - `themes/` - Application theme stylesheets
    - `util/` - Utility classes and database management

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
- JPA is used for database persistence

## Accessibility Features
- Screen reader support for visually impaired users
- Keyboard navigation for motor impaired users
- Multiple theme options including:
  - High contrast theme
  - Dark and light themes
  - Color blindness support (protanopia, deuteranopia, tritanopia, achromatopsia)
  - Configurable font sizes

## Security Features
- User authentication and authorization
- Session management and tracking
- Role-based permissions
- Remember-me functionality
- Session timeout protection

## Data Management
- JPA-based persistence layer
- Transaction categorization and search
- Statement generation in multiple formats
- Data migration utilities for version upgrades

## Troubleshooting
- If you encounter JavaFX related errors, ensure your Java installation includes JavaFX or that the JavaFX modules are correctly specified in the build.gradle file
- For logging output, the application uses Java's built-in logging with a custom format
- Check the console output for any error messages if the application fails to start
- For database connectivity issues, verify your database configuration in the application properties
