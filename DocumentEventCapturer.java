import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PushbackInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * 
 * This class captures and remembers the text events of the given document on
 * which it is put as a filter. Normally a filter is used to put restrictions
 * on what can be written in a buffer. In out case we just use it to see all
 * the events and make a copy. 
 * 
 * @author Jesper Buus Nielsen
 *
 */
public class DocumentEventCapturer extends DocumentFilter {

	/*
     * We are using a blocking queue for two reasons: 
     * 1) They are thread safe, i.e., we can have two threads add and take elements 
     *    at the same time without any race conditions, so we do not have to do  
     *    explicit synchronization.
     * 2) It gives us a member take() which is blocking, i.e., if the queue is
     *    empty, then take() will wait until new elements arrive, which is what
     *    we want, as we then don't need to keep asking until there are new elements.
     */

	protected ServerSocket serverSocket;
	protected LinkedBlockingQueue<MyTextEvent> eventHistory = new LinkedBlockingQueue<MyTextEvent>();
	protected Socket socket;
	protected ObjectInputStream objectInputStream=null;
	protected ObjectOutputStream objectOutputStream=null;
	protected PushbackInputStream pushbackInputStream;
	protected DistributedTextEditor distributedTextEditor;
	/**
	 * If the queue is empty, then the call will block until an element arrives.
	 * If the thread gets interrupted while waiting, we throw InterruptedException.
	 *
	 * @return Head of the recorded event queue.
	 */
	MyTextEvent take() throws InterruptedException {
		return eventHistory.take();
	}


	public void insertString(FilterBypass fb, int offset,
							 String str, AttributeSet a)
			throws BadLocationException {

		/* Queue a copy of the event and then modify the textarea */
		if (socket!=null){
			try {
				objectOutputStream.writeObject(new TextInsertEvent(offset,str));
			}catch (Exception e){}
		}
		super.insertString(fb, offset, str, a);
	}
    
	public void remove(FilterBypass fb, int offset, int length)
			throws BadLocationException {
		/* Queue a copy of the event and then modify the textarea */
		if (socket!=null){
			try {
				objectOutputStream.writeObject(new TextRemoveEvent(offset,length));
			}catch (Exception e){}
		}
		super.remove(fb, offset, length);
	}
    
	public void replace(FilterBypass fb, int offset,
						int length,
						String str, AttributeSet a)
			throws BadLocationException {

		/* Queue a copy of the event and then modify the text */
		if (length > 0) {
			if (socket!=null){
				try {
					objectOutputStream.writeObject(new TextRemoveEvent(offset,length));
				}catch (Exception e){}
			}
		}
		if (socket!=null){
			try {
				objectOutputStream.writeObject(new TextInsertEvent(offset,str));
			}catch (Exception e){}
		}
		super.replace(fb, offset, length, str, a);
	}
	protected void registerOnPort(int portNumber) {
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			serverSocket = null;
			System.out.println("Register failed");
			System.exit(-1);
		}
	}
	protected void waitForConnectionFromClient() {
		Socket res = null;
		try {
			System.out.println("about to listen");

			res = serverSocket.accept();
			System.out.println("client accepted");
		} catch (Exception e) {
			System.out.println("waiting for connection failed");}
		socket = res;
		distributedTextEditor.createChecker();
	}
	protected void connectToServer(String serverIp,int portNumber) {
		Socket res = null;
		try {
			System.out.println("about to  connect "+serverIp+" : "+portNumber);

			res = new Socket(serverIp,portNumber);
			System.out.println("Trying to connect "+serverIp+" : "+portNumber);
		} catch (IOException e) {
			System.out.println("connect failed");		}
		socket = res;
		distributedTextEditor.createChecker();
	}
	protected void setReference(DistributedTextEditor distributedTextEditor){
		this.distributedTextEditor = distributedTextEditor;
	}
	protected void startStream(){
		try {
			objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
			pushbackInputStream = new PushbackInputStream(socket.getInputStream());
			objectInputStream = new ObjectInputStream(pushbackInputStream);
		}catch (Exception E){}
	}
	protected void checkForInput(){
		if(socket!=null){
			try{
				for(int i; (i = pushbackInputStream.read())!=-1;){
					pushbackInputStream.unread(i);
					MyTextEvent textEvent = (MyTextEvent)objectInputStream.readObject();
					if(textEvent!=null){eventHistory.add(textEvent);}}
			}catch(Exception e){}
		}
	}
	protected boolean isConnected() {
		try{
			//	if(socket!=null&&socket.getInputStream().read()==-1){distributedTextEditor.disconect();
			//			return false;}
		}catch (Exception e){}
		return objectInputStream == null && socket != null;
	}
	protected void disconnect(){
		try {
			objectInputStream.close();
			objectOutputStream.close();
			pushbackInputStream.close();
			socket.close();
		}catch (Exception e){}
		socket=null;
		objectInputStream=null;
		objectOutputStream=null;
		pushbackInputStream=null;
	}
}
