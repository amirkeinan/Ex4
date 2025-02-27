package assignments.ex2.ex2_sol;
/**
 * SCell – Cell Implementation.
 *
 * This class is a concrete implementation of the Cell interface. Each SCell stores:
 * - The raw input provided by the user (rawInput), which is preserved for editing.
 * - The current display value (_line) which may be updated to a computed result or an error message.
 * - A type field that indicates the cell's content type (e.g., TEXT, NUMBER, FORM, IF, IF_ERR, FUNCTION).
 *
 * Key functions include:
 * - setData(String s): Stores the raw input and determines the cell type.
 * - getData(): Returns the computed display value (or an error message, if applicable).
 * - getRawInput(): Returns the original user input (used when editing a cell).
 */

public class SCell implements Cell {
    private String _line;
    private int order =0;
    private String rawInput;
    int type = Ex2Utils.TEXT;
    public SCell() {this("");}
    public SCell(String s) {setData(s);}

    @Override
    public int getOrder() {
        return order;
    }

    //@Override
    @Override
    public String toString() {
        return getData();
    }

    @Override
    public void setData(String s) {
        if (s != null) {
            // save original input
            rawInput = s;
            type = Ex2Utils.TEXT;
            if (isNumber(s)) {
                type = Ex2Utils.NUMBER;
            }
            if (s.startsWith("=")) {
                type = Ex2Utils.FORM;
            }
            _line = s;
        }
    }
    @Override
    public String getData() {
        if (
                type == Ex2Utils.ERR_FORM_FORMAT ||
                type == Ex2Utils.ERR_CYCLE_FORM ||
                type == Ex2Utils.FUNC_ERR) {
            return Ex2Utils.ERR_FORM;
        } else if (type == Ex2Utils.IF_ERR) {
            return Ex2Utils.ERR_If;
        }
        return _line;
    }

    public String getRawInput() {
        return rawInput;
    }
    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        type = t;
    }

    @Override
    public void setOrder(int t) {
        this.order = t;
    }
    public static boolean isNumber(String line) {
        boolean ans = false;
        try {
            double v = Double.parseDouble(line);
            ans = true;
        }
        catch (Exception e) {;}
        return ans;
    }
}
