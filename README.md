Ex4 Spreadsheet Project
This project implements a spreadsheet for Ex4 using Java. It supports cell input, formula evaluation (including arithmetic expressions and IF formulas), and additional range-based functions (min, max, sum, average). The code is organized modularly into several key parts:

Ex2Sheet: The main spreadsheet class that manages cells, formula evaluation, and value display.
SCell: The cell implementation that stores both the raw user input and the computed/display value.
IF Formula Handling: A set of functions that validate and evaluate IF formulas.
Range2D and RangeFunctions: Additional classes that support range-based functions on cell ranges (e.g., “A1:C5”).

The project also supports saving data to a text file and loading a text file into the GUI.

1. Ex2Sheet
Purpose:
Manages a two-dimensional array of cells and provides methods to set cell values, evaluate formulas, and return the displayed value.

Key Methods:

set(int x, int y, String s):

Creates a new cell (an instance of SCell) with the input string s.
Determines the cell type based on the input:
If the input starts with "=":
If it starts with "=if(", it calls isValidIf(form, x, y) to validate the IF formula. If valid, the cell type is set to IF; otherwise, it is set to IF_ERR.
If it is a function (detected by isFunction), the type is set to FUNCTION.
Otherwise, the type is set to FORM.
The cell is then stored in the table.

eval():

Computes the values of all cells in the spreadsheet.
Uses a dependency “depth” method to prevent circular references.
For each cell, if it’s not plain text, eval(x, y) is called to calculate the cell’s value and update the data (for numerical values) and textValues (for textual values) arrays.

value(int x, int y):

Returns the displayed value for cell (x, y).
If a computed value exists in data or textValues, that is returned; otherwise, the cell’s raw value (via c.getData()) is returned.
If the cell’s type is set to an error type (e.g., IF_ERR), it returns a fixed error string (e.g., ERR_IF or other appropriate error).

2. SCell
Purpose:
Implements the Cell interface. Each cell stores both the raw user input and the computed display value.

Key Features:

Raw Input vs. Display Value:

The field rawInput holds the exact text entered by the user.
The field _line holds the value that is currently displayed (which might be a computed result or an error message).

setData(String s):

Saves the original input in rawInput.
Determines the cell type (TEXT, NUMBER, FORM) based on the input.
Initially sets _line equal to the raw input.
getData():

Returns the displayed value.
If the cell’s type is one of the error types (IF_ERR, ERR_FORM_FORMAT, etc.), it returns a fixed error string (ERR_FORM or other appropriate error).

getRawInput():

Returns the raw user input.
This method is used by the GUI when opening a cell for editing so that the user sees the original formula (even if it computes to an error).
3. Formula Evaluation
Arithmetic and Text Formulas
computeForm(String x, int y):

Extracts the formula from the cell (removing the leading "=") and passes it to computeFormP.
Updates data or textValues based on the result.
computeFormP(String form):

Processes the formula recursively.
Checks for numeric or quoted text expressions.
If the formula begins with "if(", it calls evaluateIfFunction.
Uses regex to find cell references (using a pattern like "([A-Za-z][0-9]+)") and replaces them with their computed values.
Finally, if the formula represents an arithmetic expression, it parses the expression and returns the computed result.
IF Formulas

evaluateIfFunction(String form):

Validates that the formula starts with "if(" and ends with ")".
Uses splitIfArguments to divide the formula into three parts: condition, ifTrue, and ifFalse.
Calls evaluateCondition on the condition to determine which branch to evaluate.
Evaluates the chosen branch (via a recursive call to computeFormP).

evaluateCondition(String condition):

Splits the condition by one of the supported operators (from Ex2Utils.B_OPS such as <, >, ==, etc.) using Pattern.quote(op) to escape special regex characters.
Converts each part into a numerical value using computeFormP.
Returns true or false based on the operator’s comparison.

splitIfArguments(String s):

Iterates over the characters of the IF formula (inside the parentheses) to split the string by commas.
Uses a counter (bracketCount) to track nested parentheses and a boolean flag (inQuotes) to detect quoted text.
The continue; statement is used to skip the remainder of the loop iteration when a comma that separates arguments is encountered, ensuring that only commas outside nested structures are used as delimiters.

containsSelfReference(String formula, int x, int y):

Constructs the cell reference for cell (x, y) (e.g., "A0" for cell A0).
Checks if the given formula (converted to uppercase) contains that reference, which indicates a self-reference.

isValidIf(String form, int cellX, int cellY):

Validates that the IF formula is properly formatted:
It must start with "if(" and end with ")".
It must have exactly three arguments (condition, ifTrue, ifFalse), as determined by splitIfArguments.
Uses evaluateCondition to ensure the condition can be evaluated.
Uses isValidExpressionForArgument (see below) to validate ifTrue and ifFalse.
Checks for self-reference using the cell’s coordinates.

