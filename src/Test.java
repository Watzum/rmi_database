import rmi.Server;
import swing.SwingClient;

public class Test {
    public static void main(String[] args) {
        try {
            //Server server = new Server("127.0.0.1", "./src/machines.db");
            SwingClient client = new SwingClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
