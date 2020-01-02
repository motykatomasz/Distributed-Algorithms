import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import Process.IProcessImplementation;

public class Server {

    public static void main(String[] args){
        try{
            Registry registry = LocateRegistry.createRegistry(1099);

            BufferedReader br = new BufferedReader(new FileReader("inputFiles/clients25"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] split_line = line.split(" ");
                registry.bind("//localhost:1099"+"/" + split_line[0], new IProcessImplementation(Integer.parseInt(split_line[1]),
                        split_line[0], Integer.parseInt(split_line[2])));
            }
            br.close();

            System.out.println("Registry running");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
