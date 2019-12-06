package Process;

import Utils.Edge;
import Utils.Message;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.PriorityQueue;
import java.util.Queue;

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

    /**
     * Adjacent edges
     */
    private Queue<Edge> edges;

    private int fragmentLevel;
    private String fragmentName;
    private ProcessState state;
    private Edge bestEdge; //the adjacent edge leading towards the best candidate for the MOE it knows about
    private Edge testEdge;
    private Edge inBranch; //the adjacent edge leading to the core of the fragment
    private double bestWeight; //weight of the current candidate MOE
    public int findCount;

    public IProcessImplementation(int id, String name, int machineId) throws RemoteException {
        super();
        this.id = id;
        this.machineId = machineId;
        this.name = name;
    }

    @Override
    public void wakeUp() throws RemoteException {

    }

    @Override
    public void receive(Message message, Edge edge) throws RemoteException {

    }

    @Override
    public void receiveAccept(Message message, Edge edge) throws RemoteException {

    }

    @Override
    public void receiveChangeRoot(Message message, Edge edge) throws RemoteException {

    }

    @Override
    public void receiveConnect(Message message, Edge edge) throws RemoteException {

    }

    @Override
    public void receiveInitiate(Message message, Edge edge) throws RemoteException {

    }

    @Override
    public void receiveTest(Message message, Edge edge) throws RemoteException {

    }

    @Override
    public void receiveReject(Message message, Edge edge) throws RemoteException {

    }

    @Override
    public void receiveReport(Message message, Edge edge) throws RemoteException {

    }

    @Override
    public synchronized void printMsg() throws RemoteException {
        System.out.println("Process " + this.id + " has " + this.edges.size() + " outgoing edges.");
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
    }

    @Override
    public void addAdjacentEdges(Queue<Edge> edges) {
        this.edges = new PriorityQueue<>(edges);
    }

}