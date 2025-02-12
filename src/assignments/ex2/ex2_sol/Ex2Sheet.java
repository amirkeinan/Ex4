package assignments.ex2.ex2_sol;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The documentation of this class was removed as of Ex4...
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
        textValues = new String[x][y]; // אתחול מערך התוצאות הטקסטואליות
        eval();
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        Cell c = table[x][y];
        if (c == null) {
            return "";
        }
        if (data != null && data[x][y] != null) {
            return String.valueOf(data[x][y]);
        }
        if (textValues != null && textValues[x][y] != null) {
            return textValues[x][y];
        }
        return c.getData();
    }












    /*public String value(int x, int y) {
        if (!isIn(x, y)) return "";

        Cell c = table[x][y];

        if (c.getType() == Ex2Utils.TEXT) {
            return c.getData();  // טקסט מוחזר ישירות
        }

        if (c.getType() == Ex2Utils.NUMBER) {
            return String.valueOf(data[x][y]);  // מספר מוחזר כמחרוזת
        }

        if (c.getType() == Ex2Utils.FORM) {
            Object result = computeForm(x, y); // קבלת הערך שחושב

            if (result instanceof String) {
                return (String) result;  // החזרת טקסט תקין
            }

            if (result instanceof Double) {
                return String.valueOf(result);
            }

            return Ex2Utils.ERR_FORM;
        }

        return Ex2Utils.ERR_FORM;
    }*/











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
        System.out.println("🔍 DEBUG: set() called with x=" + x + ", y=" + y + ", s=" + s);

        if (table == null) {
            System.out.println("❌ ERROR: table is NULL!");
            return;
        }

        // --- ניקוי הערכים המחושבים הקודמים עבור תא זה ---
        // הנח שהמערכים data ו-textValues מאותחלים בגודל של הטבלה.
        data[x][y] = null;
        textValues[x][y] = null;

        // יצירת תא חדש עם הנתון שהוזן
        Cell c = new SCell(s);
        if (c == null) {
            System.out.println("❌ ERROR: Created cell is NULL!");
            return;
        }

        System.out.println("🔍 DEBUG: Created cell, type before check: " + c.getType());

        // ניתוח הקלט: אם המחרוזת מתחילה ב־"=" אז בודקים האם מדובר ב־IF או בפונקציה אחרת
        if (s.startsWith("=")) {
            if (s.startsWith("=if(")) {
                // מסירים את סימן השוויון, כך מתקבלת המחרוזת ללא "="
                String ifFormula = s.substring(1);
                if (isValidIf(ifFormula)) {
                    c.setType(Ex2Utils.IF);
                    System.out.println("✅ DEBUG: Cell type set to IF");
                } else {
                    c.setType(Ex2Utils.IF_ERR);
                    System.out.println("❌ DEBUG: Invalid IF formula, cell type set to IF_ERR");
                }
            } else if (isFunction(s)) {
                c.setType(Ex2Utils.FUNCTION);
                System.out.println("✅ DEBUG: Cell type set to FUNCTION");
            } else {
                c.setType(Ex2Utils.FORM);
                System.out.println("✅ DEBUG: Cell type set to FORM");
            }
        }

        table[x][y] = c;
        System.out.println("🔍 DEBUG: Cell type after set: " + c.getType());
    }







    ///////////////////////////////////////////////////////////

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
                    System.out.println("🔍 DEBUG: eval(" + x + "," + y + ") returned: " + res + " with cell type: " + c.getType());

                    // אם התא אמור להכיל מספר – ננסה להמיר
                    if (c.getType() == Ex2Utils.NUMBER || c.getType() == Ex2Utils.FORM || c.getType() == Ex2Utils.FUNCTION) {
                        try {
                            Double d = Double.parseDouble(res);
                            data[x][y] = d;
                        } catch (NumberFormatException e) {
                            System.out.println("DEBUG: NumberFormatException for cell (" + x + "," + y + "): " + res);
                            c.setType(Ex2Utils.ERR_FORM_FORMAT);
                        }
                    }
                    // אם התא הוא טקסט – אין המרה
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
                sp.set(x,y,s1[2]);
            }
            catch (Exception e) {
                e.printStackTrace();
                System.err.println("Line: "+data+" is in the wrong format (should be x,y,cellData)");
            }
        }
        sp.eval();
        table = sp.table;
        data = sp.data;
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
        for(int i=0;i<deps.size()&ans!=-1;i=i+1) {
            Index2D c = deps.get(i);
            int v = tmpTable[c.getX()][c.getY()];
            if(v==-1) {ans=-1;} // not yet computed;
            else {ans = Math.max(ans,v+1);}
        }
        return ans;
    }
    @Override
    public String eval(int x, int y) {
        System.out.println("🔍 DEBUG: eval(" + x + ", " + y + ") called");
        Cell c = table[x][y];
        if (c == null) {
            System.out.println("❌ DEBUG: NULL cell in eval(" + x + "," + y + ")");
            return "";
        }
        System.out.println("🔍 DEBUG: eval -> Cell content: " + c.getData());
        int type = c.getType();

        // אם לא מדובר בנוסחה – נחזיר את תוכן התא המקורי
        if (type == Ex2Utils.TEXT) {
            System.out.println("✅ DEBUG: eval returning TEXT: " + c.getData());
            return c.getData();
        }

        // אם מדובר במספר – נשתמש במערך data (אם כבר חושב)
        if (type == Ex2Utils.NUMBER) {
            System.out.println("✅ DEBUG: eval returning NUMBER: " + data[x][y]);
            return String.valueOf(data[x][y]);
        }

        // עבור נוסחאות (FORM, FUNCTION, IF וכו')
        if (type == Ex2Utils.FORM || type == Ex2Utils.FUNCTION || type == Ex2Utils.IF || type == Ex2Utils.IF_ERR) {
            System.out.println("🔍 DEBUG: Calling computeForm with: " + c.getData());
            Object result = computeForm(x, y);
            if (result == null) {
                System.out.println("❌ DEBUG: eval returned NULL, setting error.");
                return Ex2Utils.ERR_FORM;
            }
            if (result instanceof String) {
                System.out.println("✅ DEBUG: eval returning TEXT: " + result);
                return (String) result;
            }
            if (result instanceof Double) {
                System.out.println("✅ DEBUG: eval returning NUMBER: " + result);
                return String.valueOf(result);
            }
            System.out.println("❌ DEBUG: Unexpected type in eval: " + result);
            return Ex2Utils.ERR_FORM;
        }
        return Ex2Utils.ERR_FORM;
    }













    /////////////////////////////////////////////////
    public boolean isFunction(String s) {
        if (s.startsWith("=")) {
            s = s.substring(1); // מסירים את סימן השוויון
        }

        boolean result = false;
        for (String func : Ex2Utils.FUNCTIONS) {
            if (s.startsWith(func + "(")) {
                result = true;
                break;
            }
        }

        System.out.println("🔍 DEBUG: isFunction(" + s + ") = " + result);
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
        System.out.println("🔍 DEBUG: checkType() called with: " + line);

        int ans = Ex2Utils.TEXT;

        if (isNumber(line)) {
            ans = Ex2Utils.NUMBER;
        } else if (line.startsWith("=")) {
            if (line.startsWith("=if(")) {
                ans = isValidIf(line.substring(1)) ? Ex2Utils.IF : Ex2Utils.IF_ERR;
            } else {
                ans = Ex2Utils.FORM;
            }
        }
        System.out.println("🔍 DEBUG: checkType called with: " + line + " -> Returning type: " + ans);

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
        System.out.println("🔍 DEBUG: computeForm called for (" + x + ", " + y + ")");
        String form = table[x][y].getData();
        System.out.println("🔍 DEBUG: Raw formula content: " + form);
        // שמירת הפורמולה המקורית לא משתנה – לא מעדכנים אותה
        String procForm = form.substring(1); // מסירים "="
        procForm = removeSpaces(procForm);
        System.out.println("🔍 DEBUG: Processed formula: " + procForm);

        Object result = computeFormP(procForm);
        if (result == null) {
            System.out.println("❌ DEBUG: computeFormP returned null, setting error.");
            // שמירת הודעת שגיאה במערך התוצאות הטקסטואליות
            textValues[x][y] = Ex2Utils.ERR_FORM;
            return Ex2Utils.ERR_FORM;
        }
        if (result instanceof String) {
            System.out.println("✅ DEBUG: computeForm returning TEXT: " + result);
            textValues[x][y] = (String) result;
            return (String) result;
        }
        if (result instanceof Double) {
            System.out.println("✅ DEBUG: computeForm returning NUMBER: " + result);
            data[x][y] = (Double) result;
            return String.valueOf(result);
        }
        System.out.println("❌ DEBUG: Unexpected type in computeForm: " + result);
        textValues[x][y] = Ex2Utils.ERR_FORM;
        return Ex2Utils.ERR_FORM;
    }








    private boolean isFormP(String form) {
        boolean ans = false;
        while(canRemoveB(form)) {
            form = removeB(form);
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
        // נוודא שהחיפוש נעשה באותיות גדולות
        line = line.toUpperCase();
        // ביטוי רגולרי לתא: אות אחת מ-A עד Z ואחריה אחת או יותר ספרות
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
        System.out.println("🔍 DEBUG: computeFormP called with: " + form);

        // ★ תיקון: הסרת סימן "=" מוביל אם קיים
        if(form.startsWith("=")) {
            form = form.substring(1).trim();
            System.out.println("🔍 DEBUG: Removed leading '=', new form: " + form);
        }

        // שלב 1: בדיקה אם מדובר במספר
        if (isNumber(form)) {
            Double num = getDouble(form);
            System.out.println("✅ DEBUG: Parsed number: " + num);
            return num;
        }

        // שלב 2: בדיקה אם מדובר במחרוזת מוקפת במרכאות
        if (form.startsWith("\"") && form.endsWith("\"")) {
            String text = form.substring(1, form.length() - 1);
            System.out.println("✅ DEBUG: Returning TEXT: " + text);
            return text;
        }

        // שלב 3: טיפול בנוסחאות IF
        if (form.startsWith("if(") && form.endsWith(")")) {
            Object result = evaluateIfFunction(form);
            System.out.println("✅ DEBUG: computeFormP evaluated IF function, returning: " + result);
            return result;
        }

        // שלב 4: טיפול בהפניות לתאים – החלפת כל הפניה (כגון "A1" או "b2") בערכה המחושב (כמספר)
        java.util.regex.Pattern refPattern = java.util.regex.Pattern.compile("([A-Za-z][0-9]+)");
        java.util.regex.Matcher matcher = refPattern.matcher(form);
        while (matcher.find()) {
            String cellRefOrig = matcher.group(1); // לדוגמה "b1" או "A1"
            String cellRef = cellRefOrig.toUpperCase();
            Index2D idx = new CellEntry(cellRef);
            if (!isIn(idx.getX(), idx.getY())) {
                System.out.println("❌ DEBUG: Cell reference " + cellRef + " out of range.");
                return null;
            }
            String refVal = this.value(idx.getX(), idx.getY());
            Double refNum = getDouble(refVal);
            if (refNum == null) {
                System.out.println("❌ DEBUG: Cell reference " + cellRef + " does not contain a valid number.");
                return null;
            }
            // החלפה בלתי תלויה במקרה – משתמשים ב־replaceAll עם (?i)
            form = form.replaceAll("(?i)" + java.util.regex.Pattern.quote(cellRefOrig), refNum.toString());
        }

        // ★ בדיקה מחדש: אם לאחר החלפת ההפניות המחרוזת היא מספר
        if (isNumber(form)) {
            Double num = getDouble(form);
            System.out.println("✅ DEBUG: After cell replacement, parsed number: " + num);
            return num;
        }

        // שלב 5: הסרת סוגריים חיצוניים מיותרים (אם קיימים)
        while (canRemoveB(form)) {
            form = removeB(form);
        }

        // שלב 6: ניתוח ביטוי אריתמטי – חיפוש האופרטור העיקרי
        int opIndex = findLastOp(form);
        if (opIndex == -1) {
            // אם לא נמצא אופרטור ונראה שאין תווי אופרטור, נניח שמדובר בטקסט פשוט
            boolean hasOperator = false;
            for (String op : Ex2Utils.M_OPS) {
                if (form.contains(op)) {
                    hasOperator = true;
                    break;
                }
            }
            if (!hasOperator) {
                System.out.println("✅ DEBUG: Returning literal text: " + form);
                return form;  // אין אופרטור → טקסט פשוט
            }
            System.out.println("❌ DEBUG: No valid operator found in arithmetic expression: " + form);
            return null;
        }

        String leftStr = form.substring(0, opIndex);
        String rightStr = form.substring(opIndex + 1);
        char operator = form.charAt(opIndex);

        Object leftObj = computeFormP(leftStr);
        Object rightObj = computeFormP(rightStr);
        if (!(leftObj instanceof Double) || !(rightObj instanceof Double)) {
            System.out.println("❌ DEBUG: One of the operands is not a number in expression: " + form);
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
                    System.out.println("❌ DEBUG: Division by zero in expression: " + form);
                    return null;
                }
                result = leftVal / rightVal;
                break;
            default:
                System.out.println("❌ DEBUG: Unknown operator " + operator + " in expression: " + form);
                return null;
        }
        System.out.println("✅ DEBUG: Arithmetic expression " + form + " evaluated to: " + result);
        return result;
    }






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

    private Object evaluateIfFunction(String form) {
        System.out.println("🔍 DEBUG: Evaluating IF function: " + form);
        if (!form.startsWith("if(") || !form.endsWith(")")) {
            System.out.println("❌ DEBUG: Invalid IF format!");
            return null;
        }
        String inner = form.substring(3, form.length()-1).trim();
        String[] parts = splitIfArguments(inner);
        if (parts.length != 3) {
            System.out.println("❌ DEBUG: IF function has wrong number of arguments! Found: " + parts.length);
            return null;
        }
        String condition = parts[0];
        String ifTrue = parts[1];
        String ifFalse = parts[2];

        // *** בדיקה נוספת ***
        // אם אחד מהארגומנטים הוא הפניה לתא בצורה גסה (למשל "A1" או "b2"), זה נחשב לבלתי תקין.
        if (ifTrue.matches("^[A-Za-z]+[0-9]+$") || ifFalse.matches("^[A-Za-z]+[0-9]+$")) {
            System.out.println("❌ DEBUG: IF function argument is a bare cell reference, which is invalid.");
            return null;
        }

        Boolean condResult = evaluateCondition(condition);
        if (condResult == null) {
            System.out.println("❌ DEBUG: IF condition is invalid.");
            return null;
        }
        System.out.println("✅ DEBUG: IF condition evaluated to: " + condResult);
        Object chosenResult = condResult ? computeFormP(ifTrue) : computeFormP(ifFalse);
        System.out.println("✅ DEBUG: IF function chosen result: " + chosenResult);
        return chosenResult;
    }




    private Boolean evaluateCondition(String condition) {
        System.out.println("🔍 DEBUG: Evaluating condition: " + condition);
        String[] ops = {"<=", ">=", "==", "!=", "<", ">"};
        for (String op : ops) {
            if (condition.contains(op)) {
                String[] parts = condition.split(java.util.regex.Pattern.quote(op));
                if (parts.length != 2) {
                    System.out.println("❌ DEBUG: Condition split into " + parts.length + " parts, expected 2.");
                    return null; // פורמט שגוי
                }
                Object leftObj = computeFormP(parts[0].trim());
                Object rightObj = computeFormP(parts[1].trim());
                if (!(leftObj instanceof Double) || !(rightObj instanceof Double)) {
                    System.out.println("❌ DEBUG: One of the operands is not a number in condition: " + condition);
                    return null;
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
        System.out.println("❌ DEBUG: No valid operator found in condition: " + condition);
        return null;
    }


    private boolean isValidIf(String form) {
        if (!form.startsWith("if(") || !form.endsWith(")")) {
            return false; // ❌ לא בפורמט הנכון
        }

        // ✅ הסרת `if(` והסוגריים `)`
        form = form.substring(3, form.length() - 1).trim();
        String[] parts = form.split(",");

        if (parts.length != 3) {
            return false; // ❌ חייב להיות בדיוק 3 פרמטרים
        }

        String condition = parts[0].trim();
        String ifTrue = parts[1].trim();
        String ifFalse = parts[2].trim();

        // ✅ בדיקה שהתנאי תקין
        if (!isValidCondition(condition)) {
            return false;
        }

        // ✅ בדיקה ש- ifTrue ו- ifFalse הם ערכים תקינים
        return isValidExpression(ifTrue) && isValidExpression(ifFalse);
    }
    private boolean isValidCondition(String condition) {
        for (String op : Ex2Utils.B_OPS) {
            if (condition.contains(op)) {
                String[] parts = condition.split(op);
                if (parts.length != 2) return false;

                // בדיקה ששני הצדדים הם ביטויים תקינים
                return isValidExpression(parts[0].trim()) && isValidExpression(parts[1].trim());
            }
        }
        return false;
    }
    private boolean isValidExpression(String expr) {
        if (expr.startsWith("=")) {
            return true; // ✅ אם זה נוסחה זה תקין
        }
        return isNumber(expr) || expr.startsWith("\"") && expr.endsWith("\""); // ✅ מספר או טקסט
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