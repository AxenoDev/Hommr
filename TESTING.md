# Testing Guide for Hommr

## Overview

This project includes a comprehensive test suite with over 2,000 lines of test code covering all major components of the Hommr plugin.

## Quick Start

### Running All Tests

```bash
./gradlew test
```

### Running Tests with Detailed Output

```bash
./gradlew test --info
```

### Running Specific Test Classes

```bash
# Run tests for Home model
./gradlew test --tests HomeTest

# Run tests for PlayerHomes model
./gradlew test --tests PlayerHomesTest

# Run tests for DatabaseManager
./gradlew test --tests DatabaseManagerTest

# Run tests for HomeManager
./gradlew test --tests HomeManagerTest

# Run tests for Hommr plugin class
./gradlew test --tests HommrTest
```

### Running Tests for Specific Methods

```bash
# Run a single test method
./gradlew test --tests HomeTest.testFromLocationCreatesHomeWithCorrectData

# Run tests matching a pattern
./gradlew test --tests HomeTest.*Location*
```

## Test Structure

```
src/test/java/me/axeno/hommr/
├── HommrTest.java                    # Main plugin lifecycle tests
├── managers/
│   ├── DatabaseManagerTest.java     # Database operations tests
│   └── HomeManagerTest.java         # Home management logic tests
└── models/
    ├── HomeTest.java                 # Home model tests
    └── PlayerHomesTest.java          # Player homes collection tests
```

## Test Coverage

- **HomeTest**: 22 tests covering location conversion, data persistence, and edge cases
- **PlayerHomesTest**: 30 tests covering home collections, thread safety, and case sensitivity
- **DatabaseManagerTest**: 28 tests covering database operations, persistence, and integrity
- **HomeManagerTest**: 30+ tests covering business logic, events, and player interactions
- **HommrTest**: 35+ tests covering plugin lifecycle and component initialization

**Total**: 145+ comprehensive test cases

## Requirements

- **Java**: 21 or higher
- **Gradle**: 8.x (included via wrapper)

## Test Dependencies

The test suite uses:
- **JUnit 5** (Jupiter 5.10.1) - Modern testing framework
- **Mockito 5** (5.8.0) - Mocking framework for dependencies
- **Paper API** - For Bukkit/Spigot testing
- **ORMLite** - For database testing
- **SQLite JDBC** - For in-memory database tests

## Continuous Integration

Tests are automatically run in CI/CD via GitHub Actions on:
- Pull requests
- Pushes to main branches

See `.github/workflows/build.yml` for CI configuration.

## Test Patterns

### Mocking Bukkit Objects

```java
@Mock
private World mockWorld;

@Mock
private Player mockPlayer;

@BeforeEach
void setUp() {
    when(mockWorld.getName()).thenReturn("world");
    when(mockPlayer.getUniqueId()).thenReturn(testPlayerId);
}
```

### Testing Events

```java
ArgumentCaptor<HomeSetEvent> eventCaptor = ArgumentCaptor.forClass(HomeSetEvent.class);
HomeManager.setHome(mockPlayer, "home1", location);
verify(mockPluginManager).callEvent(eventCaptor.capture());
HomeSetEvent event = eventCaptor.getValue();
assertEquals("home1", event.getHomeName());
```

### Testing Database Operations

```java
@TempDir
Path tempDir;

@BeforeEach
void setUp() {
    when(mockPlugin.getDataFolder()).thenReturn(tempDir.toFile());
    databaseManager.init();
}
```

## Writing New Tests

### Best Practices

1. **Test Naming**: Use descriptive names that explain what is being tested
   - ✅ `testSetHomeCreatesNewHome()`
   - ❌ `testSetHome1()`

2. **Arrange-Act-Assert**: Structure tests clearly
   ```java
   @Test
   void testSetHomeCreatesNewHome() {
       // Arrange
       Location location = new Location(mockWorld, 100, 64, 200);

       // Act
       boolean result = HomeManager.setHome(mockPlayer, "home1", location);

       // Assert
       assertTrue(result);
       assertTrue(HomeManager.hasHome(testPlayerId, "home1"));
   }
   ```

3. **Test Isolation**: Each test should be independent
   - Use `@BeforeEach` for setup
   - Use `@AfterEach` for cleanup
   - Don't rely on test execution order

4. **Edge Cases**: Always test boundary conditions
   - Null values
   - Empty collections
   - Maximum/minimum values
   - Special characters

5. **Mock Cleanup**: Close static mocks in `@AfterEach`
   ```java
   @AfterEach
   void tearDown() {
       if (bukkitMockedStatic != null) {
           bukkitMockedStatic.close();
       }
   }
   ```

## Debugging Tests

### Enable Debug Logging

```bash
./gradlew test --debug
```

### Show Standard Output

Edit `build.gradle`:
```gradle
test {
    testLogging {
        showStandardStreams = true
    }
}
```

### Run Tests in IDE

Most IDEs (IntelliJ IDEA, Eclipse, VS Code) can run JUnit 5 tests directly:
1. Right-click on test class or method
2. Select "Run Test" or "Debug Test"

## Common Issues

### Test Failures

1. **Mockito Issues**: Ensure all mocks are properly initialized with `@ExtendWith(MockitoExtension.class)`
2. **Static Mocks**: Always close `MockedStatic` instances in `@AfterEach`
3. **Bukkit API**: Use mocks for all Bukkit classes (Server, World, Player, etc.)

### Build Issues

1. **Java Version**: Ensure Java 21 is installed
   ```bash
   java -version
   ```

2. **Gradle Sync**: Refresh Gradle dependencies
   ```bash
   ./gradlew --refresh-dependencies
   ```

## Performance

Tests are designed to run quickly:
- **Unit Tests**: < 1 second per test
- **Database Tests**: Use in-memory SQLite for speed
- **Full Suite**: Typically completes in < 30 seconds

## Coverage Reports

To generate code coverage reports (requires JaCoCo plugin):

```bash
./gradlew test jacocoTestReport
```

Report will be available at: `build/reports/jacoco/test/html/index.html`

## Contributing Tests

When adding new features:
1. Write tests first (TDD approach) or alongside the implementation
2. Ensure at least 80% code coverage for new code
3. Include both positive and negative test cases
4. Test edge cases and boundary conditions
5. Update this documentation if adding new test patterns

## Additional Resources

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Paper API Documentation](https://jd.papermc.io/paper/1.21/)

For detailed test information, see `src/test/TEST_SUMMARY.md`.