# Automation Framework

Enterprise UI + API + DB test automation framework using **Selenium WebDriver**, **TestNG**, **Cucumber JVM**, **REST-assured**, and **JDBC**.

---

## Project Structure

```
project-root/
├── config/env/               ← Environment-specific property files
├── src/main/java/com/company/automation/
│   ├── core/               ← Driver, waits, actions, logging, exceptions
│   ├── config/             ← ConfigLoader, Environment, ConfigKeys
│   ├── pages/              ← Page Objects (login/, [feature]/)
│   ├── components/         ← Reusable page fragments (Table, Modal, Pagination)
│   ├── utilities/          ← FileUtil, JsonUtil, DataTableUtil
│   ├── testdata/           ← TestDataLoader, TestDataProvider, models/
│   ├── api/                ← REST client + endpoints
│   ├── db/                 ← JDBC connection + query executor
│   └── reporting/          ← ExtentReports + ScreenshotManager
├── src/main/resources/     ← log4j2.xml
├── src/test/java/          ← Hooks, listeners, stepdefs, runners
├── src/test/resources/
│   ├── features/           ← Cucumber .feature files
│   ├── suites/             ← TestNG XML suites
│   └── testdata/           ← JSON test data files
├── reports/                ← HTML/JSON test reports (generated)
├── docker/selenium-grid/   ← Selenium Grid docker-compose
└── .github/workflows/      ← GitHub Actions CI
```

---

## Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **Chrome / Firefox / Edge** installed locally (for local execution)
- **Docker** (optional, for Selenium Grid)

---

## Quick Start

```bash
# 1. Install dependencies (WebDriverManager auto-downloads browser drivers on first run)
mvn clean compile

# 2. Run all tests against dev (default)
mvn test

# 3. Run smoke suite only
mvn test -DsuiteXmlFile=src/test/resources/suites/smoke.xml

# 4. Run regression suite
mvn test -DsuiteXmlFile=src/test/resources/suites/regression.xml
```

---

## Running with Environment Profiles

```bash
# Development (default)
mvn test

# Staging
mvn test -Denv=staging

# Production
mvn test -Denv=prod
```

Environment files are in `config/env/config.[env].properties`.

### Key Config Properties

| Key | Description | Default |
|---|---|---|
| `app.baseurl` | Application base URL | `https://dev.example.com` |
| `driver.type` | Browser: `chrome`, `firefox`, `edge` | `chrome` |
| `driver.headless` | Run browser headless | `false` (dev), `true` (prod) |
| `driver.remote.enabled` | Use Selenium Grid | `false` |
| `driver.remote.url` | Grid hub URL | empty |
| `db.enabled` | Enable DB layer | `false` |
| `report.screenshot.onfailure` | Capture screenshot on fail | `true` |

---

## Running Test Types

### Cucumber (BDD / .feature files)

```bash
# Run all feature files
mvn test

# Run only @smoke tagged scenarios
mvn test -Dcucumber.filter.tags="@smoke"

# Run only @regression tagged scenarios
mvn test -Dcucumber.filter.tags="@regression"

# Run only login feature
mvn test -Dcucumber.filter.tags="@login"

# Run with Staging environment
mvn test -Denv=staging
```

### TestNG (page-object / API / DB tests)

```bash
# Run smoke suite
mvn test -DsuiteXmlFile=src/test/resources/suites/smoke.xml

# Run regression suite (parallel)
mvn test -DsuiteXmlFile=src/test/resources/suites/regression.xml

# Run full suite
mvn test -DsuiteXmlFile=src/test/resources/suites/full.xml
```

### Headless (no browser window)

```bash
mvn test -Denv=staging -Ddriver.headless=true
```

### Against Selenium Grid

Set in `config/env/config.prod.properties`:

```
```
driver.remote.enabled=true
driver.remote.url=http://selenium-grid:4444/wd/hub
```
Then run:
```bash
mvn test -Denv=prod
```

---

## Docker Selenium Grid

Start the Grid locally:

```bash
cd docker/selenium-grid
docker-compose up -d
```

Grid console: `http://localhost:4444`

Stop:
```bash
docker-compose down
```

---

## Adding a New Page Object

### 1. Create the page class

