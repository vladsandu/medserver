package medproject.medserver.dataWriter;	

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import medproject.medlibrary.concurrency.Request;
import medproject.medserver.netHandler.ChangeRequest;
import medproject.medserver.netHandler.ClientSession;

public class DataWriter{
//FIXME: ArrayList vs something concurrent
	private ConcurrentHashMap<ClientSession,ArrayList<Request>> writingQueue = new ConcurrentHashMap<>();
	private List<ChangeRequest> pendingChanges;

	public DataWriter( List<ChangeRequest> pendingStateChanges) {
		this.pendingChanges = pendingStateChanges; 
	}

	public void processWriteRequest(ClientSession session, Request request) {
		synchronized(this.writingQueue) {
			if(writingQueue.get(session) != null)
				writingQueue.get(session).add(request);
			else{
				ArrayList<Request> requestList = new ArrayList<Request>();
				requestList.add(request);

				writingQueue.put(session, requestList);
			}
		}

		synchronized(pendingChanges){
			pendingChanges.add(new ChangeRequest(session.getChannel(), SelectionKey.OP_WRITE));
		}
	}

	public ConcurrentHashMap<ClientSession, ArrayList<Request>> getWritingQueue() {
		return writingQueue;
	}

}
