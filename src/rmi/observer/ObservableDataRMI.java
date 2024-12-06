package rmi.observer;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class ObservableDataRMI implements ObservableRMI, Serializable {

    ArrayList<RemoteObserver> observers = new ArrayList<>();

    @Override
    public void addObserver(RemoteObserver ro) throws RemoteException {
        observers.add(ro);
    }

    @Override
    public void deleteObserver(RemoteObserver ro) {
        observers.remove(ro);
    }

    @Override
    public void notifyObservers() throws RemoteException {
        RemoteObserver currentObserver = null;
        System.out.println(observers.size());
        try {
            for (RemoteObserver ro : observers) {
                currentObserver = ro;
                ro.update(this);
            }
        } catch (RemoteException e) {
            observers.remove(currentObserver);
            notifyObservers();
            e.printStackTrace();
        }
    }
}
