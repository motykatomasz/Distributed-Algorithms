import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

import Process.*;
import Utils.*;

public class Client {

    private static int numProcesses;
    private static Map<Integer, List<Edge>> edgesFromProcess;

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
     *
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

        readEdges();

        // Create a list of all threads and separate list of local threads
        for (int i = 0; i < numProcesses; i++) {
            IProcess process = (IProcess) registry.lookup(registry.list()[i]);
            System.out.println("Reading process with id " + process.getId());
            processes[process.getId()] = process;

            if (process.getMachineId() == 1) {
                localProcessesIds.add(process.getId());
            }
        }

        for (int i : localProcessesIds) {
            IProcess process = processes[i];

            ClientProcess cp = new ClientProcess(process);
            localThreads[process.getId()] = new Thread(cp);

            //Fill up process with necessary data
            System.out.println("Filling up process with id " + process.getId());
            process.addOtherProcesses(processes);
            process.addAdjacentEdges(new PriorityQueue<>(edgesFromProcess.get(process.getId())));
        }

        // Run each process in different thread
        for (int i : localProcessesIds) {
            System.out.println("Starting process.");
            localThreads[i].start();
        }
    }

    /**
     * Method for reading edges adjacent to each node.
     *
     * @return Map that contains list of outgoing edges for each process id.
     */
    private static void readEdges() {
        Map<Integer, List<Edge>> edges = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("inputFiles/edges3"));
            String line;
            int msgId = 0;
            while ((line = br.readLine()) != null) {
                String[] split_line = line.split(" ");
                int from = Integer.parseInt(split_line[0]);
                int to = Integer.parseInt(split_line[1]);
                double weight = Double.parseDouble(split_line[2]);
                int deliveryTime = Integer.parseInt(split_line[3]);

                if (!edges.containsKey(from)) {
                    List<Edge> t = new ArrayList<>();
                    t.add(new Edge(EdgeState.CANDIDATE, from, to, weight, deliveryTime));
                    edges.put(from, t);
                } else {
                    edges.get(from).add(new Edge(EdgeState.CANDIDATE, from, to, weight, deliveryTime));
                }

                if (!edges.containsKey(to)) {
                    List<Edge> t = new ArrayList<>();
                    t.add(new Edge(EdgeState.CANDIDATE, to, from, weight, deliveryTime));
                    edges.put(to, t);
                } else {
                    edges.get(to).add(new Edge(EdgeState.CANDIDATE, to, from, weight, deliveryTime));
                }

            }
        } catch (IOException e) {
            System.out.println("Problem with reading file with messages.");
        }
        edgesFromProcess = edges;
    }

}
