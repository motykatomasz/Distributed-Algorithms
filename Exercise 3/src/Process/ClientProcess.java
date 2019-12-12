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
//            if(process.getId() == 0 || process.getId() == 7) {
                Thread.sleep((int) (Math.random() * 3000 + 1000));
                process.wakeUp();
//            }
        } catch (
                Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