```java
// src/main/java/com/company/automation/pages/[feature]/MyPage.java
package com.company.automation.pages.<feature>;

import com.company.automation.pages.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class MyPage extends BasePage {

    private final By headerTitle = By.cssSelector("h1.page-title");

    public MyPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isLoaded() {
        return isDisplayed(headerTitle);
    }

    // Page-specific actions
    public void performAction(String value) {
        type(By.id("action-input"), value);
        click(By.id("submit-btn"));
    }
}
```

### 2. Register locators

Add `By` locators as private fields at the top. Group them logically.

### 3. Add page load verification

Implement `isLoaded()` — returns `true` when the page is ready (e.g. a unique element is visible).

### 4. Use in step definitions

```java
MyPage myPage = new MyPage(getDriver());
myPage.performAction("test value");
```

---

## Adding a New Feature (.feature file)

### 1. Create the feature file

```
src/test/resources/features/[feature-area]/my_feature.feature
```

### 2. Add tags

```gherkin
@feature @smoke
Feature: My feature description
  ...
```

### 3. Run the feature

```bash
mvn test -Dcucumber.filter.tags="@feature"
```

---

## Adding New Step Definitions

### 1. Create the step-def class

```java
// src/test/java/com/company/automation/stepdefinitions/[feature]/MyStepDefs.java
package com.company.automation.stepdefinitions.<feature>;

import com.company.automation.stepdefinitions.BaseStepDefs;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class MyStepDefs extends BaseStepDefs {

    @Given("the user is on the My Page")
    public void user_on_my_page() {
        openUrl("https://example.com/my-page");
    }

    @When("the user submits the form")
    public void user_submits_form() {
        // delegate to page object
    }

    @Then("a success message is shown")
    public void success_message_shown() {
        // assertions
    }
}
```

### 2. Ensure package is in Cucumber glue path

The `CucumberRunner` glue is already set to:
```
glue = {"com.company.automation.hooks", "com.company.automation.stepdefinitions"}
```
Any class under `stepdefinitions` package is automatically found.

---

## Test Data

### JSON test data

Place files under `src/test/resources/testdata/`:

```
src/test/resources/testdata/
├── login/
│   ├── valid_users.json
│   └── invalid_users.json
└── common/
    └── test_accounts.json
```

Load in step definitions:

```java
List<UserTestData> users = TestDataLoader.loadArrayAs("login/valid_users.json", UserTestData.class);
```

### CSV test data

```java
List<Map<String, String>> rows = DataTableUtil.parseCsvWithHeaders(
    Paths.get("src/test/resources/testdata/products/catalog.csv"));
```

### TestNG DataProvider

```java
@DataProvider(name = "users")
public static Iterator<Object[]> users(Method m) {
    return TestDataProvider.jsonProvider("login/valid_users.json", UserTestData.class);
}

@Test(dataProvider = "users")
public void testLogin(UserTestData user) {
    new LoginPage(getDriver()).login(user.getUsername(), user.getPassword());
}
```

---

## API Testing

```java
AuthEndpoints auth = new AuthEndpoints();
auth.login("admin", "secret123").then().statusCode(200);
```

Extend `BaseApiClient` for new endpoint groups.

---

## Database Testing

Enable in config: `db.enabled=true`

```java
List<Map<String, Object>> rows = DbQueryExecutor.select(
    "SELECT * FROM users WHERE role = ?", "admin");
```

---

## Reports

- **ExtentReports HTML**: `reports/extent/TestReport_*.html`
- **Cucumber HTML**: `reports/cucumber/cucumber_report.html`
- **Cucumber JSON**: `reports/cucumber/cucumber_report.json`
- **TestNG XML**: `reports/test-output/testng-results.xml`
- **Screenshots**: `reports/test-output/screenshots/FAIL_*.png`

---

## CI / CD

### GitHub Actions

Triggers on push and pull request to `main`. Runs `mvn test` with Maven cache.

### Jenkins

```bash
mvn test -Denv=${ENVIRONMENT}
```

Use `Jenkinsfile` for declarative pipeline or configure as a Maven project pointing to `pom.xml`.

---

## Troubleshooting

### Browser driver not found

```bash
mvn webdrivermanager:setup
```

### Tests hang / timeout

Increase timeout in `config/env/config.dev.properties`:
```
app.timeout=60
```

### Screenshot not captured

Ensure `report.screenshot.onfailure=true` and `ScreenshotManager` has access to `WebDriver`.

### Cucumber steps not found

Verify the feature file `glue` path in `CucumberRunner` includes the step definition package.

---

## License

Internal use only.
