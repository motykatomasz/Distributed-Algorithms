package Process;

import Utils.Edge;
import Utils.EdgeState;
import Utils.Message;
import Utils.MessageType;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
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
    private double fragmentName;
    private ProcessState state;
    private Edge bestEdge; //the adjacent edge leading towards the best candidate for the MOE it knows about
    private Edge testEdge;
    private Edge inBranch; //the adjacent edge leading to the core of the fragment
    private double bestWeight; //weight of the current candidate MOE
    private int findCount;
    private int delay = 500;

    public IProcessImplementation(int id, String name, int machineId) throws RemoteException {
        super();
        this.id = id;
        this.machineId = machineId;
        this.name = name;
        this.fragmentName = id;
        this.fragmentLevel = 0;
        this.state = ProcessState.SLEEPING;
    }

    @Override
    public synchronized void wakeUp() throws RemoteException {
        if (ProcessState.SLEEPING != state) {
            System.out.println(id + " already woke up");
            return;
        }

        System.out.println(id + " wakes up.");

        Edge edge = edges.peek();
        edge.setState(EdgeState.IN_MST);
        System.out.println(id + " changes state of edge to " + edge.getTo() + " to IN_MST");
        System.out.println("(" + edge.getFrom() + "," + edge.getTo() + ") in MST.");

        state = ProcessState.FOUND;
        findCount = 0;
        Message msg = new Message(MessageType.CONNECT, 0);
        System.out.println(id + " sends Connect to " + edge.getTo());
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        try {
                            int receiverId = edge.getTo();
                            otherProcesses[receiverId].receive(msg, edge);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                delay);
    }

    @Override
    public synchronized void receive(Message message, Edge edge) throws RemoteException {
        Edge oppositeEdge = getOppositeEdge(edge);
        switch (message.getType()) {
            case TEST:
                receiveTest(message, oppositeEdge);
                break;
            case ACCEPT:
                receiveAccept(message, oppositeEdge);
                break;
            case CONNECT:
                receiveConnect(message, oppositeEdge);
                break;
            case INITIATE:
                receiveInitiate(message, oppositeEdge);
                break;
            case REPORT:
                receiveReport(message, oppositeEdge);
                break;
            case REJECT:
                receiveReject(message, oppositeEdge);
                break;
            case CHANGE_ROOT:
                receiveChangeRoot(message, oppositeEdge);
                break;
        }
    }

    @Override
    public synchronized void receiveConnect(Message message, Edge edge) throws RemoteException {
        System.out.println("Process " + id + " received Connect from process " + edge.getTo());
        if (state == ProcessState.SLEEPING)
            wakeUp();

        if (message.getLevel() < fragmentLevel) {
            edge.setState(EdgeState.IN_MST);
            System.out.println("(" + edge.getFrom() + "," + edge.getTo() + ") in MST.");
            Message msg = new Message(MessageType.INITIATE, fragmentLevel, fragmentName, state);
            msg.setIsAbsorbMessage(true);
            System.out.println(id + " sends Initiate to " + edge.getTo());
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                otherProcesses[edge.getTo()].receive(msg, edge);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    delay);
            if (state == ProcessState.FIND) {
                System.out.println(id + " increments its findCount");
                findCount++;
            }
        } else {
            if (edge.getState() == EdgeState.CANDIDATE) {
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    System.out.println(id + " retrieves Connect message of process " + edge.getTo() + " from the queue");
                                    otherProcesses[edge.getFrom()].receive(message, edge);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        (int) (Math.random() * 2000 + 1000));
                System.out.println(id + " appends Connect message from process " + edge.getTo() + " to queue");
            } else {

                Message msg = new Message(MessageType.INITIATE, fragmentLevel + 1, edge.getWeight(), ProcessState.FIND);
                System.out.println(id + " sends Initiate to " + edge.getTo() + " with increased fragmentLevel");
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    otherProcesses[edge.getTo()].receive(msg, edge);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        delay);
            }
        }
    }

    @Override
    public synchronized void receiveInitiate(Message message, Edge edge) throws RemoteException {
        System.out.println(id + " received Initiate from " + edge.getTo());
        if (fragmentLevel < message.getLevel() && message.getIsAbsorbMessage())
            System.out.println("Fragment " + fragmentName + " is absorbed.");
        else if (fragmentLevel < message.getLevel())
            System.out.println("Fragment " + fragmentName + " merges.");
        fragmentLevel = message.getLevel();
        fragmentName = message.getFragmentName();
        state = message.getSenderState();
        inBranch = edge;
        bestEdge = null;
        bestWeight = Double.POSITIVE_INFINITY;

        for (Edge e : edges) {
            if (edge.compareTo(e) != 0 && EdgeState.IN_MST == e.getState()) {
                System.out.println(id + " sends Initiate to " + e.getTo());
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    Message msg = new Message(MessageType.INITIATE, fragmentLevel, fragmentName, state);
                                    otherProcesses[e.getTo()].receive(msg, e);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        delay);
                if (ProcessState.FIND == state) {
                    System.out.println(id + " increases findCount");
                    findCount++;
                }
            }
        }

        if (ProcessState.FIND == state) {
            test();
        }

    }

    @Override
    public synchronized void test() throws RemoteException {
        System.out.println(id + " executes Test procedure");
        double minWeight = Double.POSITIVE_INFINITY;
        testEdge = null;
        for (Edge e : edges) {
            if (EdgeState.CANDIDATE == e.getState() && e.getWeight() <= minWeight) {
                testEdge = e;
                minWeight = e.getWeight();
            }
        }

        if (testEdge != null) {
            System.out.println(id + " sends Test to process " + testEdge.getTo());
            Edge tmpEdge = new Edge(testEdge);
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                Message msg = new Message(MessageType.TEST, fragmentLevel, fragmentName);
                                otherProcesses[tmpEdge.getTo()].receive(msg, tmpEdge);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    delay);
        } else {
            System.out.println(id + " has no adjacent candidates");
            report();
        }
    }

    @Override
    public synchronized void receiveTest(Message message, Edge edge) throws RemoteException {
        System.out.println("Process " + id + " received Test from process " + edge.getTo());
        if (state == ProcessState.SLEEPING)
            wakeUp();

        if (message.getLevel() > fragmentLevel) {
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                System.out.println(id + " retrieves Test message of process " + edge.getTo() + " from the queue");
                                otherProcesses[edge.getFrom()].receive(message, edge);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    (int) (Math.random() * 2000 + 1000));
            System.out.println(id + " appends Test message from process " + edge.getTo() + " to queue");
        }
        else {
            if (message.getFragmentName() != fragmentName) {
                Message msg = new Message(MessageType.ACCEPT);
                System.out.println(id + " sends Accept to process " + edge.getTo());
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    otherProcesses[edge.getTo()].receive(msg, edge);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        delay);

            }
            else {
                if (EdgeState.CANDIDATE == edge.getState()) {
                    edge.setState(EdgeState.NOT_IN_MST);
                }
                if (testEdge == null || testEdge.compareTo(edge) != 0) {
                    Message msg = new Message(MessageType.REJECT);
                    System.out.println(id + " sends Reject to process " + edge.getTo());
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    try {
                                        otherProcesses[edge.getTo()].receive(msg, edge);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            delay);
                }
                else {
                    test();
                }
            }
        }

    }

    @Override
    public synchronized void receiveReject(Message message, Edge edge) throws RemoteException {
        System.out.println("Process " + id + " received Reject from process " + edge.getTo());
        if (edge.getState() == EdgeState.CANDIDATE) {
            System.out.println(id + " changes edge to " + edge.getTo() + " to NOT_IN_MST");
            edge.setState(EdgeState.NOT_IN_MST);
        }
        test();
    }

    @Override
    public synchronized void receiveAccept(Message message, Edge edge) throws RemoteException {
        System.out.println("Process " + id + " received Accept from process " + edge.getTo());
        testEdge = null;
        if (edge.getWeight() < bestWeight) {
            bestEdge = edge;
            bestWeight = edge.getWeight();
        }
        report();

    }

    @Override
    public synchronized void report() throws RemoteException {
        System.out.println(id + " executes report procedure.");
        if (testEdge != null) {
            System.out.println(id + " testing edge from " + testEdge.getFrom() + " to " + testEdge.getTo());
        }
        if (findCount == 0 && testEdge == null) {
            this.state = ProcessState.FOUND;
            Message msg = new Message(MessageType.REPORT, bestWeight);
            System.out.println(id + " sends Report to process " + inBranch.getTo());
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                otherProcesses[inBranch.getTo()].receive(msg, inBranch);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    delay
            );
        }
    }


    @Override
    public synchronized void receiveReport(Message message, Edge edge) throws RemoteException {
        System.out.println(id + " receives Report message from process " + edge.getTo());
        if (edge.compareTo(inBranch) != 0){
            findCount--;
            System.out.println(id + " decreases its FindCount");
            if (message.getWeight() < bestWeight){
                bestWeight = message.getWeight();
                bestEdge = edge;
                System.out.println(id + " found new best edge");
            }
            report();
        }
        else {
            if (state == ProcessState.FIND){
                System.out.println(id + " appends Report message of process " + edge.getTo() + " to queue");
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    System.out.println(id + " retrieves Report message of process " + edge.getTo() + " from the queue");
                                    otherProcesses[edge.getFrom()].receive(message, edge);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        (int) (Math.random() * 2000 + 1000));
            } else {
                if (message.getWeight() > bestWeight)
                    changeRoot();
                else {
                    if (message.getWeight() == bestWeight && bestWeight == Double.POSITIVE_INFINITY) {
                        System.out.println("Process " + id + " HALTS.");
                        //for (Edge e : edges)
                            //if (e.getState() == EdgeState.IN_MST)
                                //System.out.println("(" + e.getFrom() + "," + e.getTo() + ") in MST.");
                    }
                }
            }
        }
    }

    @Override
    public synchronized void changeRoot() throws RemoteException {
        if (bestEdge.getState() == EdgeState.IN_MST){
            Message msg = new Message(MessageType.CHANGE_ROOT);
            System.out.println(id + " sends ChangeRoot message to process " + bestEdge.getTo());
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                otherProcesses[bestEdge.getTo()].receive(msg, bestEdge);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    delay
            );
        } else {
            Message msg = new Message(MessageType.CONNECT, fragmentLevel);
            System.out.println(id + " sends Connect message to process " + bestEdge.getTo());
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                otherProcesses[bestEdge.getTo()].receive(msg, bestEdge);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    delay
            );
            bestEdge.setState(EdgeState.IN_MST);
            System.out.println("(" + bestEdge.getFrom() + "," + bestEdge.getTo() + ") in MST.");
        }
    }

    @Override
    public synchronized void receiveChangeRoot(Message message, Edge edge) throws RemoteException {
        System.out.println(id + " receives ChangeRoot message from process " + edge.getTo());
        changeRoot();
    }

    private synchronized Edge getOppositeEdge(Edge edge) {
        for (Edge e : edges) {
            if (e.compareTo(edge) == 0) {
                return e;
            }
        }
        return null;
    }

    @Override
    public synchronized void printMsg() throws RemoteException {
        System.out.println("Process " + this.id + " has " + this.edges.size() + " outgoing edges.");
        for (Edge e : edges) {
            System.out.println(id + " to " + e.getTo() + " with weight " + e.getWeight());
        }
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