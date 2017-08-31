/*
 * Nick Pagsanjan
 * CS 4110
 * ExpressionRecord.java
 *
 * Simple way of storing variable type and location
 */

public class ExpressionRecord {
    private char   type;
    private int    location;
    private String strLocation;

    private static int numberOfLabelsMade = 0; // keep track of the number of labels created so far

    public void setType(char type) {
        this.type = type;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public void setLocation(String strLocation) {
        this.strLocation = strLocation;
    }

    public String getStrLocation() {
        return strLocation;
    }

    public char getType() {
        return type;
    }

    public int getLocation() {
        return location;
    }

    public static String generateLabel() {
        return "label" + numberOfLabelsMade++;
    }

}
