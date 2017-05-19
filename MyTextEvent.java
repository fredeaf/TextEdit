import java.io.Serializable;
import java.util.Vector;

/**
 * 
 * @author Jesper Buus Nielsen
 *
 */
public class MyTextEvent implements Serializable{
	MyTextEvent(int offset, int[] v, int l) {
		this.offset = offset;
		lamport = l;
		clock1=v[0];
		clock2=v[1];
	}
	private int offset, clock1, clock2,lamport;
	int getOffset() { return offset; }
	int[] getClock(){return new int[]{clock1, clock2};}
	int getLamport(){return lamport;}
}
