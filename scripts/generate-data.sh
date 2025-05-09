#!/bin/bash

# Script to compile and run the data generator for SmartBank

# Ensure we're in the project root directory
cd "$(dirname "$0")/.."

# Ensure persistence.xml is in the standard location (src/main/resources/META-INF)
echo "Ensuring persistence.xml is properly set up..."

# Create the standard directory
mkdir -p src/main/resources/META-INF

# Check if persistence.xml exists in the standard location
if [ -f src/main/resources/META-INF/persistence.xml ]; then
    echo "Persistence configuration found in standard location."
elif [ -f src/META-INF/persistence.xml ]; then
    # If it exists in the old location, copy it to the standard location
    echo "Copying persistence.xml from src/META-INF to standard location..."
    cp src/META-INF/persistence.xml src/main/resources/META-INF/
else
    # Create a default persistence.xml if not found
    echo "Creating default persistence.xml file in standard location..."
    cat > src/main/resources/META-INF/persistence.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="http://xmlns.jcp.org/xml/ns/persistence"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/persistence
             http://xmlns.jcp.org/xml/ns/persistence/persistence_2_2.xsd"
             version="2.2">
    <persistence-unit name="smartbank-pu" transaction-type="RESOURCE_LOCAL">
        <description>SmartBank Persistence Unit</description>
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
        
        <!-- Entity classes -->
        <class>com.smartbank.model.Account</class>
        <class>com.smartbank.model.SavingsAccount</class>
        <class>com.smartbank.model.CreditAccount</class>
        <class>com.smartbank.model.CreditHistory</class>
        <class>com.smartbank.model.CreditLimitChangeRequest</class>
        <class>com.smartbank.model.User</class>
        <class>com.smartbank.model.ThemePreference</class>
        <class>com.smartbank.model.Transaction</class>
        <class>com.smartbank.model.TransactionCategory</class>
        <class>com.smartbank.model.RecurringTransaction</class>
        <class>com.smartbank.service.interest.InterestCalculationRecord</class>
        <class>com.smartbank.service.recurring.RecurringTransactionExecution</class>
        <class>com.smartbank.service.statement.StatementRecord</class>
        
        <properties>
            <!-- Specify the database connection -->
            <property name="javax.persistence.jdbc.driver" value="org.sqlite.JDBC"/>
            <property name="javax.persistence.jdbc.url" value="jdbc:sqlite:smartbank.db"/>
            
            <!-- Configure Hibernate for SQLite -->
            <property name="hibernate.dialect" value="org.hibernate.dialect.SQLiteDialect"/>
            <property name="hibernate.connection.autocommit" value="true"/>
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
            
            <!-- Fix SQLite datetime handling -->
            <property name="hibernate.jdbc.time_zone" value="UTC"/>
            <property name="hibernate.connection.handling_mode" value="DELAYED_ACQUISITION_AND_HOLD"/>
            
            <!-- Configure table generation -->
            <property name="hibernate.hbm2ddl.auto" value="update"/>
            
            <!-- Disable second-level cache -->
            <property name="hibernate.cache.use_second_level_cache" value="false"/>
            <property name="hibernate.cache.use_query_cache" value="false"/>
        </properties>
    </persistence-unit>
</persistence>
EOF
fi

# Clean and build the project to ensure all dependencies are available
echo "Building the project..."
./gradlew clean build

if [ $? -ne 0 ]; then
    echo "Build failed! Please check for errors."
    exit 1
fi

echo "Backing up existing database file..."
if [ -f smartbank.db ]; then
    timestamp=$(date +%Y%m%d%H%M%S)
    mv smartbank.db "smartbank.db.backup.$timestamp"
    echo "Existing database backed up to smartbank.db.backup.$timestamp"
fi

echo "Running data generator..."
# Use gradle to run the DataGenerator class with the mainClass property
./gradlew run -PmainClass=com.smartbank.scripts.DataGenerator

if [ $? -eq 0 ]; then
    echo "Data generation completed successfully!"
else
    echo "Data generation failed. Check the error messages above."
    exit 1
fi

echo "You can now run the application with: ./gradlew run"