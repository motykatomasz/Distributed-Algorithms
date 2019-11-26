import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote object Interface
 */
public interface IProcess extends Remote {

    void printMsg() throws RemoteException;

    void sendMessage() throws RemoteException;

    void receiveMessage(int processId, int numReq) throws RemoteException;

    void receiveToken(Token m) throws RemoteException;

    Integer getId() throws RemoteException;

    Integer getMachineId() throws RemoteException;

    void addOtherProcesses(IProcess[] m) throws RemoteException;

    void setToken(Token t) throws  RemoteException;

    boolean isTokenPresent() throws RemoteException;
}
