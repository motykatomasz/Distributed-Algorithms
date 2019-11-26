import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Remote Object Implementation
 */
public class IProcessImplementation extends UnicastRemoteObject implements IProcess {

    private static final long serialVersionUID = -9148998192432372389L;

    /**
     * Process name.
     */
    private String name;

    /**
     * Process id.
     */
    private int id;

    /**
     * Id of a machine the process should run on.
     */
    private int machineId;

    /**
     * List of other processes.
     */
    private IProcess[] otherProcesses;

    private int[] N;

    private Token token;

    private boolean tokenPresent;

    private boolean isInCriticalSection;

    IProcessImplementation(int id, String name, int machineId, boolean tokenPresent) throws RemoteException {
        super();
        this.id = id;
        this.machineId = machineId;
        this.name = name;
        this.tokenPresent = tokenPresent;
        this.isInCriticalSection = false;
    }

    @Override
    public synchronized void printMsg() throws RemoteException {
        System.out.println("Hello " + this.name);
    }

    @Override
    public synchronized void sendMessage() throws RemoteException {
        //TODO Broadcasting a message by the process according to the Suzuki-Kasami algorithm.
        N[id] = N[id] + 1;
        System.out.println("Process " + this.id + " broadcasts a request with " + N[id]);
        for (IProcess p : otherProcesses) {
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                p.receiveMessage(id, N[id]);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    200
            );
        }
    }

    @Override
    public synchronized void receiveMessage(int senderId, int numReq) throws RemoteException {
        //TODO Receiving a message by the process according to the Suzuki-Kasami algorithm
        N[senderId] = numReq;
        System.out.println("Process " + id + " received broadcast request with number " + numReq + " from process " + senderId);
        if (tokenPresent && !isInCriticalSection && (N[senderId] > token.getTN(senderId))) {
            tokenPresent = false;
            System.out.println("Process " + id + " sending a token to process " + senderId + ". TN = " + token.toString());
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                otherProcesses[senderId].receiveToken(token);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    200
            );
        }
    }

    @Override
    public synchronized void receiveToken(Token m) throws RemoteException {
        //TODO Receiving a token according to the Suzuki-Kasami algorithm.
        System.out.println("Process " + id + " received a token with TN = " + m.toString());
        tokenPresent = true;
        token = m;
        criticalSection();
        token.setTN(id, N[id]);
        System.out.println("Updating a token array after leaving critical section. TN = " + token.toString());

        for (int i : getListofForwardProcesses()) {
            if (N[i] > token.getTN(i)) {
                tokenPresent = false;
                otherProcesses[i].receiveToken(token);
                System.out.println("Process " + this.id + " send token after leaving his critical section to process "
                        + i + ". TN = " + token.toString());
                break;
            }
        }
    }

    private void criticalSection() {
        isInCriticalSection = true;
        System.out.println("Process " + this.id + " enters his critical section.");
        try {
            Thread.sleep( (int) (Math.random() * 4000 + 1000)); //Critical section lasts between 1 to 5 seconds
        } catch (InterruptedException e) {
            System.out.println("Critical section interrupted");
        }
        isInCriticalSection = false;
        System.out.println("Process " + id + " leaves his critical section.");
    }

    @Override
    public String toString() {
        return "Client {" +
                "id='" + name + '\'' +
                '}';
    }

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public Integer getMachineId() {
        return this.machineId;
    }

    @Override
    public void addOtherProcesses(IProcess[] otherProcesses) {
        this.otherProcesses = otherProcesses;
        this.N = new int[otherProcesses.length];
    }

    @Override
    public void setToken(Token t) {
        this.token = t;
        this.tokenPresent = true;
    }

    @Override
    public boolean isTokenPresent() {
        return this.tokenPresent;
    }

    private List<Integer> getListofForwardProcesses() {
        List<Integer> forwardProcesses = new ArrayList<>();
        for (int i = id + 1; i < otherProcesses.length; i++) {
            forwardProcesses.add(i);
        }

        for (int i = 0; i < id; i++) {
            forwardProcesses.add(i);
        }
        return forwardProcesses;
    }
}
