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
            this.process.sendMessage();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