isValidExpressionForArgument(String expr):

Ensures that an expression intended as an argument for IF (ifTrue/ifFalse) is valid.
Allows formulas (starting with "=" or "if("), numbers, or quoted text.
Disallows a bare cell reference (e.g., "A1").

4. Range Functionality
Range2D
Purpose:
Represents a rectangular range of cells (e.g., "A1:C5").

The constructor parses the range string into two cell references (start and end).
Provides methods to get the starting and ending column and row indices.
The method getAllCells() returns an ArrayList of Index2D objects for every cell within the range.

RangeFunctions
Purpose:
Provides static methods for calculating values over a range of cells.

min(Ex2Sheet sheet, Range2D range): Iterates over all cells in the range and returns the minimum numerical value.
max(Ex2Sheet sheet, Range2D range): Returns the maximum numerical value.
sum(Ex2Sheet sheet, Range2D range): Returns the sum of all numerical values.
average(Ex2Sheet sheet, Range2D range): Returns the average of the numerical values.

computeFunction
Within Ex2Sheet, a helper function computeFunction(String form) is implemented to detect if a formula begins with a range function (min, max, sum, or average) and to call the appropriate method in RangeFunctions:

For example, if the formula is =min(A1:C5), the function extracts "A1:C5", creates a Range2D object from it, and calls RangeFunctions.min(this, range).

5. Overall Code Flow
Cell Input and Setting:

When a user inputs a value in a cell, the set method creates a new SCell and classifies the input (plain text, number, formula, IF, function).
The raw input is stored in rawInput for later editing, while the displayed value (_line) may later change if the formula is evaluated.
Evaluation:

The eval method iterates through all cells, computes their values using eval(x, y), and updates the data and textValues arrays.
eval(x, y) returns the computed result or, in the case of an error, a fixed error string (e.g., ERR_FORM).
Formula Processing:

computeForm removes the leading "=" and calls computeFormP.
computeFormP checks for range functions (via computeFunction), numbers, quoted text, IF formulas (via evaluateIfFunction), and cell references.
If cell references are found, a regex-based mechanism (using Pattern and Matcher) replaces them with their computed values.
Arithmetic expressions are parsed using helper methods such as findLastOp and then computed.
Range Functions:

The additional classes Range2D and RangeFunctions enable formulas like =min(A1:C5), =max(A1:C5), etc.
computeFunction in Ex2Sheet recognizes these formulas, creates a Range2D object, and calls the corresponding method in RangeFunctions.
Editing and Display:

When a cell is edited via the GUI, the raw input (stored in SCell’s rawInput) is used so that even if a cell’s display shows an error (e.g., ERR_FORM), the user sees the original input for correction.
6. Additional Technical Details
Regex Usage (Pattern and Matcher):

The code uses java.util.regex.Pattern.compile("([A-Za-z][0-9]+)") to define a regex that matches a cell reference (one letter followed by one or more digits).
A Matcher is then used on the formula to find all such references so they can be replaced by their numerical values.
The use of Pattern.quote(op) in functions like evaluateCondition ensures that any operator (which might have special meaning in regex) is treated as a literal string.
StringBuilder:

While not explicitly shown in every function, StringBuilder is typically used to efficiently build strings in loops. It is more efficient than concatenating immutable String objects in Java.
splitIfArguments and continue:

7. How to Run and Extend the Project
Running the Project:

The main method in Ex2GUI initializes an Ex2Sheet instance and runs a GUI loop that listens for mouse clicks.
The GUI uses StdDrawEx2 to render the grid and handle input.
When a cell is clicked, inputCell opens a text box pre-filled with the cell’s raw input (via getRawInput() from SCell).
Extending the Project:

To add new functions (e.g., additional range functions), you can create new static methods in the RangeFunctions class and update computeFunction accordingly.
Further enhancements to formula parsing can be added by extending computeFormP or the IF evaluation functions.

Conclusion:

This README summarizes the architecture and core functionality of the Ex4 project. The project is designed modularly to:
Manage cell input and classification.
Evaluate formulas (arithmetic, IF, and range functions).
Handle errors gracefully (displaying error messages in the grid while preserving raw input for editing).
Provide range-based functions via new classes (Range2D and RangeFunctions) for operations like min, max, sum, and average.

Here are some examples of the project's capabilities in the GUI:

Func example:

https://github.com/user-attachments/assets/66a6726a-1d92-435f-b548-c758d11ecfb6

If Example:

https://github.com/user-attachments/assets/6f6360d7-a5ef-405d-9cd1-d0eda8778481

Editing and update cells:

https://github.com/user-attachments/assets/64ea17ff-fe31-4bd7-9c88-b2ccce984966


