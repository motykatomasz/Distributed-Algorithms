import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class Client {

    private static int numProcesses;

    public static void main(String[] args) throws IOException, NotBoundException {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        //Get already running registry
        Registry registry = LocateRegistry.getRegistry();

        System.out.println("To run processes, press enter");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();

        runProcesses(registry);

        System.out.println("Processes started");
    }

    /**
     * Method for running all the processes.
     * @param registry
     * @throws NumberFormatException
     * @throws IOException
     * @throws NotBoundException
     */
    private static void runProcesses(Registry registry) throws NumberFormatException, IOException, NotBoundException {
        numProcesses = registry.list().length;

        IProcess[] processes = new IProcess[numProcesses];
        Thread[] localThreads = new Thread[numProcesses];
        List<Integer> localProcessesIds = new ArrayList<>();

        // Create a list of all threads and separate list of local threads
        for (int i = 0; i < numProcesses; i++) {
            IProcess process = (IProcess) registry.lookup(registry.list()[i]);
            System.out.println("Reading process with id " + process.getId());
            processes[process.getId()] = process;

            if (process.getMachineId() == 1) {
                localProcessesIds.add(process.getId());
            }

            if(process.isTokenPresent()){
                process.setToken(new Token(numProcesses));
            }
        }

        for (int i : localProcessesIds) {
            IProcess process = processes[i];

            ClientProcess cp = new ClientProcess(process);
            localThreads[process.getId()] = new Thread(cp);

            //Fill up process with necessary data
            System.out.println("Filling up process with id " + process.getId());
            process.addOtherProcesses(processes);
        }

        // Run each process in different thread
        for (int i : localProcessesIds) {
            System.out.println("Starting process.");
            localThreads[i].start();
        }
    }

}
