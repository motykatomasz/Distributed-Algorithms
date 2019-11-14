import java.util.HashMap;

/**
 * Buffer that is sent with each message.
 * It consists of (process_id, timestamp) pairs.
 */
public class MessageBuffer {

    public HashMap<Integer,VectorClock> messageBuffer;
}
