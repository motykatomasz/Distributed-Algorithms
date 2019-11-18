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
     * Local message buffer.
     */
    private MessageBuffer S;

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
        this.S = new MessageBuffer();
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
            this.clock.incerementClock(this.id);
            m.setMessageBuffer(this.S.clone());
            m.setVectorClock(new VectorClock(m.getId(), this.clock.clone()));
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                otherProcesses[m.getReceiver()].receive(m);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    m.getDeliveryTime()
            );
            System.out.println("Process " + this.id + " sending a message " + m.getContent() + " to process " + m.getReceiver());
            this.S.put(m.getReceiver(), new VectorClock(m.getId(), this.clock.clone()));
            this.msgToSend.remove(0);
        }
    }

    @Override
    public synchronized void receive(Message m) throws RemoteException {
        //TODO Receiving a message by the process according to the Schiper-Eggli-Sandoz algorithm.
        MessageBuffer receivedMessageBuffer = m.getMessageBuffer();
        boolean receivedBufferContainsId = receivedMessageBuffer.contains(this.id);
        System.out.println("Process " + this.id + " received a message " + m.getContent() + " from process " + m.getSender());
        if (!receivedBufferContainsId || receivedMessageBuffer.get(this.id).smallerOrEqual(this.clock)) {
            deliver(m);
            Message queuedMessage = this.buffer.peek();
            if (queuedMessage != null) {
                MessageBuffer queuedMessageBuffer = queuedMessage.getMessageBuffer();
                while (!queuedMessageBuffer.contains(this.id) || queuedMessageBuffer.get(this.id).smallerOrEqual(this.clock)) {
                    deliver(queuedMessage);
                    this.buffer.poll();
                    queuedMessage = this.buffer.peek();
                    if (queuedMessage == null)
                        break;
                    queuedMessageBuffer = queuedMessage.getMessageBuffer();
                }
            }
        } else {
            System.out.println("Message " + m.getContent() + " cannot be delivered to process " + this.id + ". Putting to buffer.");
            this.buffer.add(m);
        }

    }

    @Override
    public synchronized void deliver(Message m) throws RemoteException {
        //TODO Delivering a delayed massage according to the Schiper-Eggli-Sandoz algorithm.
        System.out.println("Message " + m.getContent() + " has been delivered to process " + m.getReceiver()
                + " by process " + m.getSender());
        this.clock.setTimeVector(this.clock.merge(m.getVectorClock()));
        this.clock.incerementClock(this.id);
        for (int i = 0; i < this.otherProcesses.length; i++) {
            if (m.getMessageBuffer().contains(i)) {
                S.merge(i, m.getMessageBuffer().get(i));
            }
        }
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
