package assignments.ex2.ex2_sol;

import java.util.ArrayList;

public class Range2D {
    private int colStart, colEnd, rowStart, rowEnd;

    /**
     * Constructor that parses a range string in the format "A1:C5".
     * Assumes first cell is top-left and second is bottom-right.
     */
    public Range2D(String rangeStr) {
        String[] parts = rangeStr.split(":");
        if(parts.length != 2) {
            throw new IllegalArgumentException("Invalid range format: " + rangeStr);
        }
        CellEntry start = new CellEntry(parts[0].trim());
        CellEntry end = new CellEntry(parts[1].trim());
        this.colStart = Math.min(start.getX(), end.getX());
        this.colEnd = Math.max(start.getX(), end.getX());
        this.rowStart = Math.min(start.getY(), end.getY());
        this.rowEnd = Math.max(start.getY(), end.getY());
    }

    public int getColStart() { return colStart; }
    public int getColEnd() { return colEnd; }
    public int getRowStart() { return rowStart; }
    public int getRowEnd() { return rowEnd; }

    /**
     * Returns all cells (as Index2D objects) within the range.
     */
    public ArrayList<Index2D> getAllCells() {
        ArrayList<Index2D> list = new ArrayList<>();
        for (int col = colStart; col <= colEnd; col++) {
            for (int row = rowStart; row <= rowEnd; row++) {
                // Construct cell reference using Ex2Utils.ABC for column letter.
                String cellRef = Ex2Utils.ABC[col] + row;
                list.add(new CellEntry(cellRef));
            }
        }
        return list;
    }
}
