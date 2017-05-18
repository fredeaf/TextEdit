import java.util.Vector;

/**
 * 
 * @author Jesper Buus Nielsen
 *
 */
public class TextInsertEvent extends MyTextEvent {

	private String text;
	
	public TextInsertEvent(int offset, String text, int[] v) {
		super(offset,v);
		this.text = text;
	}
	public String getText() { return text; }

}

