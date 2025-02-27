package assignments.ex2.ex2_sol;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for the RangeFunctions class.
 */
public class RangeFunctionsTest {

    @Test
    public void testMinFunction() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        // Set some numeric values
        sheet.set(0, 0, "10");
        sheet.set(1, 0, "5");
        sheet.set(2, 0, "20");
        sheet.eval();
        // Create a range covering these cells; note that cell indices in Range2D depend on your implementation.
        Range2D range = new Range2D("A0:C0");
        Double min = RangeFunctions.min(sheet, range);
        assertEquals(5.0, min, "Minimum should be 5.0");
    }

    @Test
    public void testMaxFunction() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "10");
        sheet.set(1, 0, "5");
        sheet.set(2, 0, "20");
        sheet.eval();
        Range2D range = new Range2D("A0:C0");
        Double max = RangeFunctions.max(sheet, range);
        assertEquals(20.0, max, "Maximum should be 20.0");
    }

    @Test
    public void testSumFunction() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "10");
        sheet.set(1, 0, "5");
        sheet.set(2, 0, "20");
        sheet.eval();
        Range2D range = new Range2D("A0:C0");
        Double sum = RangeFunctions.sum(sheet, range);
        assertEquals(35.0, sum, "Sum should be 35.0");
    }

    @Test
    public void testAverageFunction() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "10");
        sheet.set(1, 0, "5");
        sheet.set(2, 0, "20");
        sheet.eval();
        Range2D range = new Range2D("A0:C0");
        Double avg = RangeFunctions.average(sheet, range);
        assertEquals(35.0 / 3, avg, 0.0001, "Average should be approximately 11.6667");
    }
}
