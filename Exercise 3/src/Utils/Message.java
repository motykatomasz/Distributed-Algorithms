package Utils;

import java.io.Serializable;
import Process.ProcessState;

public class Message implements Serializable {

    private static final long serialVersionUID = 5787101278423113957L;

    private MessageType type;
    private int senderId;
    private int receiverID;
    private int level;

    private double fragmentName;
    private double bestWeight;
    private ProcessState senderState;

    private boolean IsAbsorbMessage;


    // constructor for ACCEPT, REJECT and CHANGE_ROOT messages
    public Message(MessageType type) {
        this.type = type;
    }

    // constructor for CONNECT messages
    public Message(MessageType type, int level) {
        this.type = type;
        this.level = level;
    }

    // constructor for REPORT messages
    public Message(MessageType type, double weight) {
        this.type = type;
        this.bestWeight = weight;
    }

    // constructor for TEST messages
    public Message(MessageType type, int level, double name) {
        this.type = type;
        this.level = level;
        fragmentName = name;
    }

    //constructor for INITIATE messages
    public Message(MessageType type, int level, double name, ProcessState s) {
        this.type = type;
        this.level = level;
        fragmentName = name;
        senderState = s;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(int receiverID) {
        this.receiverID = receiverID;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getFragmentName() {
        return fragmentName;
    }

    public void setFragmentName(double fragmentName) {
        this.fragmentName = fragmentName;
    }

    public double getWeight() {
        return bestWeight;
    }

    public void setWeight(double weight) {
        this.bestWeight = weight;
    }

    public ProcessState getSenderState() {
        return senderState;
    }

    public void setSenderState(ProcessState senderState) {
        this.senderState = senderState;
    }

    public boolean getIsAbsorbMessage() {
        return IsAbsorbMessage;
    }

    public void setIsAbsorbMessage(boolean value) {
        this.IsAbsorbMessage = value;
    }

}
