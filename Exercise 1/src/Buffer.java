import java.util.Comparator;
import java.util.PriorityQueue;


/**
 * Buffer class used to store unordered messages.
 */
public class Buffer {

    public static class MessageComparator implements Comparator<Message> {
        public int compare(Message m1, Message m2) {
            if(m1.getVectorClock().smallerOrEqual(m2.getVectorClock())){
                return -1;
            }
            else{
                return 1;
            }
        };
    }


    public PriorityQueue<Message> buffer;

    public Buffer(){
        this.buffer = new PriorityQueue<>(1000, new MessageComparator());
    }

    public Message peek(){
        return this.buffer.peek();
    }

    public Message poll(){
        return this.buffer.poll();
    }

    public void add(Message msg){
        this.buffer.add(msg);
    }
}
