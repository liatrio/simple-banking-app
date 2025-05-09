# SmartBank Testing Framework

This directory contains the testing framework and tests for the SmartBank application.

## Testing Approach

The testing approach follows these key principles:

1. **Test Coverage Targets**:
   - Model classes: 80% line coverage (enforced by quality gate)
   - Service classes: 70% line coverage (enforced by quality gate)
   - Utility classes: 85% line coverage (enforced by quality gate)
   - Overall: 70% branch coverage (enforced by quality gate)

2. **Test Organization**:
   - Tests mirror the package structure of the main codebase
   - Unit tests are placed in the corresponding package
   - Integration tests are placed in an `integration` package

3. **Testing Tools**:
   - JUnit 5 (Jupiter) for test execution
   - Mockito for mocking dependencies
   - AssertJ for fluent assertions
   - JaCoCo for code coverage analysis
   - TestFX for JavaFX UI testing
   - H2 in-memory database for repository testing

## Running Tests

To run all tests:
```
./gradlew test
```

To generate a test coverage report:
```
./gradlew jacocoTestReport
```
The report will be available at `build/reports/jacoco/test/html/index.html`

To verify code coverage against quality gates:
```
./gradlew jacocoTestCoverageVerification
```

## CI/CD Integration

The testing framework is integrated with CI/CD through GitHub Actions. The workflow is defined in `.github/workflows/gradle-test.yml` and includes:

1. **Automated Test Execution**: Tests are automatically run on every push to main/master/develop branches and on pull requests.
2. **Coverage Reports**: JaCoCo reports are generated and uploaded as artifacts.
3. **Quality Gates**: Code coverage is verified against the defined thresholds.
4. **Test Results**: Test results are uploaded as artifacts for review.

The CI/CD pipeline will fail if:
- Any test fails
- Code coverage falls below the defined thresholds
- The build process fails

## Test Fixtures

Common test fixtures and utilities are available in the `test/com/smartbank/util/fixtures` package.
These should be used to ensure consistent test data across test cases.
