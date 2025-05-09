#!/bin/bash

# Script to compile and run the data generator for SmartBank

# Ensure we're in the project root directory
cd "$(dirname "$0")/.."

# Ensure persistence.xml is in both required classpath locations
echo "Ensuring persistence.xml is properly set up..."
if [ ! -f src/META-INF/persistence.xml ]; then
    echo "Error: persistence.xml not found in src/META-INF/"
    echo "Please ensure the persistence.xml file exists before running this script."
    exit 1
fi

mkdir -p src/main/resources/META-INF
cp src/META-INF/persistence.xml src/main/resources/META-INF/
echo "Persistence configuration copied successfully."

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