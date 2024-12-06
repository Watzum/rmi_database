/*
 * FindDialog.java
 * (c) 04.12.2021, Harald R. Haberstroh
 */
package swing;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class FindDialog {
    /**
     * show Dialog for many fields
     *
     * @param parentComponent parent window
     * @param title           title of dialog
     * @param fields          array with field names
     * @param defaults        array with predefined values (might be null)
     * @return array with filled fields ("", if empty)
     */
    public static String[] showFindDialog(Component parentComponent, String title, String[] fields, String[] defaults) {
        JPanel thePanel = new JPanel();
        thePanel.setLayout(new GridLayout(fields.length, 2));
        JTextField[] textFields = new JTextField[fields.length];
        JLabel[] labels = new JLabel[fields.length];
        String[] returnValues = new String[fields.length];
        Arrays.fill(returnValues, "");
        for (int i = 0; i < fields.length; i++) {
            labels[i] = new JLabel(fields[i] + ":");
            thePanel.add(labels[i]);
            textFields[i] = new JTextField("");
            if (defaults != null && defaults[i] != null)
                textFields[i].setText(defaults[i]);
            thePanel.add(textFields[i]);
        }
        int result = JOptionPane.showConfirmDialog(parentComponent, thePanel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            for (int i = 0; i < returnValues.length; i++) {
                returnValues[i] = textFields[i].getText();
            }
        }
        return returnValues;
    }

    public static void main(String[] args) {
        String[] fieldDescription = {"name", "", "Phone"};
        String[] result = showFindDialog(null, "Find Dialog", fieldDescription, null);
        for (int i = 0; i < result.length; i++) {
            System.out.printf("%s: \"%s\"\n", fieldDescription[i], result[i]);
        }
    }
}
