package medproject.medserver.dataWriter;	

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import medproject.medserver.netHandler.ChangeRequest;

public class DataWriter{

private ConcurrentHashMap<SocketChannel,ArrayList<Object>> writingQueue = new ConcurrentHashMap<>();
private List<ChangeRequest> pendingChanges;

	public DataWriter( List<ChangeRequest> pendingStateChanges) {
		this.pendingChanges = pendingStateChanges; 
	}
	
	public void processWriteRequest(SocketChannel channel, Object object) {
		synchronized(this.writingQueue) {
			if(writingQueue.containsKey(channel) && writingQueue.get(channel) != null)
				writingQueue.get(channel).add(object);
			else{
			ArrayList<Object> requestList = new ArrayList<Object>();
			requestList.add(object);
			
			writingQueue.put(channel, requestList);
			}
		}
		
		synchronized(pendingChanges){
			pendingChanges.add(new ChangeRequest(channel, SelectionKey.OP_WRITE));
		}
	  }

	public ConcurrentHashMap<SocketChannel, ArrayList<Object>> getWritingQueue() {
		return writingQueue;
	}

}
