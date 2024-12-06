package rmi.observer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ObservableRMI extends Remote {
    void addObserver(RemoteObserver ro) throws RemoteException;
    void deleteObserver(RemoteObserver ro) throws RemoteException;
    void notifyObservers() throws RemoteException;
}
