import java.io.Serializable;
import java.util.HashMap;

/**
 * Buffer that is sent with each message.
 * It consists of (process_id, timestamp) pairs.
 */
public class MessageBuffer implements Serializable {

    private static final long serialVersionUID = -7744896354138774799L;

    public HashMap<Integer, VectorClock> messageBuffer;

    public MessageBuffer() {
        this.messageBuffer = new HashMap<>();
    }

    public MessageBuffer(HashMap<Integer, VectorClock> messageBuffer) {
        this.messageBuffer = messageBuffer;
    }

    public HashMap<Integer, VectorClock> getMessageBuffer() {
        return messageBuffer;
    }

    public void setMessageBuffer(HashMap<Integer, VectorClock> messageBuffer) {
        this.messageBuffer = messageBuffer;
    }

    public MessageBuffer clone() {
        HashMap<Integer, VectorClock> temp = new HashMap<>();
        for (int i : messageBuffer.keySet()) {
            temp.put(i, messageBuffer.get(i));
        }
        return new MessageBuffer(temp);
    }

    public void put(int processId, VectorClock vt) {
        messageBuffer.put(processId, vt);
    }

    public void merge(int i , VectorClock vt){
        if(messageBuffer.containsKey(i)){
            messageBuffer.put(i, new VectorClock(i,messageBuffer.get(i).merge(vt)));
        }
        else{
            messageBuffer.put(i, vt);
        }
    }

    public boolean contains(int processId) {
        return this.messageBuffer.containsKey(processId);
    }

    public VectorClock get(int processId) {
        return this.messageBuffer.get(processId);
    }
}
