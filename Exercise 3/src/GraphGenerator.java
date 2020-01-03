import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class GraphGenerator {
	
	private static int nodes, nr;
	private static double maxWeight;
	private static double edgeProbability;
	
	public static void main(String[] args) {
		if (args.length < 4) {
			System.out.println("Generator requires 4 parameters:");
			System.out.println("  number of nodes");
			System.out.println("  edge probability");
			System.out.println("  max. weight of the edges");
			System.out.println("  id (for multiple instances with the same settings)");
			System.exit(0);
		}
		
		for (int i = 0; i < args.length; i++) {
			try {
				nodes = Integer.parseInt(args[0]);
				edgeProbability = Double.parseDouble(args[1]);
				maxWeight = Double.parseDouble(args[2]);
				nr = Integer.parseInt(args[3]);
			} catch(Exception e) {
				System.out.println("Couldn't parse all arguments: ");
				System.out.println("  " + e);
				System.exit(0);
			}
		}
		
		if (edgeProbability < 0 || edgeProbability > 1) {
			System.out.println("Please pick an edge probability between 0 and 1.");
			System.exit(0);
		}
		
		if (nodes < 0 || maxWeight < 0 || nr < 0) {
			System.out.println("Please use positive numbers as arguments.");
			System.exit(0);
		}
		
		
		//construct the filename
		String edge = ""+edgeProbability;
		edge = edge.replaceFirst("\\.", "");
		if (edge.length() > 5)
			edge = edge.substring(0, 6);
		String id = String.format("%02d", nr);
		String filename = "maxcut_"+nodes+"_"+edge+"_"+maxWeight+"_instance_"+id;
		
		
		//write the file
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(filename)));
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		double weight;
		double random;

		for (int n1 = 1; n1 < nodes; n1++) {
			for (int n2 = 0; n2 < n1; n2++) {
				if (n1 == n2)
					continue;
				random = Math.random();
				if (random < edgeProbability) {
					weight = (Math.random() * maxWeight + 1);
					out.println(n1 + " " + n2 + " " + weight);
				}
			}
		}
		out.flush();
		out.close();
	}
}
