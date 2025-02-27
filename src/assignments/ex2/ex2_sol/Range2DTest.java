package assignments.ex2.ex2_sol;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

/**
 * Tests for the Range2D class.
 */
public class Range2DTest {

    @Test
    public void testRangeParsing() {
        // Create a range "A1:C5". Assuming that A corresponds to column index 0.
        Range2D range = new Range2D("A1:C5");
        // For "A1:C5", we expect colStart = 0, colEnd = 2 (A, B, C) and rowStart = 1, rowEnd = 5.
        assertEquals(0, range.getColStart(), "colStart should be 0 for 'A1:C5'");
        assertEquals(2, range.getColEnd(), "colEnd should be 2 for 'A1:C5'");
        assertEquals(1, range.getRowStart(), "rowStart should be 1 for 'A1:C5'");
        assertEquals(5, range.getRowEnd(), "rowEnd should be 5 for 'A1:C5'");
    }

    @Test
    public void testGetAllCells() {
        Range2D range = new Range2D("A1:C5");
        ArrayList<Index2D> cells = range.getAllCells();
        // Expecting 3 columns * 5 rows = 15 cells.
        assertEquals(15, cells.size(), "Range A1:C5 should contain 15 cells.");
        // Check that the first cell is "A1" and the last cell is "C5"
        CellEntry first = new CellEntry("A1");
        CellEntry last = new CellEntry("C5");
        // Compare using toString() – assuming that CellEntry.toString() returns the cell reference.
        assertEquals(first.toString(), cells.get(0).toString(), "First cell should be A1.");
        assertEquals(last.toString(), cells.get(cells.size() - 1).toString(), "Last cell should be C5.");
    }
}
