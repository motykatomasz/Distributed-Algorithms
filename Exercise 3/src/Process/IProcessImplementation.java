package Process;

import Utils.Edge;
import Utils.EdgeState;
import Utils.Message;
import Utils.MessageType;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    private Map<Double, Long> lastDelayByEdge;

    //Metrics
    public AtomicInteger ConnectSent;
    public AtomicInteger TestSent;
    public AtomicInteger InitiateSent;
    public AtomicInteger ReportSent;
    public AtomicInteger AcceptSent;
    public AtomicInteger RejectSent;
    public AtomicInteger ChangeRootSent;
    public AtomicInteger ConnectReceived;
    public AtomicInteger TestReceived;
    public AtomicInteger InitiateReceived;
    public AtomicInteger ReportReceived;
    public AtomicInteger AcceptReceived;
    public AtomicInteger RejectReceived;
    public AtomicInteger ChangeRootReceived;
    public AtomicInteger merges;
    public AtomicInteger absorbs;
    public Map<Double, Edge> cores;
    public Map<Double, Edge> mstEdges;
    public Map<Integer, List<Edge>> levels;

    private AtomicBoolean wasAlreadyReported = new AtomicBoolean(false);

    public IProcessImplementation(int id, String name, int machineId) throws RemoteException {
        super();
        this.id = id;
        this.machineId = machineId;
        this.name = name;
        this.fragmentName = id;
        this.fragmentLevel = 0;
        this.state = ProcessState.SLEEPING;
        initializeMetrics();
        cores = new HashMap<>();
        mstEdges = new HashMap<>();
        levels = new HashMap<>();
    }

    private void initializeMetrics() {
        ConnectSent = new AtomicInteger(0);
        TestSent = new AtomicInteger(0);
        InitiateSent = new AtomicInteger(0);
        ReportSent = new AtomicInteger(0);
        AcceptSent = new AtomicInteger(0);
        RejectSent = new AtomicInteger(0);
        ChangeRootSent = new AtomicInteger(0);
        ConnectReceived = new AtomicInteger(0);
        TestReceived = new AtomicInteger(0);
        InitiateReceived = new AtomicInteger(0);
        ReportReceived = new AtomicInteger(0);
        AcceptReceived = new AtomicInteger(0);
        RejectReceived = new AtomicInteger(0);
        ChangeRootReceived = new AtomicInteger(0);
        merges = new AtomicInteger(0);
        absorbs = new AtomicInteger(0);
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
        mstEdges.put(edge.getWeight(), edge);
        System.out.println(id + " changes state of edge to " + edge.getTo() + " to IN_MST");
        System.out.println("(" + edge.getFrom() + "," + edge.getTo() + ") in MST.");

        state = ProcessState.FOUND;
        findCount = 0;
        Message msg = new Message(MessageType.CONNECT, 0);
        System.out.println(id + " sends Connect to " + edge.getTo() + " - WakeUp");
        ConnectSent.getAndIncrement();
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
                getDelay(edge));
    }

    @Override
    public synchronized void receive(Message message, Edge edge) throws RemoteException {
        Edge oppositeEdge = getOppositeEdge(edge);
        switch (message.getType()) {
            case TEST:
                TestReceived.getAndIncrement();
                receiveTest(message, oppositeEdge);
                break;
            case ACCEPT:
                AcceptReceived.getAndIncrement();
                receiveAccept(message, oppositeEdge);
                break;
            case CONNECT:
                ConnectReceived.getAndIncrement();
                receiveConnect(message, oppositeEdge);
                break;
            case INITIATE:
                InitiateReceived.getAndIncrement();
                receiveInitiate(message, oppositeEdge);
                break;
            case REPORT:
                ReportReceived.getAndIncrement();
                receiveReport(message, oppositeEdge);
                break;
            case REJECT:
                RejectReceived.getAndIncrement();
                receiveReject(message, oppositeEdge);
                break;
            case CHANGE_ROOT:
                ChangeRootReceived.getAndIncrement();
                receiveChangeRoot(message, oppositeEdge);
                break;
        }
    }

    @Override
    public synchronized void receiveConnect(Message message, Edge edge) throws RemoteException {
        System.out.println(id + " receives Connect from process " + edge.getTo());
        if (state == ProcessState.SLEEPING)
            wakeUp();

        if (message.getLevel() < fragmentLevel) {
            edge.setState(EdgeState.IN_MST);
            System.out.println("(" + edge.getFrom() + "," + edge.getTo() + ") in MST.");
            Message msg = new Message(MessageType.INITIATE, fragmentLevel, fragmentName, state);
            msg.setIsAbsorbMessage(true);
            System.out.println(id + " sends Initiate to " + edge.getTo());
            mstEdges.put(edge.getWeight(), edge);
            InitiateSent.getAndIncrement();
            absorbs.getAndIncrement();
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
                    getDelay(edge));
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
                                    ConnectReceived.getAndDecrement();
                                    otherProcesses[edge.getFrom()].receive(message, edge);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        (int) (Math.random() * 1000 + 500));
                System.out.println(id + " appends Connect message from process " + edge.getTo() + " to queue");
            } else {

                Message msg = new Message(MessageType.INITIATE, fragmentLevel + 1, edge.getWeight(), ProcessState.FIND);
                System.out.println(id + " sends Initiate to " + edge.getTo() + " with increased fragmentLevel");
                InitiateSent.getAndIncrement();
                merges.getAndIncrement();
                cores.put(edge.getWeight(), edge);
                List<Edge> tt;
                if (levels.containsKey(fragmentLevel + 1))
                    tt = levels.get(fragmentLevel + 1);
                else
                    tt = new ArrayList<Edge>();
                tt.add(edge);
                levels.put(fragmentLevel + 1, tt);
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
                        getDelay(edge));
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

                Message msg = new Message(MessageType.INITIATE, fragmentLevel, fragmentName, state);
                InitiateSent.getAndIncrement();
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    otherProcesses[e.getTo()].receive(msg, e);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        getDelay(e));
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

            Message msg = new Message(MessageType.TEST, fragmentLevel, fragmentName);
            Edge tmpTestEdge = new Edge(testEdge);
            TestSent.getAndIncrement();
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                otherProcesses[tmpTestEdge.getTo()].receive(msg, tmpTestEdge);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    getDelay(tmpTestEdge));
        } else {
            System.out.println(id + " has no adjacent candidates");
            report();
        }
    }

    @Override
    public synchronized void receiveTest(Message message, Edge edge) throws RemoteException {
        System.out.println(id + " received Test from process " + edge.getTo());
        if (state == ProcessState.SLEEPING)
            wakeUp();

        if (message.getLevel() > fragmentLevel) {
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                System.out.println(id + " retrieves Test message of process " + edge.getTo() + " from the queue");
                                TestReceived.getAndDecrement();
                                otherProcesses[edge.getFrom()].receive(message, edge);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    (int) (Math.random() * 1000 + 500));
            System.out.println(id + " appends Test message from process " + edge.getTo() + " to queue");
        } else {
            if (message.getFragmentName() != fragmentName) {
                Message msg = new Message(MessageType.ACCEPT);
                System.out.println(id + " sends Accept to process " + edge.getTo());
                AcceptSent.getAndIncrement();
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
                        getDelay(edge));

            } else {
                if (EdgeState.CANDIDATE == edge.getState()) {
                    edge.setState(EdgeState.NOT_IN_MST);
                }
                if (testEdge == null || testEdge.compareTo(edge) != 0) {
                    Message msg = new Message(MessageType.REJECT);
                    System.out.println(id + " sends Reject to process " + edge.getTo());
                    RejectSent.getAndIncrement();
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
//                            delay);
                            getDelay(edge));
                } else {
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

            Edge tmpInBranch = new Edge(inBranch);
            ReportSent.getAndIncrement();
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                otherProcesses[tmpInBranch.getTo()].receive(msg, tmpInBranch);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    getDelay(tmpInBranch)
            );
        }
    }


    @Override
    public synchronized void receiveReport(Message message, Edge edge) throws RemoteException {
        System.out.println(id + " receives Report message from process " + edge.getTo());
        if (edge.compareTo(inBranch) != 0) {
            findCount--;
            System.out.println(id + " decreases its FindCount");
            if (message.getWeight() < bestWeight) {
                bestWeight = message.getWeight();
                bestEdge = edge;
                System.out.println(id + " found new best edge");
            }
            report();
        } else {
            if (state == ProcessState.FIND) {
                System.out.println(id + " appends Report message of process " + edge.getTo() + " to queue");
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    System.out.println(id + " retrieves Report message of process " + edge.getTo() + " from the queue");
                                    ReportReceived.getAndDecrement();
                                    otherProcesses[edge.getFrom()].receive(message, edge);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        (int) (Math.random() * 1000 + 500));

            } else {
                if (message.getWeight() > bestWeight)
                    changeRoot();
                else {
                    if (message.getWeight() == bestWeight && bestWeight == Double.POSITIVE_INFINITY) {
                        System.out.println("Process " + id + " HALTS.");
                        System.out.println("Final level " + this.fragmentLevel);
                        printStats();
                    }
                }
            }
        }
    }

    @Override
    public synchronized void changeRoot() throws RemoteException {
        if (bestEdge.getState() == EdgeState.IN_MST) {
            Message msg = new Message(MessageType.CHANGE_ROOT);
            System.out.println(id + " sends ChangeRoot message to process " + bestEdge.getTo());

            Edge tmpBestEdge = new Edge(bestEdge);
            ChangeRootSent.getAndIncrement();
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                otherProcesses[tmpBestEdge.getTo()].receive(msg, tmpBestEdge);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    getDelay(tmpBestEdge)
            );
        } else {
            Message msg = new Message(MessageType.CONNECT, fragmentLevel);
            System.out.println(id + " sends Connect message to process " + bestEdge.getTo() + " - ChangeRoot");

            Edge tmpBestEdge = new Edge(bestEdge);
            ConnectSent.getAndIncrement();
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            try {
                                otherProcesses[tmpBestEdge.getTo()].receive(msg, tmpBestEdge);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    getDelay(tmpBestEdge)
            );
            bestEdge.setState(EdgeState.IN_MST);
            mstEdges.put(bestEdge.getWeight(), bestEdge);
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

    private synchronized int getDelay(Edge edge) {
        long lastDelay = this.lastDelayByEdge.get(edge.getWeight());
        long currentTime = System.currentTimeMillis();
        int newDelay = (int) (Math.random() * 1000 + 1000);
        long diff = (currentTime + newDelay) - lastDelay;

        if (diff < 0)
            newDelay = (int) (newDelay - diff);

        this.lastDelayByEdge.put(edge.getWeight(), currentTime + newDelay);
        return newDelay;
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
        this.lastDelayByEdge = new HashMap<>();
        for (Edge e : edges) {
            lastDelayByEdge.put(e.getWeight(), System.currentTimeMillis());
        }
    }

    @Override
    public Map<String, Integer> getMetrics() throws RemoteException {
        Map<String, Integer> stat = new HashMap<String, Integer>();
        stat.put("ConnectSent", ConnectSent.get());
        stat.put("InitiateSent", InitiateSent.get());
        stat.put("AcceptSent", AcceptSent.get());
        stat.put("RejectSent", RejectSent.get());
        stat.put("TestSent", TestSent.get());
        stat.put("ReportSent", ReportSent.get());
        stat.put("ChangeRootSent", ChangeRootSent.get());
        stat.put("ConnectReceived", ConnectReceived.get());
        stat.put("InitiateReceived", InitiateReceived.get());
        stat.put("AcceptReceived", AcceptReceived.get());
        stat.put("RejectReceived", RejectReceived.get());
        stat.put("TestReceived", TestReceived.get());
        stat.put("ReportReceived", ReportReceived.get());
        stat.put("ChangeRootReceived", ChangeRootReceived.get());
        stat.put("Merges", merges.get());
        stat.put("Absorbs", absorbs.get());
        return stat;
    }

    public void printStats() throws RemoteException {
        Map<String, Integer> stats = null;
        Map<String, Integer> temp;
        int t;
        for (IProcess node : otherProcesses) {
            if (node.getId() == id) {
                temp = getMetrics();
            } else {
                temp = node.getMetrics();
                for (Double w : node.getMST().keySet()) {
                    if (!mstEdges.containsKey(w))
                        mstEdges.put(w, node.getMST().get(w));
                }
                for (Double w : node.getCores().keySet()) {
                    if (!cores.containsKey(w))
                        cores.put(w, node.getCores().get(w));
                }
                for (int i : node.getLevels().keySet()) {
                    if (!levels.containsKey(i)) {
                        levels.put(i, node.getLevels().get(i));
                    } else {
                        List<Edge> tt;
                        for (Edge l : node.getLevels().get(i)) {
                            if (!levels.get(i).stream().anyMatch(p -> p.getWeight() == l.getWeight())) {
                                tt = levels.get(i);
                                tt.add(l);
                                levels.put(i, tt);
                            }
                        }
                    }
                }
            }
            if (stats == null)
                stats = new HashMap<>(temp);
            else {
                for (String k : stats.keySet()) {
                    t = stats.get(k);
                    stats.put(k, t + temp.get(k));
                }
            }
        }
        t = stats.get("Merges");
        stats.put("Merges", t / 2);
        presentStats(stats);
    }

    @Override
    public Map<Double, Edge> getCores() throws RemoteException {
        return cores;
    }

    @Override
    public Map<Double, Edge> getMST() throws RemoteException {
        return mstEdges;
    }

    @Override
    public Map<Integer, List<Edge>> getLevels() throws RemoteException {
        return levels;
    }

    private void presentStats(Map<String, Integer> stats) {
        for (String k : stats.keySet()) {
            System.out.println(k + ": " + stats.get(k));
        }
        System.out.println("---------- MST ----------");
        for (Double w : mstEdges.keySet()) {
            System.out.println("(" + mstEdges.get(w).getFrom() + " - " + mstEdges.get(w).getTo() + ")");
        }
        System.out.println("---------- Cores at each level ----------");
        for (int l : levels.keySet()) {
            System.out.println("At level " + l);
            for (Edge e : levels.get(l)) {
                System.out.println("(" + e.getFrom() + " - " + e.getTo() + ")");
            }
        }
    }
}