package swing;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;

public class SwingClient {

    public static void main(String[] args) {
        SwingClient sw = new SwingClient();
    }

    public SwingClient() {
        SwingUtilities.invokeLater(() -> {
            try {
                final DatabaseModel dm = new DatabaseModel("127.0.0.1");
                final MainView mw = new MainView();
                MainViewController mvw = new MainViewController(mw, dm);
            } catch (FileNotFoundException | RemoteException e) {
                e.printStackTrace();
            }
        });
    }
}
