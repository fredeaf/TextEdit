import java.io.Serializable;
import java.util.Vector;

/**
 * 
 * @author Jesper Buus Nielsen
 *
 */
public class MyTextEvent implements Serializable{
	MyTextEvent(int offset, int[] v) {
		this.offset = offset;
		this.clock=v;
	}
	private int offset;
	private int[] clock;
	int getOffset() { return offset; }
	int[] getClock(){return clock;}
}
