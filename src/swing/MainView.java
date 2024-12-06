package swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Locale;

import static swing.ObserverFactory.MakeMenu;
import static swing.ObserverFactory.MakeMenuItem;

public class MainView extends JFrame {
    private final LanguageObservable languageObservable;

    HashMap<String, AbstractButton> clickableObservers = new HashMap<>();

    private JScrollPane tableScrollPane;

    public void setPreferredSizeOfTableScrollPane(Dimension d) {
        tableScrollPane.setPreferredSize(d);
    }

    protected JTable dataTable;

    MainView() {
        languageObservable =
                new LanguageObservable();
        setJMenuBar(getNewMenuBar());
        Container cp = getContentPane();
        cp.add(getPanelWithTableView());
        configureTable(dataTable);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Swing RMI-DataClient");
    }

    private Component getPanelWithTableView() {
        JPanel panel = new JPanel();
        dataTable = new JTable();
        tableScrollPane = new JScrollPane(dataTable);
        panel.add(tableScrollPane);
        return panel;
    }

    private void configureTable(JTable t) {
        t.setRowSelectionAllowed(false);
        t.setColumnSelectionAllowed(false);
        t.setCellSelectionEnabled(true);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    }

    private JMenuBar getNewMenuBar() {
        JMenuBar mb = new JMenuBar();
        mb.add(getNewFileMenu());
        mb.add(getNewHelpMenu());
        return mb;
    }


    private JMenu getNewHelpMenu() {
        JMenu jm = MakeMenu("help", languageObservable);
        jm.add(getNewLanguageMenu());
        JMenuItem jmi = getNewMenuItem("about");
        jmi.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_T, ActionEvent.ALT_MASK));
        jm.add(jmi);
        return jm;
    }

    private JMenu getNewLanguageMenu() {
        JMenu jm = MakeMenu("lang", languageObservable);
        jm.add(getNewMenuItem("en"));
        jm.add(getNewMenuItem("de"));
        return jm;
    }

    private JMenu getNewFileMenu() {
        JMenu jm = MakeMenu("file", languageObservable);
        JMenuItem jmi = getNewMenuItem("delete");
        jm.add(jmi);
        jmi = getNewMenuItem("create");
        jm.add(jmi);
        jmi = getNewMenuItem("find");
        jm.add(jmi);
        return jm;
    }


    private JMenuItem getNewMenuItem(String key) {
        JMenuItem jmi = MakeMenuItem(key, languageObservable);
        clickableObservers.put(jmi.getName(), jmi);
        return jmi;
    }

    public void setMenuItemActionOf(String key, Action a) {
        AbstractButton b = clickableObservers.get(key);
        KeyStroke acc = null;
        if (b instanceof JMenuItem) {
            acc = ((JMenuItem) b).getAccelerator();
        }
        String s = b.getText();
        b.setAction(a);
        b.setText(s);
        if (acc != null) {
            ((JMenuItem) b).setAccelerator(acc);
        }
    }

    public String getText(String key) {
        return languageObservable.getText(key);
    }

    public void setNewLanguage(Locale l) {
        languageObservable.setNewLanguage(l);
    }
}