import java.util.List;

/**
 * Client process class
 */
public class ClientProcess implements Runnable {

    IProcess process;
    int numMessages;
    List<Integer> delays;

    public ClientProcess(IProcess process, int numMessages, List<Integer> delays) {
        this.process = process;
        this.numMessages = numMessages;
        this.delays = delays;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            for (int i = 0; i < this.numMessages; i++) {
                Thread.sleep(delays.get(i));
                process.sendMessage();

            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
