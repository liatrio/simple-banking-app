#!/bin/bash

# Script to run the SmartBank Data Migration Tool
# This script migrates data from in-memory storage to the SQLite database

# Change to project root directory
cd "$(dirname "$0")/.." || exit 1

echo "=== SmartBank Data Migration Tool ==="
echo "This utility will migrate data from in-memory storage to the SQLite database."
echo ""

# Check if the database file exists
if [ -f "smartbank.db" ]; then
    echo "Database file exists. Proceeding with migration..."
else
    echo "Warning: Database file 'smartbank.db' not found. A new database will be created."
fi

# Ask for confirmation
read -p "Do you want to continue with the migration? (y/n): " CONFIRM
if [ "$CONFIRM" != "y" ] && [ "$CONFIRM" != "Y" ]; then
    echo "Migration cancelled."
    exit 0
fi

echo ""
echo "Starting migration..."

# Run the data migration tool using Gradle
./gradlew run --args="migration" 2>/dev/null || {
    # If Gradle command fails, try running the java command directly
    echo "Gradle execution failed. Trying direct Java execution..."
    
    # Compile the code if needed
    ./gradlew compileJava
    
    # Run the migration tool directly
    java -cp build/classes/java/main com.smartbank.util.migration.DataMigrationTool
}

# Check if migration was successful
if [ $? -eq 0 ]; then
    echo "Migration completed successfully."
else
    echo "Migration failed. Please check the logs for details."
    exit 1
fi

# Print instructions for verifying migration
echo ""
echo "To verify the migration, you can run:"
echo "sqlite3 smartbank.db 'SELECT COUNT(*) FROM users; SELECT COUNT(*) FROM accounts; SELECT COUNT(*) FROM transactions;'"

exit 0