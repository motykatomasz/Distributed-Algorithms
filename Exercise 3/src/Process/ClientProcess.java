package Process;

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
            Thread.sleep(2000);
            process.printMsg();
        } catch (
                Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
