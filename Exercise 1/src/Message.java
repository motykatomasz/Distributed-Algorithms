import java.io.Serializable;

public class Message implements Serializable {

    private static final long serialVersionUID = -5621003653757775672L;

    /**
     * Id of the message
     */
    int id;

    /**
     * Content of the message
     */
    String content;

    /**
     * Local vector clock of client
     */
    VectorClock vectorClock;

    /**
     * Id of receiving process
     */
    int receiver;

    /**
     * Id of sending process
     */
    int sender;

    /**
     * Buffer of (id, timestamp) pairs
     */
    MessageBuffer messageBuffer;

    /**
     * Time needed to deliver the message to the receiver after sending it.
     */
    int deliveryTime;

    public Message(int id, String content, VectorClock vectorClock, MessageBuffer messageBuffer, int sender, int receiver, int deliveryTime) {
        this.id = id;
        this.content = content;
        this.vectorClock = vectorClock;
        this.messageBuffer = messageBuffer;
        this.receiver = receiver;
        this.sender = sender;
        this.deliveryTime = deliveryTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public VectorClock getVectorClock() {
        return vectorClock;
    }

    public void setVectorClock(VectorClock vectorClock) {
        this.vectorClock = vectorClock;
    }

    public int getReceiver() {
        return receiver;
    }

    public void setReceiver(int receiver) {
        this.receiver = receiver;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public int getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(int deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public MessageBuffer getMessageBuffer() {
        return messageBuffer;
    }

    public void setMessageBuffer(MessageBuffer messageBuffer) {
        this.messageBuffer = messageBuffer;
    }

    @Override
    public String toString() {
        return this.content;
    }
}
