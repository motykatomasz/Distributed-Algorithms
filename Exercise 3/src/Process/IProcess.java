package Process;

import Utils.Edge;
import Utils.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;
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

    Integer getId() throws RemoteException;

    Integer getMachineId() throws RemoteException;

    void addOtherProcesses(IProcess[] m) throws RemoteException;

    void addAdjacentEdges(Queue<Edge> edges) throws RemoteException;

}
