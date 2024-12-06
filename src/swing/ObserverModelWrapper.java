package swing;

import rmi.observer.ObservableRMI;
import rmi.observer.RemoteObserver;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ObserverModelWrapper extends UnicastRemoteObject implements RemoteObserver {

    DatabaseModel model;

    public ObserverModelWrapper(DatabaseModel m) throws RemoteException {
        model = m;
    }

    @Override
    public void update(ObservableRMI o) throws RemoteException {
        System.out.println("calling update on client...");
        model.updateModel();
    }
}