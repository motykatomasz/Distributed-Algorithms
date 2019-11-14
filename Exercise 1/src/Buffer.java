import java.util.PriorityQueue;


/**
 * Buffer class used to store unordered messages.
 */
public class Buffer {

    public PriorityQueue<Message> buffer;

    public Buffer(){
        this.buffer = new PriorityQueue<>();
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
