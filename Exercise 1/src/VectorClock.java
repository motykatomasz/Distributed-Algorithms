import java.io.Serializable;

/**
 * Class representing vector clock.
 */
public class VectorClock implements Serializable {

    private static final long serialVersionUID = 6059047457532265703L;

    int id;

    int[] timeVector;

    public VectorClock() {}

    public VectorClock(int id, int[] timeVector) {
        this.id = id;
        this.timeVector = timeVector;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int[] getTimeVector() {
        return timeVector;
    }

    public void setTimeVector(int[] timeVector) {
        this.timeVector = timeVector;
    }

    public int[] merge(VectorClock vc2) {
        if (vc2 == null) {
            return this.timeVector;
        }
        for (int i = 0; i < this.timeVector.length; i++) {
            int localTime = this.timeVector[i];
            int receivedTime = vc2.getTimeVector()[i];

            if (localTime < receivedTime) this.timeVector[i] = receivedTime;
        }
        return this.timeVector;
    }

    public boolean smallerOrEqualThan(VectorClock vc2) {
        if (vc2 == null) {
            return true;
        }

        boolean smallerOrEqual = true;
        for (int i = 0; i < this.timeVector.length; i++) {
            int localTime = this.timeVector[i];
            int receivedTime = vc2.getTimeVector()[i];
            if (localTime > receivedTime) {
                smallerOrEqual = false;
            }
            if (!smallerOrEqual) {
                break;
            }
        }
        return smallerOrEqual;
    }

}
