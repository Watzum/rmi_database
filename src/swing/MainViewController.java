package swing;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Locale;

import static swing.FindDialog.showFindDialog;

public class MainViewController {

    private final MainView view;
    private final DatabaseModel model;

    private long currentLockCookie;

    public MainViewController(MainView v, DatabaseModel m) {
        view = v;
        model = m;
        setActions();
        linkTableToModel(v.dataTable);
        setViewSizes(v.dataTable);
        view.setVisible(true);
        view.setLocationRelativeTo(null);
    }

    private void linkTableToModel(JTable t) {
        setTableModel(t);
        setTableColumnModel(t);
        view.dataTable.addPropertyChangeListener(getNewOnEditLocker());
    }

    private void setViewSizes(JTable t) {
        int columnWidth = getWidthOfColumns(t);
        Dimension tableSize = new Dimension(columnWidth + 50, 600);
        t.setPreferredSize(tableSize);
        view.setPreferredSizeOfTableScrollPane(tableSize);
        view.setSize(new Dimension(tableSize.width + 50,
                tableSize.height + 100));
    }


    private void setTableColumnModel(JTable t) {
        DefaultTableColumnModel cm = new DefaultTableColumnModel();
        int maxFontWidth = getMaxFontWidthOf(t);
        String[] tableHeaders = model.getTableHeaders();
        for (int i = 0; i < tableHeaders.length; i++) {
            int columnWidth = maxFontWidth * model.getColumnLength(i);
            TableColumn tc = new TableColumn(i, columnWidth);
            tc.setMinWidth(columnWidth);
            tc.setHeaderValue(tableHeaders[i]);
            cm.addColumn(tc);
        }
        t.setColumnModel(cm);
    }

    private void setTableModel(JTable t) {
        DefaultTableModel tm = new DefaultTableModel() {
            @Override
            public int getRowCount() {
                return model.tableCells.length;
            }

            @Override
            public int getColumnCount() {
                try {
                    return model.getColumnInformation().length;
                } catch (RemoteException e) {
                    return 0;
                }
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return model.tableCells[rowIndex][columnIndex];
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                if (!model.tableCells[rowIndex][columnIndex].equals(aValue)) {
                    model.tableCells[rowIndex][columnIndex] = (String) aValue;
                    model.update(rowIndex, model.tableCells[rowIndex], currentLockCookie);
                    model.unlock(rowIndex, currentLockCookie);
                }
            }
        };
        t.setModel(tm);
        model.setTableModel(tm);
    }

    private int getWidthOfColumns(JTable t) {
        int width = 0;
        for (int i = 0; i < t.getColumnCount(); i++) {
            width += t.getColumnModel().getColumn(i).getWidth();
        }
        return width;
    }

    private int getMaxFontWidthOf(JComponent c) {
        FontMetrics fontMetrics = c.getFontMetrics(c.getFont());
        int maxFontWidth =
                Arrays.stream(fontMetrics.getWidths()).max().getAsInt();
        return maxFontWidth;
    }

    //Locks the record, when the according cell gets edited
    private PropertyChangeListener getNewOnEditLocker() {
        PropertyChangeListener l = evt -> {
            if (evt.getPropertyName().equals("tableCellEditor")) {
                if (evt.getNewValue() != null) {
                    lockRecord(view.dataTable.getSelectedRow());
                } else if (evt.getOldValue() != null) {
                    try {
                        model.unlock(view.dataTable.getSelectedRow(), currentLockCookie);
                    } catch (Exception e) {}
                }
            }
        };
        return l;
    }

    private void lockRecord(int recNo) {
        try {
            currentLockCookie =
                    model.tryLock(recNo);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view,
                    "Record is locked!",
                    view.getText("err"),
                    JOptionPane.INFORMATION_MESSAGE);
            cancelEditing();
        }
    }

    private void cancelEditing() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10);
                    view.dataTable.getCellEditor().cancelCellEditing();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread t1 = new Thread(r);
        t1.start();
    }

    private void setActions() {
        setMenuItemActions();
        setWindowClosingEvent();
    }


    private void setMenuItemActions() {
        view.setMenuItemActionOf("about", getHelpAboutMenuItemAction());
        view.setMenuItemActionOf("de", getChangeLanguageAction("de"));
        view.setMenuItemActionOf("en", getChangeLanguageAction("en"));
        view.setMenuItemActionOf("delete", getNewDeleteAction());
        view.setMenuItemActionOf("create", getNewCreateAction());
        view.setMenuItemActionOf("find", getNewFindAction());
    }

    private Action getNewDeleteAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int currentIdx = view.dataTable.getSelectedRow();
                if (currentIdx >= 0) {
                    lockRecord(currentIdx);
                    model.delete(currentIdx, currentLockCookie);
                    model.updateModel();
                    view.dataTable.updateUI();
                } else {
                    System.out.println("No record selected");
                }
            }
        };
    }

    private Action getNewCreateAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String[] data = new String[model.getColumnInformation().length];
                    for (int i = 0; i < data.length; i++) {
                        data[i] = "";
                    }
                    model.create(data);
                    model.updateModel();
                    view.dataTable.updateUI();
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    private enum STATUS {disabled, enabled};

    private Action getNewFindAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] fieldDescription = model.getTableHeaders();
                String[] matchCriteria =
                        showFindDialog(null, "Find Dialog",
                                fieldDescription, null);
                if (emptyMatchCriteria(matchCriteria)) {
                    setChangeOperationsStatus(STATUS.enabled);
                } else {
                    setChangeOperationsStatus(STATUS.disabled);
                }
                int[] recordNumbers = model.find(matchCriteria);
                model.tableCells = model.readSomeRecords(recordNumbers);
                view.dataTable.updateUI();
            }
        };
    }

    private void setChangeOperationsStatus(STATUS s) {
        boolean status;
        if (s.equals(STATUS.enabled)) {
            status = true;
        } else {
            status = false;
        }
        view.clickableObservers.get("delete").setEnabled(status);
        view.clickableObservers.get("delete").updateUI();
        view.clickableObservers.get("create").setEnabled(status);
        view.clickableObservers.get("create").updateUI();
        view.dataTable.setEnabled(status);
        view.dataTable.updateUI();
    }

    private boolean emptyMatchCriteria(String[] criteria) {
        for (String s : criteria) {
            if (!s.equals("")) {
                return false;
            }
        }
        return true;
    }

    private Action getHelpAboutMenuItemAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(view,
                        "Binary Database RMI SwingClient 1.0 \n "
                        + "© 2021, Emil Watz, 5AHIF",
                        view.getText("about"),
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        };
    }


    private Action getChangeLanguageAction(String lang) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                view.setNewLanguage(new Locale(lang));
            }
        };
    }

    private void setWindowClosingEvent() {
        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    System.out.println("delete observer in client");
                    model.deleteObserver();
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
                super.windowClosing(e);
            }
        });
    }
}
