import java.util.Vector;

public class TextRemoveEvent extends MyTextEvent {

	private int length;
	
	public TextRemoveEvent(int offset, int length, int[] v, int l) {
		super(offset,v,l);
		this.length = length;
	}
	
	public int getLength() { return length; }
}
