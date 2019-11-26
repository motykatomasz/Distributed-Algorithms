import java.util.List;

/**
 * Client process class
 */
public class ClientProcess implements Runnable {

    IProcess process;

    public ClientProcess(IProcess process) {
        this.process = process;
    }

    @Override
    public void run() {
        try {
            Thread.sleep( (int) (Math.random() * 9000 + 1000)); //wait 1 to 10 seconds before broadcasting request for critical section
            process.sendMessage();
        } catch (
                Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
