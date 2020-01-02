package Process;

import Utils.Edge;
import Utils.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Remote object Interface
 */
public interface IProcess extends Remote {

    void printMsg() throws RemoteException;

    void wakeUp() throws RemoteException;

    void receive(Message message, Edge edge) throws RemoteException;

    void receiveAccept(Message message, Edge edge) throws RemoteException;

    void receiveChangeRoot(Message message, Edge edge) throws RemoteException;

    void receiveConnect(Message message, Edge edge) throws RemoteException;

    void receiveInitiate(Message message, Edge edge) throws RemoteException;

    void receiveTest(Message message, Edge edge) throws RemoteException;

    void receiveReject(Message message, Edge edge) throws RemoteException;

    void receiveReport(Message message, Edge edge) throws RemoteException;

    void test() throws RemoteException;

    void report() throws RemoteException;

    void changeRoot() throws RemoteException;

    Integer getId() throws RemoteException;

    Integer getMachineId() throws RemoteException;

    void addOtherProcesses(IProcess[] m) throws RemoteException;

    void addAdjacentEdges(Queue<Edge> edges) throws RemoteException;

    public Map<String,Integer> getMetrics() throws RemoteException;

    public Map<Double,Edge> getCores() throws RemoteException;

    public Map<Double,Edge> getMST() throws RemoteException;

    public Map<Integer, List<Edge>> getLevels() throws RemoteException;

}
