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
     * Vector clock associated with the process.
     */
    private VectorClock clock;

    /**
     * List of other processes.
     */
    private IProcess[] otherProcesses;

    /**
     * Local buffer for message ordering.
     */
    private Buffer buffer;

    /**
     * Test msg to send
     *
     * @param name
     * @throws RemoteException
     */
    private List<Message> msgToSend;

    IProcessImplementation(int id, String name) throws RemoteException {
        super();
        this.id = id;
        this.name = name;
        this.msgToSend = new ArrayList<>();
        this.buffer = new Buffer();
    }

    @Override
    public synchronized void printMsg() throws RemoteException {
        System.out.println("Hello " + this.name);
    }

    @Override
    public synchronized void sendMessage() throws RemoteException {
        //TODO Sending a message by the process according to the Schiper-Eggli-Sandoz algorithm.
        if (!this.msgToSend.isEmpty()) {
            Message m = this.msgToSend.get(0);
            System.out.println("Process " + this.id + " sending a message " + m.getContent() + " to process " + m.getReceiver());
            otherProcesses[m.getReceiver()].receive(m);
        }
    }

    @Override
    public synchronized void receive(Message m) throws RemoteException {
        //TODO Receiving a message by the process according to the Schiper-Eggli-Sandoz algorithm.
        System.out.println("Process " + this.id + " received a message " + m.getContent() + " from process " + m.getSender());
    }

    @Override
    public synchronized void deliver(Message m) throws RemoteException {
        //TODO Delivering a delayed massage according to the Schiper-Eggli-Sandoz algorithm.
    }

    @Override
    public String toString() {
        return "Client {" +
                "id='" + name + '\'' +
                '}';
    }

    @Override
    public void addMessagesToSend(List<Message> m) {
        if (m != null) {
            this.msgToSend.addAll(m);
        }
    }

    @Override
    public void addOtherProcesses(IProcess[] otherProcesses) {
        this.otherProcesses = otherProcesses;
    }

    @Override
    public void setVectorClock(int id, int n) {
        this.clock = new VectorClock(id, n);
    }

    @Override
    public Integer getId() {
        return this.id;
    }
}
