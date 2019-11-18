import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class Client {

    private static int numProcesses;
    private static Map<Integer, List<Message>> messagesBySender;
    private static Map<Integer, List<Integer>> delaysBySender;

    public static void main(String[] args) throws IOException, NotBoundException {
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
        Thread[] threads = new Thread[numProcesses];

        // Read messages that each process will send. Map contains list of messages for each process id.
        readMessages();

        // Create a list of all local threads
        for (int i = 0; i < numProcesses; i++) {
            IProcess process = (IProcess) registry.lookup(registry.list()[i]);
            System.out.println("Reading process with id " + process.getId());
            processes[process.getId()] = process;

            //Add number of messages to local thread
            int numMessages = 0;
            if (messagesBySender.containsKey(process.getId()))
                numMessages = messagesBySender.get(process.getId()).size();
            else
                messagesBySender.put(process.getId(), new ArrayList<>());

            //Add list of delays to local threads
            List<Integer> delays = new ArrayList<>();
            if (delaysBySender.containsKey(process.getId()))
                delays = delaysBySender.get(process.getId());
            else
                delaysBySender.put(process.getId(), delays);

            ClientProcess cp = new ClientProcess(process, numMessages, delays);
            threads[process.getId()] = new Thread(cp);
        }

        // Fill up processes with necessary data
        for (int i = 0; i < numProcesses; i++) {
            IProcess p = processes[i];
            System.out.println("Filling up process with id " + p.getId());
            p.addOtherProcesses(processes);
            p.setVectorClock(p.getId(), numProcesses);
            p.addMessagesToSend(messagesBySender.get(p.getId()));
        }

        // Run each process in different thread
        for (int i = 0; i < numProcesses; i++) {
            System.out.println("Starting process.");
            threads[i].start();
        }
    }

    /**
     * Method for reading messages that will be send in the system.
     * @return Map that contains list of messages for each process id.
     */
    private static void readMessages(){
        Map<Integer, List<Message>> messages = new HashMap<>();
        Map<Integer, List<Integer>> delays = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("../inputFiles/messages"));
            String line;
            int msgId = 0;
            while ((line = br.readLine()) != null) {
                String[] split_line = line.split(" ");
                int sender = Integer.parseInt(split_line[0]);
                int receiver = Integer.parseInt(split_line[1]);
                String content = split_line[2];
                int deliveryTime = Integer.parseInt(split_line[3]);
                int sendingTime = Integer.parseInt(split_line[4]);
                Message m = new Message(msgId, content, new VectorClock(sender, numProcesses), new MessageBuffer(), sender, receiver, deliveryTime);
                System.out.println("Creating message from " + sender + " to " + receiver);

                if (!messages.containsKey(sender)) {
                    List<Message> messagesFromSender = new ArrayList<>();
                    messagesFromSender.add(m);
                    messages.put(sender, messagesFromSender);
                } else {
                    messages.get(sender).add(m);
                }
                msgId++;

                if (!delays.containsKey(sender)) {
                    List<Integer> senderSendingTimes = new ArrayList<>();
                    senderSendingTimes.add(sendingTime);
                    delays.put(sender, senderSendingTimes);
                } else {
                    delays.get(sender).add(sendingTime);
                }

            }
        } catch (IOException e) {
            System.out.println("Problem with reading file with messages.");
        }
        messagesBySender =  messages;
        delaysBySender =  delays;
    }
}
