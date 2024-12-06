package rmi.observer;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteObserver extends Remote {
    void update(ObservableRMI o) throws RemoteException;
}