package Utils;

public class QueuedMessage {
    QueuedMessageType type;
    //Message msg;

    public QueuedMessage(QueuedMessageType t){
        type = t;
        //msg = m;
    }

    public QueuedMessageType getType() {
        return type;
    }

    public void setType(QueuedMessageType t) {
        this.type = t;
    }
}
