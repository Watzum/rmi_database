package rmi;

import db.DB;
import exceptions.DuplicateKeyException;
import exceptions.RecordNotFoundException;
import rmi.observer.RemoteObserver;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.HashMap;

public class Client implements DB {

    public static void main(String[] args) {
        try {
            Client client = new Client("127.0.0.1");
            String[] r = client.read(11);
            client.printStringArray(r);

            //client.delete(22, 123);

            //client.update(123123, new String[0], 123);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final String server_ip;
    protected IDataRMI database;


    public Client(String server_ip) throws FileNotFoundException {
        this.server_ip = server_ip;
        try {
            setDataRMI();
        } catch (RemoteException e) {
            if (e.getMessage().contains("FileNotFoundException")) {
                throw new FileNotFoundException(e.getMessage());
            }
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    private void setDataRMI() throws MalformedURLException, NotBoundException, RemoteException {
        String rmi = "rmi://" + server_ip + ":" + Registry.REGISTRY_PORT
                        + "/Data";
        database = (IDataRMI) Naming.lookup(rmi);
    }


    public void printStringArray(String[] a) {
        System.out.print("| ");
        for (String s : a) {
            System.out.print(s);
            System.out.print(" |");
        }
        System.out.println();
    }

    public String[] read(int recNo) throws RecordNotFoundException {
        try {
            return database.read(recNo);
        } catch (RemoteException e) {
            throw new RecordNotFoundException(e.getMessage());
        }
    }

    public void update(int recNo, String[] data, long lockCookie)
            throws RecordNotFoundException, SecurityException {
        try {
            database.update(recNo, data, lockCookie);
        } catch (RemoteException e) {
            if (e.getMessage().contains("RecordNotFoundException")) {
                throw new RecordNotFoundException(e.getMessage());
            } else {
                throw new SecurityException(e.getMessage());
            }
        }
    }


    public int create(String[] data) throws DuplicateKeyException {
        try {
            return database.create(data);
        } catch (RemoteException e) {
            throw new DuplicateKeyException(e.getMessage());
        }
    }


    public void delete(int recNo, long lockCookie) throws
            RecordNotFoundException, SecurityException {
        try {
            database.delete(recNo, lockCookie);
        } catch (RemoteException e) {
            if (e.getMessage().contains("RecordNotFound")) {
                throw new RecordNotFoundException(e.getMessage());
            } else {
                throw new SecurityException(e.getMessage());
            }
        }
    }


    public int[] find(String[] criteria) {
        try {
            return database.find(criteria);
        } catch (RemoteException e) {
            System.out.println(e.getMessage());
            return new int[0];
        }
    }


    public long lock(int recNo) throws RecordNotFoundException {
        try {
            return database.lock(recNo);
        } catch (RemoteException e) {
            throw new RecordNotFoundException(e.getMessage());
        }
    }

    public long tryLock(int recNo) throws SecurityException, RecordNotFoundException, RemoteException {
        try {
            return database.tryLock(recNo);
        } catch (Exception e) {
            if (e.getMessage().contains("SecurityException")) {
                throw new SecurityException(e.getMessage());
            } else if (e.getMessage().contains("RecordNotFoundException")) {
                throw new RecordNotFoundException(e.getMessage());
            } else {
                throw new RemoteException(e.getMessage());
            }
        }
    }

    public void unlock(int recNo, long lockCookie)
            throws RecordNotFoundException, SecurityException {
        try {
            database.unlock(recNo, lockCookie);
        } catch (RemoteException e) {
            if (e.getMessage().contains("RecordNotFoundException")) {
                throw new RecordNotFoundException(e.getMessage());
            } else {
                throw new SecurityException(e.getMessage());
            }
        }
    }

    public HashMap[] getColumnInformation() throws RemoteException {
        return database.getColumnInformation();
    }

    public String[][] readAllRecords() throws RemoteException {
        return database.readAllRecords();
    }

    public int getRecNoWithDeletedRecords(int recNo) throws RemoteException{
        return database.getRecNoWithDeletedRecords(recNo);
    }
}
