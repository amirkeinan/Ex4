package assignments.ex2.ex2_sol;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;
/**
 * Ex2Sheet – Spreadsheet Implementation.
 *
 * This class implements a simple spreadsheet. It maintains a 2D array of Cell objects
 * (using SCell as the concrete implementation) and supports various operations such as:
 * - Setting and retrieving cell values via set(x, y, s) and get(x, y).
 * - Evaluating formulas in cells using the eval() and eval(x, y) methods.
 * - Processing formulas including arithmetic expressions, IF formulas, and function calls.
 *
 * The class also manages two auxiliary arrays, 'data' (for numeric computed values)
 * and 'textValues' (for computed text values), which are used by the value(x, y) method
 * to determine what to display in the GUI.
 */

public class Ex2Sheet implements Sheet {
    private Cell[][] table;
    private Double[][] data;
    private String[][] textValues;

    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell("");
            }
        }
        data = new Double[x][y];
        textValues = new String[x][y]; // Initializing the textual result set
        eval();
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }
//value:The value(x,y) method is intended to display the value that appears in the GUI.
// If the calculated value (e.g., the result of a formula calculation) exists, it is displayed.
//Otherwise, the raw text is returned.
    @Override
    public String value(int x, int y) {
        Cell c = table[x][y];
        if (c == null) {
            return "";
        }
        //If there is a calculated value for the cell or text generated from the result of a formula then the function will return it,
        // otherwise it will return raw cell information.
        if (data != null && data[x][y] != null) {
            return String.valueOf(data[x][y]);
        }
        if (textValues != null && textValues[x][y] != null) {
            return textValues[x][y];
        }
        return c.getData();
    }

    @Override
    public Cell get(int x, int y) {
        return table[x][y];
    }

    @Override
    public Cell get(String cords) {
        Cell ans = null;
        Index2D c = new CellEntry(cords);
        int x = c.getX(), y= c.getY();
        if(isIn(x,y)) {ans = table[x][y];}
        return ans;
    }

    @Override
    public int width() {
        return table.length;
    }
    @Override
    public int height() {
        return table[0].length;
    }
    @Override

    public void set(int x, int y, String s) {

        if (table == null) {
            return;
        }

        // Clear previous calculation value for this cell
        data[x][y] = null;
        textValues[x][y] = null;

        Cell c = new SCell(s);
        if (c == null) {
            return;
        }


        // Input analysis: If the string starts with "=", check if it is an IF or another function
        if (s.startsWith("=")) {
            if (s.startsWith("=if(")) {
                //Remove the equal sign so that the string is obtained without the "="
                String ifFormula = s.substring(1);

                if (isValidIf(ifFormula, x, y)) {
                    c.setType(Ex2Utils.IF);
                } else {
                    c.setType(Ex2Utils.IF_ERR);
                }
            } else if (isFunction(s)) {
                c.setType(Ex2Utils.FUNCTION);
            } else {
                c.setType(Ex2Utils.FORM);
            }
        }

        table[x][y] = c;
    }








    ///////////////////////////////////////////////////////////
    //Run on all cells and call eval(x,y) to work on each of them.
    @Override
    public void eval() {
        int[][] dd = depth();
        data = new Double[width()][height()];
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                Cell c = table[x][y];
                if (c == null) continue;
                if (dd[x][y] != -1) {
                    String res = eval(x, y);

                    // If the cell is supposed to contain a number – we will try to convert
                    if (c.getType() == Ex2Utils.NUMBER || c.getType() == Ex2Utils.FORM || c.getType() == Ex2Utils.FUNCTION) {
                        try {
                            Double d = getDouble(res);
                            data[x][y] = d;
                        } catch (NumberFormatException e) {
                            c.setType(Ex2Utils.ERR_FORM_FORMAT);
                        }
                    }
                    // If the cell is text – no conversion
                }
                if (dd[x][y] == -1) {
                    c.setType(Ex2Utils.ERR_CYCLE_FORM);
                }
            }
        }
    }





    @Override
    public boolean isIn(int xx, int yy) {
        boolean ans = true;
        if(xx<0 |yy<0 | xx>=width() | yy>=height()) {ans = false;}
        return ans;
    }

    @Override
    public int[][] depth() {
        int[][] ans = new int[width()][height()];
        for (int x = 0; x < width(); x = x + 1) {
            for (int y = 0; y < height(); y = y + 1) {
                Cell c = this.get(x, y);
                int t = c.getType();
                if(Ex2Utils.TEXT!=t) {
                    ans[x][y] = -1;
                }
            }
        }
        int count = 0, all = width()*height();
        boolean changed = true;
        while (changed && count<all) {
            changed = false;
            for (int x = 0; x < width(); x = x + 1) {
                for (int y = 0; y < height(); y = y + 1) {
                    if(ans[x][y]==-1) {
                        Cell c = this.get(x, y);
                        //   ArrayList<Coord> deps = allCells(c.toString());
                        ArrayList<Index2D> deps = allCells(c.getData());
                        int dd = canBeComputed(deps, ans);
                        if (dd!=-1) {
                            ans[x][y] = dd;
                            count++;
                            changed = true;
                        }
                    }
                }
            }
        }
        return ans;
    }

    @Override
    public void load(String fileName) throws IOException {
        Ex2Sheet sp = new Ex2Sheet();
        File myObj = new File(fileName);
        Scanner myReader = new Scanner(myObj);
        String s0 = myReader.nextLine();
        if(Ex2Utils.Debug) {
            System.out.println("Loading file: "+fileName);
            System.out.println("File info (header:) "+s0);
        }
        while (myReader.hasNextLine()) {
            s0 = myReader.nextLine();
            String[] s1 = s0.split(",");
            try {
                int x = Ex2Sheet.getInteger(s1[0]);
                int y = Ex2Sheet.getInteger(s1[1]);
                //Solution to splitting a condition by "," instead of for loop:
                // combining the remaining array cells (except X,Y) into a single expression
                String res = Arrays.stream(s1,2,s1.length).collect(Collectors.joining(","));
                sp.set(x,y,res);
            }
            catch (Exception e) {
                e.printStackTrace();
                System.err.println("Line: "+data+" is in the wrong format (should be x,y,cellData)");
            }
        }
        sp.eval();
        table = sp.table;
        data = sp.data;
        textValues = sp.textValues;
    }

    @Override

    public void save(String fileName) throws IOException {
        FileWriter myWriter = new FileWriter(fileName);
        myWriter.write("I2CS ArielU: SpreadSheet (Ex2) assignment\n");

        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                Cell c = get(x, y);
                if (c != null && !c.getData().equals("")) {
                    myWriter.write(x + "," + y + "," + c.getData() + "\n");
                }
            }
        }
        myWriter.close();
    }


    private int canBeComputed(ArrayList<Index2D> deps, int[][] tmpTable) {
        int ans = 0;
        //try catch to keep from index exceptions
        try {
            for(int i=0;i<deps.size()&ans!=-1;i=i+1) {
                Index2D c = deps.get(i);
                int v = tmpTable[c.getX()][c.getY()];
                if(v==-1) {ans=-1;} // not yet computed;
                else {ans = Math.max(ans,v+1);}
            }
        } catch (Exception e) {
            return Ex2Utils.ERR_FORM_FORMAT;
        }

        return ans;
    }
    @Override
    public String eval(int x, int y) {
        Cell c = table[x][y];
        if (c == null) {
            return "";
        }
        int type = c.getType();

        // If it is text – directly return the original cell contents
        if (type == Ex2Utils.TEXT) {
            return c.getData();
        }

        /*
        If it is a number – we will check if there is a calculated value in the data array,
         and if not, we will return the original value.
         */
        if (type == Ex2Utils.NUMBER) {
            if (data != null && data[x][y] != null) {
                return String.valueOf(data[x][y]);
            } else {
                return c.getData();
            }
        }

        // If the cell type is IF_ERR – we will return ERR_IF immediately
        if (type == Ex2Utils.IF_ERR) {
            return Ex2Utils.ERR_If;
        }

        // for valid formulas (FORM, FUNCTION, IF)
        if (type == Ex2Utils.FORM || type == Ex2Utils.FUNCTION || type == Ex2Utils.IF) {
            Object result = computeForm(x, y);
            if (result == null) {
                c.setType(Ex2Utils.ERR_FORM_FORMAT);
                return Ex2Utils.ERR_FORM;
            }
            if (result instanceof String) {
                return (String) result;
            }
            if (result instanceof Double) {
                return String.valueOf(result);
            }
            c.setType(Ex2Utils.ERR_FORM_FORMAT);
            return Ex2Utils.ERR_FORM;
        }
        return Ex2Utils.ERR_FORM;
    }














    /////////////////////////////////////////////////
    public boolean isFunction(String s) {
        if (s.startsWith("=")) {
            s = s.substring(1); // cut "="
        }

        boolean result = false;
        for (String func : Ex2Utils.FUNCTIONS) {
            if (s.startsWith(func + "(")) {
                result = true;
                break;
            }
        }

        return result;
    }




    public static Integer getInteger(String line) {
        Integer ans = null;
        try {
            ans = Integer.parseInt(line);
        }
        catch (Exception e) {;}
        return ans;
    }
    public static Double getDouble(String line) {
        Double ans = null;
        try {
            ans= Double.parseDouble(line);
        }
        catch (Exception e) {;}
        return ans;
    }
    public static String removeSpaces(String s) {
        String ans = null;
        if (s!=null) {
            String[] words = s.split(" ");
            ans = new String();
            for(int i=0;i<words.length;i=i+1) {
                ans+=words[i];
            }
        }
        return ans;
    }

    public int checkType(String line) {
        line = removeSpaces(line);

        int ans = Ex2Utils.TEXT;

        if (isNumber(line)) {
            ans = Ex2Utils.NUMBER;
        } else if (line.startsWith("=")) {
            if (line.startsWith("=if(")) {
                ans = isValidIf(line.substring(1), -1, -1) ? Ex2Utils.IF : Ex2Utils.IF_ERR;
            }
            else {
                ans = Ex2Utils.FORM;
            }
        }

        return ans;
    }

    public boolean isForm(String form) {
        boolean ans = false;
        if(form!=null) {
            form = removeSpaces(form);
            try {
                ans = isFormP(form);
            }
            catch (Exception e) {;}
        }
        if (form.startsWith("if(")) {
            return true;
        }

        return ans;
    }
    private Object computeForm(int x, int y) {
        String form = table[x][y].getData();
        String procForm = form.substring(1); // cut the "="
        procForm = removeSpaces(procForm);

        Object result = computeFormP(procForm);
        if (result == null) {
            //Saving an error message in the textual result set
            textValues[x][y] = Ex2Utils.ERR_FORM;
            return Ex2Utils.ERR_FORM;
        }
        if (result instanceof String) {
            textValues[x][y] = (String) result;
            return (String) result;
        }
        if (result instanceof Double) {
            data[x][y] = (Double) result;
            return String.valueOf(result);
        }
        textValues[x][y] = Ex2Utils.ERR_FORM;
        return Ex2Utils.ERR_FORM;
    }








    private boolean isFormP(String form) {
        boolean ans = false;
        while(canRemoveB(form)) {
            form = removeB(form);//remove "()"
        }
        Index2D c = new CellEntry(form);
        if(isIn(c.getX(), c.getY())) {ans = true;}
        else{
            if(isNumber(form)){ans = true;}
            else {
                int ind = findLastOp(form);// bug
                if(ind==0) {  // the case of -1, or -(1+1)
                    char c1 = form.charAt(0);
                    if(c1=='-' | c1=='+') {
                        ans = isFormP(form.substring(1));}
                    else {ans = false;}
                }
                else {
                    String f1 = form.substring(0, ind);
                    String f2 = form.substring(ind + 1);
                    ans = isFormP(f1) && isFormP(f2);
                }
            }
        }
        return ans;
    }
    public static ArrayList<Index2D> allCells(String line) {
        ArrayList<Index2D> ans = new ArrayList<Index2D>();
        if (line == null) return ans;
        line = line.toUpperCase();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("([A-Z][0-9]+)");
        java.util.regex.Matcher matcher = pattern.matcher(line);
        while (matcher.find()) {
            String cellRef = matcher.group(1);
            Index2D idx = new CellEntry(cellRef);
            if (idx.isValid()) {
                ans.add(idx);
            }
        }
        return ans;
    }

