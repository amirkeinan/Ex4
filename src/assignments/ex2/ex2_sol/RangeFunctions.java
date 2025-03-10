package assignments.ex2.ex2_sol;

import java.util.ArrayList;
/**
 * RangeFunctions – Range-based Function Calculations.
 *
 * This class provides static methods for performing calculations over a range of cells.
 * The supported functions include:
 * - min(Ex2Sheet sheet, Range2D range): Returns the minimum numeric value in the range.
 * - max(Ex2Sheet sheet, Range2D range): Returns the maximum numeric value in the range.
 * - sum(Ex2Sheet sheet, Range2D range): Computes the sum of all numeric values in the range.
 * - average(Ex2Sheet sheet, Range2D range): Computes the average of the numeric values in the range.
 *
 * These functions work by iterating over all cell references provided by Range2D and using
 * the Ex2Sheet’s value() method to retrieve and convert cell contents to numbers.
 */

public class RangeFunctions {
    /**
     * Returns the minimum value among cells in the given range.
     */
    public static Double min(Ex2Sheet sheet, Range2D range) {
        ArrayList<Index2D> cells = range.getAllCells();
        Double min = null;
        for (Index2D idx : cells) {
            int x = idx.getX(), y = idx.getY();
            String valStr = sheet.value(x, y);
            Double val = Ex2Sheet.getDouble(valStr);
            if (val != null) {
                if (min == null || val < min) {
                    min = val;
                }
            }
        }
        return min;
    }

    /**
     * Returns the maximum value among cells in the given range.
     */
    public static Double max(Ex2Sheet sheet, Range2D range) {
        ArrayList<Index2D> cells = range.getAllCells();
        Double max = null;
        for (Index2D idx : cells) {
            int x = idx.getX(), y = idx.getY();
            String valStr = sheet.value(x, y);
            Double val = Ex2Sheet.getDouble(valStr);
            if (val != null) {
                if (max == null || val > max) {
                    max = val;
                }
            }
        }
        return max;
    }

    /**
     * Returns the sum of values among cells in the given range.
     */
    public static Double sum(Ex2Sheet sheet, Range2D range) {
        ArrayList<Index2D> cells = range.getAllCells();
        double total = 0;
        boolean found = false;
        for (Index2D idx : cells) {
            int x = idx.getX(), y = idx.getY();
            String valStr = sheet.value(x, y);
            Double val = Ex2Sheet.getDouble(valStr);
            if (val != null) {
                total += val;
                found = true;
            }
        }
        return found ? total : null;// if found true return total. else null
    }

    /**
     * Returns the average of values among cells in the given range.
     */
    public static Double average(Ex2Sheet sheet, Range2D range) {
        ArrayList<Index2D> cells = range.getAllCells();
        double total = 0;
        int count = 0;
        for (Index2D idx : cells) {
            int x = idx.getX(), y = idx.getY();
            String valStr = sheet.value(x, y);
            Double val = Ex2Sheet.getDouble(valStr);
            if (val != null) {
                total += val;
                count++;
            }
        }
        return count > 0 ? (total / count) : null;// if count >0 return avg(total/count). else null
    }
}
