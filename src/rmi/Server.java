package rmi;

import rmi.observer.ObservableDataRMI;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {

    public static void main(String[] args) {
        try {
            ObservableDataRMI o = new ObservableDataRMI();
            Server s = new Server("127.0.0.1", "./src/machines.db", o);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private final String host_ip;
    private final DataRMI data;

    public Server(String ip, String filePath) throws RemoteException {
        host_ip = ip;
        data = new DataRMI(filePath);
        setUpServer();
    }

    public Server(String ip, String filePath, ObservableDataRMI o) throws RemoteException {
        host_ip = ip;
        data = new DataRMI(filePath, o);
        setUpServer();
    }

    private void setUpServer() {
        System.setProperty("java.rmi.server.hostname", host_ip);
        try {
            LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        addDataToServer();
    }


    private void addDataToServer() {
        try {
            Naming.rebind("rmi://" + host_ip + "/Data", data);
        } catch (MalformedURLException | RemoteException e) {
            System.out.println(e.getMessage());
        }
    }
}
