package swing;

import exceptions.RecordNotFoundException;
import rmi.Client;
import rmi.observer.RemoteObserver;

import javax.swing.table.DefaultTableModel;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.HashMap;

public class DatabaseModel extends Client { //implements RemoteObserver

    private final HashMap<String, String>[] columnInformation;

    public int getColumnLength(int columnNumber) {
        String s = columnInformation[columnNumber].get("Laenge");
        return Integer.parseInt(s);
    }

    private final String[] tableHeaders;

    public String[] getTableHeaders() {
        return tableHeaders;
    }

    public String[][] tableCells;

    private DefaultTableModel tableModel;

    private ObserverModelWrapper observerWrapper;

    public DatabaseModel(String DBServer_ip) throws FileNotFoundException,
            RemoteException {
        super(DBServer_ip);
        columnInformation = getColumnInformation();
        tableHeaders = getColumnsNames();
        tableCells = readAllRecords();
        observerWrapper = new ObserverModelWrapper(this);
        System.out.println("test");
        addObserver();
    }

    public void updateModel() {
        try {
            tableCells = readAllRecords();
            tableModel.fireTableDataChanged();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private String[] getColumnsNames() {
        String[] s = new String[columnInformation.length];
        for (int i = 0; i < columnInformation.length; i++) {
            s[i] = columnInformation[i].get("Feldbeschreibung");
        }
        return s;
    }

    @Override
    public void update(int recNo, String[] data, long lockCookie)
        throws RecordNotFoundException, SecurityException {
        try {
            recNo = super.getRecNoWithDeletedRecords(recNo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.update(recNo,  data, lockCookie);
    }

    @Override
    public String[] read(int recNo) throws RecordNotFoundException{
        try {
            recNo = super.getRecNoWithDeletedRecords(recNo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return super.read(recNo);
    }

    @Override
    public void delete(int recNo, long lockCookie) throws
            RecordNotFoundException, SecurityException {
        try {
            recNo = super.getRecNoWithDeletedRecords(recNo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.delete(recNo, lockCookie);
    }

    @Override
    public long tryLock(int recNo) throws RecordNotFoundException, SecurityException, RemoteException {
        try {
            recNo = super.getRecNoWithDeletedRecords(recNo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return super.tryLock(recNo);
    }

    @Override
    public void unlock(int recNo, long lockCookie)
            throws RecordNotFoundException, SecurityException {
        try {
            recNo = super.getRecNoWithDeletedRecords(recNo);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.unlock(recNo, lockCookie);
    }

    protected String[][] readSomeRecords(int[] recordNumbers) {
        String[][] records = new String[recordNumbers.length][tableHeaders.length];
        for (int i = 0; i < recordNumbers.length; i++) {
            records[i] = super.read(recordNumbers[i]);
        }
        return records;
    }

    public void setTableModel(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
    }

    public void addObserver() throws RemoteException {
        database.addObserver(observerWrapper);
    }

    public void deleteObserver() throws RemoteException {
        database.deleteObserver(observerWrapper);
    }
}