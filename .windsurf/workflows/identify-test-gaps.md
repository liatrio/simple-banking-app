---
description: Identify areas with insufficient test coverage
---

1. Generate the latest JaCoCo coverage report:
   ```
   ./gradlew clean test jacocoTestReport
   ```

2. Open the coverage report:
   ```
   open build/reports/jacoco/test/html/index.html
   ```

3. Identify areas with insufficient coverage, focusing on:
   - Service layer (currently missing tests)
   - Controller layer (limited testing)
   - Integration tests (not implemented)
   - UI components that need TestFX testing

4. Generate a list of components that need additional tests, prioritizing:
   - Components below the 70% line coverage threshold for model and utility classes
   - Components below the 50% branch coverage threshold overall
   - Critical business logic components
   
5. Run the JaCoCo verification to check against quality gates:
   ```
   ./gradlew jacocoTestCoverageVerification
   ```

6. Create test stubs for identified components using the appropriate testing framework:
   - JUnit 5 for unit tests
   - Mockito for service and controller tests
   - TestFX for UI components
   
7. Focus on the following areas based on current coverage gaps:
   - Theme and accessibility services (Task 25)
   - Service implementations
   - Controller classes beyond LoginController
