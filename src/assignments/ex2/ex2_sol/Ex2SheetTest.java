package assignments.ex2.ex2_sol;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Ex2SheetTest verifies the main functionalities of the Ex2Sheet class.
 * It tests cell input (plain text, numbers, formulas), IF formulas (valid and self-referential),
 * range functions, and the load() method.
 */
public class Ex2SheetTest {

    private Ex2Sheet sheet;

    @BeforeEach
    public void setUp() {
        // Create a 10x10 sheet for testing.
        sheet = new Ex2Sheet(10, 10);
    }

    @Test
    @DisplayName("Plain text cell returns its text")
    public void plainTextCellReturnsText() {
        sheet.set(0, 0, "Hello World");
        assertEquals("Hello World", sheet.value(0, 0), "Cell (0,0) should return 'Hello World'");
    }

    @Test
    @DisplayName("Number cell returns its input if no formula evaluation")
    public void numberCellReturnsNumber() {
        sheet.set(1, 1, "123.45");
        // For a number cell, if no formula evaluation occurs, value should equal the raw text.
        assertEquals("123.45", sheet.value(1, 1), "Cell (1,1) should return '123.45'");
    }

    @Test
    @DisplayName("Arithmetic formula evaluates correctly")
    public void arithmeticFormulaEvaluation() {
        sheet.set(2, 2, "=4*5");
        sheet.eval();
        // Expected: 4 * 5 = 20.0
        assertEquals("20.0", sheet.value(2, 2), "Cell (2,2) should evaluate to '20.0'");
    }

    @Test
    @DisplayName("Valid IF formula evaluates correctly")
    public void validIfFormulaEvaluation() {
        // Set cell A1 (0,1) with a number so that a condition can be evaluated.
        sheet.set(0, 1, "9");
        // Use a valid IF formula in cell (3,3): condition "a1>2" (i.e. 9 > 2 is true)
        sheet.set(3, 3, "=if(a1>2,2,4)");
        sheet.eval();
        // Since the condition is true, the expected result is 2.0.
        assertEquals("2.0", sheet.value(3, 3), "Cell (3,3) should evaluate to '2.0'");
    }

    @Test
    @DisplayName("Self-referential IF formula returns error")
    public void selfReferentialIfFormulaError() {
        // In cell A0 (0,0) insert a self-referential IF formula.
        sheet.set(0, 0, "=if(a0>3,2,4)");
        sheet.eval();
        // Self-reference should cause the cell to be classified as IF_ERR and return ERR_FORM.
        assertEquals(Ex2Utils.ERR_If, sheet.value(0, 0),
                "Self-referential IF formula in cell A0 should return ERR_FORM");
    }

    @Test
    @DisplayName("Invalid range function returns error")
    public void invalidRangeFunctionError() {
        // An invalid range function (e.g. =sum(A1) instead of a proper range like A1:B1)
        sheet.set(4, 4, "=sum(A1)");
        sheet.eval();
        // Expected: invalid input causes error output.
        assertEquals(Ex2Utils.FUNC_EROR, sheet.value(4, 4),
                "Invalid range function should return ERR_FORM");
    }

    @Test
    @DisplayName("Valid range function (sum) evaluates correctly")
    public void validRangeFunctionSum() {
        // Set cells A0, B0, and C0 with numeric values.
        sheet.set(0, 0, "10");
        sheet.set(1, 0, "20");
        sheet.set(2, 0, "30");
        sheet.eval();
        // Now set a cell with a sum formula over the range "A0:C0".
        sheet.set(3, 0, "=sum(A0:C0)");
        sheet.eval();
        // Expected sum: 10+20+30 = 60.0.
        assertEquals("60.0", sheet.value(3, 0),
                "Cell (3,0) should evaluate to '60.0' as the sum of A0, B0, and C0");
    }

    @Test
    @DisplayName("Load file populates sheet correctly")
    public void loadFileTest() throws IOException {
        // Create a temporary file with sample cell data.
        File tempFile = File.createTempFile("testSheet", ".txt");
        tempFile.deleteOnExit();
        FileWriter writer = new FileWriter(tempFile);
        writer.write("I2CS ArielU: SpreadSheet (Ex2) assignment\n");
        writer.write("0,0,Hello\n");
        writer.write("1,1,=4*5\n");      // Expected: 20.0
        writer.write("2,2,=if(1<2,100,200)\n"); // Expected: 100.0
        writer.close();

        // Load the file into the sheet.
        sheet.load(tempFile.getAbsolutePath());
        sheet.eval();

        assertEquals("Hello", sheet.value(0, 0), "Cell (0,0) should contain 'Hello' after loading.");
        assertEquals("20.0", sheet.value(1, 1), "Cell (1,1) should evaluate to '20.0' after loading.");
        assertEquals("100.0", sheet.value(2, 2), "Cell (2,2) should evaluate to '100.0' after loading.");
    }
}
