import javax.swing.JTextArea;
import java.awt.EventQueue;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 * 
 * Takes the event recorded by the DocumentEventCapturer and replays
 * them in a JTextArea. The delay of 1 sec is only to make the individual
 * steps in the reply visible to humans.
 * 
 * @author Jesper Buus Nielsen
 *
 */
public class EventReplayer implements Runnable {
	
	private DocumentEventCapturer dec;
	private JTextArea area,area2;
	private DistributedTextEditor editor;
	private ArrayList<MyTextEvent> que = new ArrayList<>();
					MyTextEvent mte;


	public EventReplayer(DocumentEventCapturer dec, JTextArea area,JTextArea area2,DistributedTextEditor editor) {
		this.dec = dec;
		this.area = area;
		this.area2 = area2;
		this.editor = editor;

	}
	
	public void run() {
		boolean wasInterrupted = false;
		while (!wasInterrupted) {
			try {
				if(!que.isEmpty()){
					mte = que.get(0);
					que.remove(0);}
				else {
					mte = dec.take();  }
				if (mte instanceof TextInsertEvent || ) {
					final TextInsertEvent tie = (TextInsertEvent)mte;
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							//if(!editor.isServer()){try {Thread.sleep(5000);} catch (InterruptedException e) {e.printStackTrace();}}
							try {
								if(!checkClocks(tie.getClock(),tie.getLamport())){
									que.add(tie);
									return;
								}
								dec.setIsReplay();
								area.insert(tie.getText(), tie.getOffset());
								dec.increaseOtherClock(tie.getClock());
								dec.increaseMyClock();
								area2.append(tie.getClock()[0]+":"+tie.getClock()[1]+" \n");
							} catch (Exception e) {
								System.err.println(e);
								/* We catch all axceptions, as an uncaught exception would make the
				     * EDT unwind, which is now healthy.
				     */
							}
						}
					});
				} else if (mte instanceof TextRemoveEvent) {
					final TextRemoveEvent tre = (TextRemoveEvent)mte;
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							try {
								if(checkClocks(tre.getClock(),tre.getLamport())){
										que.add(tre);
								}   	return;
								dec.setIsReplay();
								area.replaceRange(null, tre.getOffset(), tre.getOffset()+tre.getLength());
								dec.increaseOtherClock(tre.getClock());
								dec.increaseMyClock();
								area2.append(tre.getClock()[0]+":"+tre.getClock()[1]+ "\n");

							} catch (Exception e) {
								System.err.println(e);
								/* We catch all axceptions, as an uncaught exception would make the
				     * EDT unwind, which is now healthy.
				     */
							}
						}
					});
				}
				Thread.sleep(0);
			} catch (Exception _) {
				wasInterrupted = true;
			}


		}
		System.out.println("I'm the thread running the EventReplayer, now I die!");
	}
	private boolean checkClocks(int[] recievedClock, int recivedLamport){
		if(editor.isServer()) {
			if(recievedClock[0]!=dec.getClock()[0]){
				//TODO
				area2.append("Concurrency hit \n");
				return false;

			}
			if(recivedLamport !=dec.getLamport()){
				//TODO
				area2.append("bad Lamport\n");
				return false;

			}
		}
		else {
			if(recievedClock[1]!=dec.getClock()[1]){
				//TODO
				area2.append("Concurrency hit \n");
				return false;
			}
			if(recivedLamport !=dec.getLamport()){
				//TODO
				area2.append("bad Lamport\n");
				return false;
			}

		}
		return true;
	}
	public void resetQue(){
		que = new ArrayList<>();
	}
}
