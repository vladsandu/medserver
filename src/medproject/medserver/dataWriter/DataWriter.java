package medproject.medserver.dataWriter;	

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import medproject.medlibrary.concurrency.Request;
import medproject.medserver.netHandler.ChangeRequest;

public class DataWriter{
//FIXME: ArrayList vs something concurrent
	private ConcurrentHashMap<SocketChannel,ArrayList<Request>> writingQueue = new ConcurrentHashMap<>();
	private List<ChangeRequest> pendingChanges;

	public DataWriter( List<ChangeRequest> pendingStateChanges) {
		this.pendingChanges = pendingStateChanges; 
	}

	public void processWriteRequest(SocketChannel channel, Request request) {
		synchronized(this.writingQueue) {
			if(writingQueue.containsKey(channel) && writingQueue.get(channel) != null)
				writingQueue.get(channel).add(request);
			else{
				ArrayList<Request> requestList = new ArrayList<Request>();
				requestList.add(request);

				writingQueue.put(channel, requestList);
			}
		}

		synchronized(pendingChanges){
			pendingChanges.add(new ChangeRequest(channel, SelectionKey.OP_WRITE));
		}
	}

	public ConcurrentHashMap<SocketChannel, ArrayList<Request>> getWritingQueue() {
		return writingQueue;
	}

}
