import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class Client {

    public static void main(String[] args) throws IOException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry();

        System.out.println("To run processes, press enter");
        Scanner scan = new Scanner(System.in);
        scan.nextLine();
        runProcesses(registry);
        System.out.println("Processes started");
    }

    private static void runProcesses(Registry registry) throws NumberFormatException, IOException, NotBoundException {
        int numProcesses = registry.list().length;

        List<IProcess> processes = new ArrayList<>();
        Thread[] threads = new Thread[numProcesses];

        Map<Integer, List<Message>> messages = readMessages();

        for (int i = 0; i < numProcesses; i++) {
            IProcess process = (IProcess) registry.lookup(registry.list()[i]);
            process.addMessagesToSend(messages.get(i));
            processes.add(process);
            ClientProcess p = new ClientProcess(process);
            threads[i] = new Thread(p);
        }

        for (int i = 0; i < numProcesses; i++) {
            processes.get(i).addOtherProcesses(processes);
            threads[i].start();
        }
    }

    private static Map<Integer, List<Message>> readMessages(){
        Map<Integer, List<Message>> messages = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader("../inputFiles/messages"));
            String line;
            int msgId = 0;
            while ((line = br.readLine()) != null) {
                String[] split_line = line.split(" ");
                int sender = Integer.parseInt(split_line[0]);
                int receiver = Integer.parseInt(split_line[1]);
                String content = split_line[2];
                Message m = new Message(msgId, content, new VectorClock(), sender, receiver);

                if (!messages.containsKey(sender)) {
                    List<Message> messagesFromSender = new ArrayList<>();
                    messagesFromSender.add(m);
                    messages.put(sender, messagesFromSender);
                } else {
                    messages.get(sender).add(m);
                }
                msgId++;
            }
        } catch (IOException e) {
            System.out.println("Problem with reading file with messages.");
        }
        return messages;
    }
}