private Object computeFormP(String form) {
    // Removing unnecessary spaces
    form = form.trim();

    // We'll check if the formula starts with one of the functions: min, max, sum, average.
    // If so, we'll send it to computeFunction which will return the calculated value.
    Object funcResult = computeFunction(form);
    if (funcResult != null) {
        return funcResult;
    }

    // cut the (=)
    if (form.startsWith("=")) {
        form = form.substring(1).trim();
    }

    // check if number
    if (isNumber(form)) {
        Double num = getDouble(form);
        return num;
    }

    // check if it texts
    if (form.startsWith("\"") && form.endsWith("\"")) {
        return form.substring(1, form.length() - 1);
    }

    // If conditions
    if (form.startsWith("if(") && form.endsWith(")")) {
        return evaluateIfFunction(form);
    }

    // referring to other cells-switch the cell to their numbers value
    java.util.regex.Pattern refPattern = java.util.regex.Pattern.compile("([A-Za-z][0-9]+)");
    java.util.regex.Matcher matcher = refPattern.matcher(form);
    while (matcher.find()) {
        String cellRefOrig = matcher.group(1);
        String cellRef = cellRefOrig.toUpperCase();
        Index2D idx = new CellEntry(cellRef);
        if (!isIn(idx.getX(), idx.getY())) {
            return null;
        }
        String refVal = this.value(idx.getX(), idx.getY());
        Double refNum = getDouble(refVal);
        if (refNum == null) {
            return null;
        }
        form = form.replaceAll("(?i)" + java.util.regex.Pattern.quote(cellRefOrig), refNum.toString());
    }

    // Recheck: If after swapping references the string is a number, we return the number
    if (isNumber(form)) {
        Double num = getDouble(form);
        return num;
    }

    // Removing unnecessary outer brackets
    while (canRemoveB(form)) {
        form = removeB(form);
    }

    //Arithmetic expression analysis – Finding a primary operator
    int opIndex = findLastOp(form);
    if (opIndex == -1) {
        // If no operator is found and there appear to be no operator characters, assume it is simple text.
        boolean hasOperator = false;
        for (String op : Ex2Utils.M_OPS) {
            if (form.contains(op)) {
                hasOperator = true;
                break;
            }
        }
        if (!hasOperator) {
            return form;
        }
        return null;
    }

    String leftStr = form.substring(0, opIndex);
    String rightStr = form.substring(opIndex + 1);
    char operator = form.charAt(opIndex);

    Object leftObj = computeFormP(leftStr);
    Object rightObj = computeFormP(rightStr);
    if (!(leftObj instanceof Double) || !(rightObj instanceof Double)) {
        return null;
    }
    double leftVal = (Double) leftObj;
    double rightVal = (Double) rightObj;
    Double result = null;
    switch (operator) {
        case '+': result = leftVal + rightVal; break;
        case '-': result = leftVal - rightVal; break;
        case '*': result = leftVal * rightVal; break;
        case '/':
            if (Math.abs(rightVal) < Ex2Utils.EPS) {
                return null;
            }
            result = leftVal / rightVal;
            break;
        default:
            return null;
    }
    return result;
}




