import java.io.Serializable;
import java.util.HashMap;

/**
 * Buffer that is sent with each message.
 * It consists of (process_id, timestamp) pairs.
 */
public class MessageBuffer implements Serializable {

    private static final long serialVersionUID = -7744896354138774799L;

    public HashMap<Integer,VectorClock> messageBuffer;

    public MessageBuffer() {
        this.messageBuffer = new HashMap<>();
    }

    public HashMap<Integer, VectorClock> getMessageBuffer() {
        return messageBuffer;
    }

    public void setMessageBuffer(HashMap<Integer, VectorClock> messageBuffer) {
        this.messageBuffer = messageBuffer;
    }
}
