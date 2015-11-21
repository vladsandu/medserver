package medproject.medserver.dataWriter;	

import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import medproject.medlibrary.concurrency.Request;
import medproject.medserver.netHandler.ChangeRequest;
import medproject.medserver.netHandler.ClientSession;

public class DataWriter{
//FIXME: ArrayList vs something concurrent
	private ConcurrentHashMap<ClientSession,List<Request>> writingQueue = new ConcurrentHashMap<>();
	private List<ChangeRequest> pendingChanges;

	public DataWriter( List<ChangeRequest> pendingStateChanges) {
		this.pendingChanges = pendingStateChanges; 
	}

	public void processWriteRequest(ClientSession session, Request request) {
		synchronized(this.writingQueue) {
			if(writingQueue.get(session) != null)
				writingQueue.get(session).add(request);
			else{
				List<Request> requestList = Collections.synchronizedList(new ArrayList<Request>());
				requestList.add(request);

				writingQueue.put(session, requestList);
			}
		}

		pendingChanges.add(new ChangeRequest(session.getChannel(), SelectionKey.OP_WRITE));
	}

	public ConcurrentHashMap<ClientSession, List<Request>> getWritingQueue() {
		return writingQueue;
	}

}