/*splitIfArguments: split the arguments of given "if" function to three parts using
array list and stringBuilder method to make it simple.
 */
    private String[] splitIfArguments(String s) {
        java.util.ArrayList<String> parts = new java.util.ArrayList<>();
        int bracketCount = 0;
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            }
            if (!inQuotes) {
                if (c == '(') {
                    bracketCount++;
                } else if (c == ')') {
                    bracketCount--;
                } else if (c == ',' && bracketCount == 0) {
                    parts.add(current.toString().trim());
                    current.setLength(0);
                    continue;
                }
            }
            current.append(c);
        }
        parts.add(current.toString().trim());
        return parts.toArray(new String[0]);
    }
/**
evaluateIfFunction: the first "touch" the if condition. first check if it is
 a real if and then split it with splitIfArguments.
 then checks if it's referring to another cell and then other checks.
*/
    private Object evaluateIfFunction(String form) {
        if (!form.startsWith("if(") || !form.endsWith(")")) {
            return Ex2Utils.IF_ERR;
        }
        String inner = form.substring(3, form.length()-1).trim();
        String[] parts = splitIfArguments(inner);
        if (parts.length != 3) {
            return Ex2Utils.IF_ERR;
        }
        String condition = parts[0];
        String ifTrue = parts[1];
        String ifFalse = parts[2];


        //checks if referring to another cell using regex method
        if (ifTrue.matches("^[A-Za-z]+[0-9]+$") || ifFalse.matches("^[A-Za-z]+[0-9]+$")) {
            return Ex2Utils.IF_ERR;
        }

        Boolean condResult = evaluateCondition(condition);
        if (condResult == null) {
            return Ex2Utils.IF_ERR;
        }
        // if condResult is true it means that the condition is true therefore we will call computeFormP(ifTrue)
        // if it false we will call computeFormP with the "if_false".
        Object chosenResult = condResult ? computeFormP(ifTrue) : computeFormP(ifFalse);
        return chosenResult;
    }



