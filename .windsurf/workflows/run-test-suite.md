---
description: Run the complete test suite with coverage reporting
---

1. Navigate to the project root directory
2. Run the test suite with JaCoCo coverage enabled:
   ```
   gradlew clean test jacocoTestReport
   ```
3. Verify that the test coverage meets the quality gates:
   - 70% line coverage for model and utility classes
   - 50% branch coverage overall
4. View the generated coverage report:
   ```
   open target/site/jacoco/index.html
   ```
5. Check test results summary to identify any failures