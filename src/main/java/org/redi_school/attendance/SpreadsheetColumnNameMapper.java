package org.redi_school.attendance;

class SpreadsheetColumnNameMapper {
    String columnIndexToLetter(int columnNumber) {
        int temp = 0;
        StringBuilder letters = new StringBuilder();

        while (columnNumber > 0) {
            temp = (columnNumber - 1) % 26;
            letters.append(charForInt(temp));
            columnNumber = (columnNumber - temp - 1) / 26;
        }
        return letters.reverse().toString();
    }

    private String charForInt(int toMakeAChar) {
        int asciiOffsetForAt = 65;
        return String.valueOf((char) (toMakeAChar + asciiOffsetForAt));
    }
}
