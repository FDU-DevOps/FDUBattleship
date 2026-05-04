package org.fdu;
import java.util.*;
// Checkstyle: missing Javadoc on public class
public class Qualityviolations {
    // Checkstyle: magic numbers, public mutable field
    // PMD: public field, avoid using raw types
    public List items = new ArrayList();
    // SpotBugs: non-private field exposed publicly
    public static String[] sharedArray = new String[10];
    // Checkstyle: method name violates naming convention (should be camelCase)
    // PMD: method too long, avoid deeply nested blocks
    public int BadMethodName(int x) {
        // PMD: avoid reassigning parameters
        x = x + 1;
        int result = 0;
        for (int i = 0; i < 100; i++) {       // Checkstyle: magic number
            for (int j = 0; j < 100; j++) {    // Checkstyle: magic number
                for (int k = 0; k < 100; k++) {// Checkstyle: magic number
                    result += i * j * k;
                }
            }
        }
        return result;
    }
    // SpotBugs: null dereference, return value of method ignored
    public String nullDereferenceBug() {
        String value = null;
        // SpotBugs: NP_NULL_ON_SOME_PATH, dereferencing known null
        return value.trim();
    }
    // SpotBugs: equals() on incompatible types
    public boolean wrongEqualsBug(Object obj) {
        String name = "battleship";
        // SpotBugs: EC_UNRELATED_TYPES
        return name.equals(42);
    }
    // SpotBugs: ignored return value (String.replace returns a new string)
    public void ignoredReturnValue(String s) {
        s.replace("a", "b"); // result never used
    }

    // ---- CPD BLOCK 1: original ----
    // CPD: duplicated across BadMethodName, AnotherBadMethod, YetAnotherBadMethod
    public int AnotherBadMethod(int x) {
        // PMD: avoid reassigning parameters
        x = x + 1;
        int result = 0;
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                for (int k = 0; k < 100; k++) {
                    result += i * j * k;
                }
            }
        }
        return result;
    }

    // ---- CPD BLOCK 2: third copy of the same logic, makes CPD unambiguous ----
    public int YetAnotherBadMethod(int x) {
        x = x + 1;
        int result = 0;
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                for (int k = 0; k < 100; k++) {
                    result += i * j * k;
                }
            }
        }
        return result;
    }

    // ---- CPD BLOCK 3: duplicated string processing logic ----
    // CPD: processInput and processOutput are identical in structure
    public String processInput(String input) {
        if (input == null) {
            return "";
        }
        String trimmed = input.trim();
        String lowered = trimmed.toLowerCase();
        String replaced = lowered.replace(" ", "_");
        return replaced + "_input";
    }

    // CPD: copy paste of processInput with only the suffix changed
    public String processOutput(String input) {
        if (input == null) {
            return "";
        }
        String trimmed = input.trim();
        String lowered = trimmed.toLowerCase();
        String replaced = lowered.replace(" ", "_");
        return replaced + "_output";
    }

    // PMD: empty catch block
    public void emptyCatch() {
        try {
            int x = Integer.parseInt("abc");
        } catch (NumberFormatException e) {
            // swallowed exception, PMD: EmptyCatchBlock
        }
    }
    // PMD: avoid using System.out
    // Checkstyle: missing Javadoc
    public void printStuff() {
        System.out.println("hello");   // PMD: SystemPrintln
        System.out.println("world");
    }
}
