import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote object Interface
 */
public interface IProcess extends Remote {

    /**
     * For debugging.
     * @throws RemoteException
     */
    void printMsg() throws RemoteException;

    /**
     * Method for sending a message in point-to-point communication
     * @throws RemoteException
     */
    void sendMessage() throws RemoteException;

    /**
     * Method for receiving a message in point-to-point communication
     * @param m Message
     * @throws RemoteException
     */
    void receive(Message m) throws RemoteException;

    /**
     * Method for delivering a message in point-to-point communication
     * @param m Message
     * @throws RemoteException
     */
    void deliver(Message m) throws RemoteException;

    void addMessagesToSend(List<Message> m) throws RemoteException;

    void addOtherProcesses(IProcess[] m) throws RemoteException;

    void setVectorClock(int id, int n) throws RemoteException;

    Integer getId() throws RemoteException;
}
