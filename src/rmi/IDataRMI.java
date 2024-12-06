package rmi;

import rmi.observer.RemoteObserver;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface IDataRMI extends Remote {
    String[] read(int recNo) throws RemoteException;
    void update(int recNo, String[] data, long lockCookie)
            throws RemoteException;
    void delete(int recNo, long lockCookie)
            throws RemoteException;
    int[] find(String[] criteria) throws RemoteException;
    int create(String[] data) throws RemoteException;
    long lock(int recNo) throws RemoteException;
    long tryLock(int recNo) throws RemoteException;
    void unlock(int recNo, long cookie)
            throws RemoteException;
    HashMap[] getColumnInformation() throws RemoteException;
    String[][] readAllRecords() throws RemoteException;
    int getRecNoWithDeletedRecords(int recNo) throws RemoteException;
    void addObserver(RemoteObserver ro) throws RemoteException;
    void deleteObserver(RemoteObserver ro) throws RemoteException;
    boolean isObservable() throws RemoteException;
}