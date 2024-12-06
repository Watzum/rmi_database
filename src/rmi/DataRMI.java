package rmi;

import exceptions.DuplicateKeyException;
import exceptions.RecordNotFoundException;
import rmi.observer.ObservableDataRMI;
import rmi.observer.RemoteObserver;

import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

public class DataRMI extends UnicastRemoteObject implements IDataRMI {

    private final db.Data databaseAccess;
    private final String filePath;
    private ObservableDataRMI observable = null;

    private ArrayList<Integer> lockedRecords = new ArrayList<>();

    public DataRMI(String filePath) throws RemoteException {
        try {
            databaseAccess = new db.Data(filePath);
            this.filePath = filePath;
        } catch (FileNotFoundException e) {
            throw new RemoteException("FileNotFoundException: " + e);
        }
    }

    public DataRMI(String filePath, ObservableDataRMI o) throws RemoteException {
        try {
            databaseAccess = new db.Data(filePath);
            this.filePath = filePath;
            observable = o;
        } catch (FileNotFoundException e) {
            throw new RemoteException("FileNotFoundException: " + e);
        }
    }

    @Override
    public String[] read(int recNo) throws RemoteException {
        try {
            return databaseAccess.read(recNo);
        } catch (RecordNotFoundException e) {
            throw new RemoteException("RecordNotFoundException: " + e);
        }
    }

    @Override
    public void update(int recNo, String[] data, long lockCookie) throws RemoteException {
        try {
            databaseAccess.update(recNo, data, lockCookie);
            notifyObservers();
        } catch (RecordNotFoundException e) {
            throw new RemoteException("RecordNotFoundException " + e);
        }
    }

    @Override
    public void delete(int recNo, long lockCookie) throws RemoteException {
        try {
            databaseAccess.delete(recNo, lockCookie);
            notifyObservers();
        } catch (RecordNotFoundException e) {
            throw new RemoteException("RecordNotFoundException " + e);
        } catch (SecurityException e) {
            throw new RemoteException("SecurityException " + e);
        }
    }

    @Override
    public int[] find(String[] criteria) throws RemoteException {
        return databaseAccess.find(criteria);
    }

    @Override
    public int create(String[] data) throws RemoteException {
        try {
            int d =  databaseAccess.create(data);
            notifyObservers();
            return d;
        } catch (DuplicateKeyException e) {
            throw new RemoteException("DuplicateKeyException: " + e);
        }
    }

    @Override
    public long lock(int recNo) throws RemoteException {
        try {
            long lockCookie = databaseAccess.lock(recNo);
            lockedRecords.add(recNo);
            return lockCookie;
        } catch (RecordNotFoundException e) {
            throw new RemoteException("RecordNotFoundException " + e);
        }
    }

    public long tryLock(int recNo) throws RemoteException {
        if (!lockedRecords.contains(recNo)) {
            return lock(recNo);
        } else {
            throw new RemoteException("SecurityException " + "Record is locked!");
        }
    }

    @Override
    public void unlock(int recNo, long cookie) throws RemoteException {
        try {
            databaseAccess.unlock(recNo, cookie);
            int idx = lockedRecords.indexOf(recNo);
            lockedRecords.remove(idx);
        } catch (RecordNotFoundException e) {
            throw new RemoteException("RecordNotFoundException " + e);
        } catch (SecurityException e) {
            throw new SecurityException("SecurityException " + e);
        }
    }

    @Override
    public HashMap[] getColumnInformation() throws RemoteException {
        return databaseAccess.getColumnInformation();
    }

    @Override
    public String[][] readAllRecords() throws RemoteException {
        return databaseAccess.readAllRecords();
    }

    @Override
    public int getRecNoWithDeletedRecords(int recNo) throws  RemoteException {
        return databaseAccess.getRecNoWithDeletedRecords(recNo);
    }

    @Override
    public void addObserver(RemoteObserver ro) throws RemoteException {
        if (observable != null) {
            observable.addObserver(ro);
        }
    }

    @Override
    public void deleteObserver(RemoteObserver ro) throws RemoteException {
        if (observable != null) {
            observable.deleteObserver(ro);
        }
    }

    @Override
    public boolean isObservable() throws RemoteException {
        return observable == null;
    }

    private void notifyObservers() {
        if (observable != null) {
            System.out.println("notify observers...");
            try {
                observable.notifyObservers();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}