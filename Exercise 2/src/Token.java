import java.io.Serializable;
import java.util.Arrays;

public class Token implements Serializable{

    private static final long serialVersionUID = -8137720137856271674L;

    private int[] TN;

    public Token(){

    }

    public Token(int n){
        this.TN = new int[n];
        for (int i=0; i<n; i++){
            this.TN[i] = 0;
        }
    }

    public void setTN(int i, int c){
        this.TN[i] = c;
    }

    public int getTN(int i){
        return this.TN[i];
    }

    public int[] getTN() {
        return TN;
    }

    public void setTN(int[] TN) {
        this.TN = TN;
    }

    @Override
    public String toString() {
        return Arrays.toString(TN);
    }
}