/*
evaluateCondition: checks the condition itself (a1>a2 for example) by "translate"
the parts of the condition to numbers.
 */
    private Boolean evaluateCondition(String condition) {
        String[] ops = {"<=", ">=", "==", "!=", "<", ">"};
        for (String op : ops) {
            if (condition.contains(op)) {
                String[] parts = condition.split(java.util.regex.Pattern.quote(op));
                if (parts.length != 2) {
                    return null; // wrong format
                }
                Object leftObj = computeFormP(parts[0].trim());
                Object rightObj = computeFormP(parts[1].trim());
                if (!(leftObj instanceof Double) || !(rightObj instanceof Double)) {
                    return null; //also wrong format
                }
                Double left = (Double) leftObj;
                Double right = (Double) rightObj;
                switch (op) {
                    case "<=": return left <= right;
                    case ">=": return left >= right;
                    case "==": return left.equals(right);
                    case "!=": return !left.equals(right);
                    case "<": return left < right;
                    case ">": return left > right;
                    default: return null;
                }
            }
        }
        return null;
    }
/**
* isValidIf: checks given "if" validation
Condition: has to contain three arguments and start with "if" and end with ")"
 * condition should be able to evaluate and iftrue, iffalse should be proper expression
 and not self-referencing .
 */

    private boolean isValidIf(String form, int cellX, int cellY) {
        form = form.trim();

        // base check: start with if and end with )
        if (!form.startsWith("if(") || !form.endsWith(")")) {
            return false;
        }

        String inner = form.substring(3, form.length() - 1).trim();
        String[] parts = splitIfArguments(inner);
        if (parts.length != 3) {
            return false;
        }

        String condition = parts[0].trim();
        String ifTrue = parts[1].trim();
        String ifFalse = parts[2].trim();

        // condition check by evaluateCondition()
        Boolean condResult = evaluateCondition(condition);
        if (condResult == null) {
            return false;
        }

        //  ifTrue, ifFalse arguments checking
        if (!isValidExpressionForArgument(ifTrue) || !isValidExpressionForArgument(ifFalse)) {
            return false;
        }

        // self-referencing check
        String cellRef = Ex2Utils.ABC[cellX].toUpperCase() + cellY;  //For example "A0" for cell A0
        if (condition.toUpperCase().contains(cellRef) ||
                ifTrue.toUpperCase().contains(cellRef) ||
                ifFalse.toUpperCase().contains(cellRef)) {
            return false;
        }

        return true;
    }


