package com.company.automation.components;

import com.company.automation.core.exception.ElementNotFoundException;
import com.company.automation.core.logging.AutomationLogger;
import com.company.automation.core.wait.WaitConditions;
import com.company.automation.core.wait.WaitFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable table fragment helper.
 * Supports:
 * - Row / cell retrieval by index
 * - Cell retrieval by row index + column index
 * - Header-based column lookup
 * - Row count and data retrieval
 */
public class TableComponent extends BaseComponent {

    private final By tableLocator;
    private final By tbodyLocator;
    private final By headerLocator;

    public TableComponent(WebDriver driver, By tableLocator) {
        super(driver);
        this.tableLocator = tableLocator;
        this.tbodyLocator = By.xpath("./tbody");
        this.headerLocator = By.xpath(".//th");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // STRUCTURE
    // ══════════════════════════════════════════════════════════════════════════

    /** Returns the table WebElement. */
    protected WebElement table() {
        return WaitFactory.wait(driver)
                .until(WaitConditions.visible(tableLocator));
    }

    /** Returns tbody rows. Falls back to direct tr children if no tbody. */
    protected List<WebElement> rows() {
        WebElement tbl = table();
        List<WebElement> rows = tbl.findElements(tbodyLocator);
        if (rows.isEmpty()) {
            rows = tbl.findElements(By.xpath("./tr"));
        }
        return rows;
    }

    /** Returns all header cells (th). */
    protected List<String> headers() {
        WebElement tbl = table();
        List<WebElement> ths = tbl.findElements(headerLocator);
        List<String> result = new ArrayList<>();
        for (WebElement th : ths) {
            result.add(th.getText().trim());
        }
        return result;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ROW / CELL ACCESS
    // ══════════════════════════════════════════════════════════════════════════

    /** Returns the row WebElement at 0-based index (excludes header). */
    protected WebElement getRow(int rowIndex) {
        List<WebElement> dataRows = rows();
        int idx = rowIndex < 0 ? 0 : Math.min(rowIndex, dataRows.size() - 1);
        return dataRows.get(idx);
    }

    /** Returns a specific cell. */
    protected WebElement getCell(int rowIndex, int colIndex) {
        WebElement row = getRow(rowIndex);
        List<WebElement> cells = row.findElements(By.tagName("td"));
        if (colIndex < 0 || colIndex >= cells.size()) {
            throw new ElementNotFoundException(
                    "Cell [" + rowIndex + "][" + colIndex + "] outside bounds. Columns: " + cells.size());
        }
        return cells.get(colIndex);
    }

    /** Returns cell text. */
    protected String getCellText(int rowIndex, int colIndex) {
        return getCell(rowIndex, colIndex).getText().trim();
    }

    /** Returns the index of a column by its header text. */
    protected int getColumnIndex(String headerText) {
        List<String> hdrs = headers();
        for (int i = 0; i < hdrs.size(); i++) {
            if (hdrs.get(i).equalsIgnoreCase(headerText.trim())) {
                return i;
            }
        }
        throw new ElementNotFoundException("Column header not found: " + headerText);
    }

    /** Returns cell text using header name. */
    protected String getCellByColumnName(int rowIndex, String columnName) {
        int colIndex = getColumnIndex(columnName);
        return getCellText(rowIndex, colIndex);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TABLE OPERATIONS
    // ══════════════════════════════════════════════════════════════════════════

    /** Clicks a link/button inside a cell. */
    protected void clickCell(int rowIndex, int colIndex) {
        WebElement cell = getCell(rowIndex, colIndex);
        WebElement clickable = WaitFactory.wait(driver)
                .until(WaitConditions.clickable(cell));
        clickable.click();
        AutomationLogger.debug("Clicked cell [{][}]", rowIndex, colIndex);
    }

    /** Waits until the table has at least N rows (excludes header). */
    protected void waitForRowCount(int minRows) {
        WaitFactory.wait(driver).until(d -> {
            List<WebElement> dataRows = rows();
            return dataRows.size() >= minRows;
        });
    }

    /** Returns all row texts as a list of string arrays. */
    protected List<String[]> getAllRowData() {
        List<String[]> result = new ArrayList<>();
        List<WebElement> dataRows = rows();
        for (WebElement row : dataRows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            String[] rowData = new String[cells.size()];
            for (int i = 0; i < cells.size(); i++) {
                rowData[i] = cells.get(i).getText().trim();
            }
            result.add(rowData);
        }
        return result;
    }

    /** Returns total data row count (excludes header). */
    protected int rowCount() {
        return rows().size();
    }
}
