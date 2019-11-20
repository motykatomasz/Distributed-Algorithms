import java.io.Serializable;
import java.util.Arrays;

/**
 * Class representing vector clock.
 */
public class VectorClock implements Serializable {

    private static final long serialVersionUID = 6059047457532265703L;

    /**
     * Id.
     */
    int id;

    /**
     * Vector of timestamps for each process in the system.
     */
    int[] timeVector;

    public VectorClock(int id, int n) {
        this.timeVector = new int[n];
        for (int i = 0; i < n; i++) {
            this.timeVector[i] = 0;
        }
        this.id = id;
    }

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

    public boolean smallerOrEqual(VectorClock other) {
        if (other == null) {
            return true;
        }

        boolean smallerOrEqual = true;
        for (int i = 0; i < this.timeVector.length; i++) {
            if (this.timeVector[i] > other.getTimeVector()[i]) {
                smallerOrEqual = false;
            }
            if (!smallerOrEqual) {
                break;
            }
        }
        return smallerOrEqual;
    }

    public void incerementClock(int processId) {
        this.timeVector[processId] += 1;
    }

    public int[] clone() {
        return Arrays.copyOf(this.timeVector, this.timeVector.length);
    }

    @Override
    public String toString() {
        return "VectorClock{" + Arrays.toString(timeVector) + '}';
    }
}