/**
* containsSelfReference: checks if condition contain self reference.
*/


    private boolean containsSelfReference(String formula, int x, int y) {
        String cellReference = Ex2Utils.ABC[x].toUpperCase() + y;
        return formula.toUpperCase().contains(cellReference);
    }


    private boolean isValidCondition(String condition) {
        for (String op : Ex2Utils.B_OPS) {
            if (condition.contains(op)) {
                String[] parts = condition.split(op);
                if (parts.length != 2) return false;

                //Checking that both sides are valid expressions
                return isValidExpression(parts[0].trim()) && isValidExpression(parts[1].trim());
            }
        }
        return false;
    }
    /**
     * isValidExpression: Checks whether an expression is a formula, a number
     * , or text enclosed in quotation marks.
     */
    private boolean isValidExpression(String expr) {
        if (expr.startsWith("=")) {
            return true; // it is a formula
        }
        return isNumber(expr) || expr.startsWith("\"") && expr.endsWith("\""); // number or text
    }
    private boolean isValidExpressionForArgument(String expr) {
        if (expr.startsWith("=") || expr.startsWith("if("))
            return true;
        if (isNumber(expr))
            return true;
        if (expr.startsWith("\"") && expr.endsWith("\""))
            return true;
        // check if the argument is a cell
        if (expr.matches("^[A-Za-z]+[0-9]+$"))
            return false;
        return !expr.isEmpty();
    }
    private Object computeFunction(String form) {
        form = form.trim();
        // cut the (=)
        if (form.startsWith("=")) {
            form = form.substring(1).trim();
        }
        try {
            if(form.startsWith("min(") && form.endsWith(")")) {
                String rangeStr = form.substring(4, form.length()-1).trim();
                Range2D range = new Range2D(rangeStr);
                return RangeFunctions.min(this, range);
            }
            if(form.startsWith("max(") && form.endsWith(")")) {
                String rangeStr = form.substring(4, form.length()-1).trim();
                Range2D range = new Range2D(rangeStr);
                return RangeFunctions.max(this, range);
            }
            if(form.startsWith("sum(") && form.endsWith(")")) {
                String rangeStr = form.substring(4, form.length()-1).trim();
                Range2D range = new Range2D(rangeStr);
                return RangeFunctions.sum(this, range);
            }
            if(form.startsWith("average(") && form.endsWith(")")) {
                String rangeStr = form.substring(8, form.length()-1).trim();
                Range2D range = new Range2D(rangeStr);
                return RangeFunctions.average(this, range);
            }
        } catch(Exception e) {
            // in case of invalid input return Func Eror
            return Ex2Utils.FUNC_EROR;
        }
        return null;
    }


    private static int opCode(String op){
        int ans =-1;
        for(int i = 0; i< Ex2Utils.M_OPS.length; i=i+1) {
            if(op.equals(Ex2Utils.M_OPS[i])) {ans=i;}
        }
        return ans;
    }
    private static int findFirstOp(String form) {
        int ans = -1;
        int s1=0,max=-1;
        for(int i=0;i<form.length();i++) {
            char c = form.charAt(i);
            if(c==')') {s1--;}
            if(c=='(') {s1++;}
            int op = op(form, Ex2Utils.M_OPS, i);
            if(op!=-1){
                if(s1>max) {max = s1;ans=i;}
            }
        }
        return ans;
    }
    public static int findLastOp(String form) {
        int ans = -1;
        double s1=0,min=-1;
        for(int i=0;i<form.length();i++) {
            char c = form.charAt(i);
            if(c==')') {s1--;}
            if(c=='(') {s1++;}
            int op = op(form, Ex2Utils.M_OPS, i);
            if(op!=-1){
                double d = s1;
                if(op>1) {d+=0.5;}
                if(min==-1 || d<=min) {min = d;ans=i;}
            }
        }
        return ans;
    }
    private static String removeB(String s) {
        if (canRemoveB(s)) {
            s = s.substring(1, s.length() - 1);
        }
        return s;
    }
    private static boolean canRemoveB(String s) {
        boolean ans = false;
        if (s!=null && s.startsWith("(") && s.endsWith(")")) {
            ans = true;
            int s1 = 0, max = -1;
            for (int i = 0; i < s.length()-1; i++) {
                char c = s.charAt(i);
                if (c == ')') {
                    s1--;
                }
                if (c == '(') {
                    s1++;
                }
                if (s1 < 1) {
                    ans = false;
                }
            }
        }
        return ans;
    }
    private static int op(String line, String[] words, int start) {
        int ans = -1;
        line = line.substring(start);
        for(int i = 0; i<words.length&&ans==-1; i++) {
            if(line.startsWith(words[i])) {
                ans=i;
            }
        }
        return ans;
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