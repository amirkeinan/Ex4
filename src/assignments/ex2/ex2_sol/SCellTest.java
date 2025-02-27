package assignments.ex2.ex2_sol;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * Tests for the SCell class.
 */
public class SCellTest {

    @Test
    public void testSetDataAndRawInput() {
        SCell cell = new SCell();
        cell.setData("=if(1<2,good,bad)");
        // Verify that the raw input is preserved.
        assertEquals("=if(1<2,good,bad)", cell.getRawInput(),
                "Raw input should match the input string.");
    }

    @Test
    public void testGetDataForTextCell() {
        SCell cell = new SCell("Hello");
        // For a text cell, getData returns the original text.
        assertEquals("Hello", cell.getData(), "getData should return 'Hello' for a text cell.");
    }

    @Test
    public void testGetDataForErrorCell() {
        SCell cell = new SCell("=if(a0>3,2,4)");
        // Simulate that the cell is flagged with an error.
        cell.setType(Ex2Utils.IF_ERR);
        assertEquals(Ex2Utils.ERR_If, cell.getData(),
                "For an error cell, getData should return ERR_FORM.");
    }

    @Test
    public void testIsNumber() {
        assertTrue(SCell.isNumber("123"), "123 should be recognized as a number.");
        assertFalse(SCell.isNumber("abc"), "'abc' should not be recognized as a number.");
    }
}
