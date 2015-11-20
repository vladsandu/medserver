package medproject.medserver.requestHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import medproject.medlibrary.concurrency.Request;
import medproject.medlibrary.concurrency.RequestStatus;
import medproject.medserver.dataWriter.DataWriter;
import medproject.medserver.databaseHandler.DatabaseRequest;
import medproject.medserver.databaseHandler.DatabaseThread;
import medproject.medserver.logging.LogWriter;
import medproject.medserver.netHandler.ClientSession;

public class RequestHandler implements Runnable{

	private final Logger LOG = LogWriter.getLogger("RequestHandler");

	private final LinkedBlockingQueue<RequestEntry> requestQueue = new LinkedBlockingQueue<RequestEntry>();
	private final DatabaseThread databaseThread;

	private final DataWriter dataWriter;

	private final Thread t;
	private volatile boolean shouldStop = false;

	private int bytesForMessageSize = 8;

	public RequestHandler(DataWriter dataWriter) throws SQLException {
		this.dataWriter = dataWriter;
		this.databaseThread = new DatabaseThread(this, "jdbc:oracle:thin:@localhost:1521/pdbmed", "medadmin", "test");

		this.t = new Thread(this);
	}

	public void start() {
		databaseThread.start();
		t.start();
	}

	public void stop() {   
		shouldStop = true;
		databaseThread.stop();
	}

	public void processNewRequest(ClientSession client) throws IOException, ClassNotFoundException {

		Request currentRequest = null;

		ByteBuffer clientBuffer = client.getReadBuffer();

		clientBuffer.clear();
		byte[] packetBytes = new byte[client.getCurrentMessageByteSize()];
		clientBuffer.position(bytesForMessageSize);

		clientBuffer.get(packetBytes,0,packetBytes.length);		

		ByteArrayInputStream inputStream = new ByteArrayInputStream(packetBytes);
		ObjectInputStream objectStream = new ObjectInputStream(inputStream);

		currentRequest = (Request) objectStream.readObject();

		objectStream.close();

		clientBuffer.clear();

		synchronized(this.requestQueue) {
			if(currentRequest != null){
				requestQueue.offer(new RequestEntry(currentRequest, client));
			}
		}
	}

	@Override
	public void run() {
		//TODO: implement shouldStop variables in all projects
		while(!shouldStop) {
			RequestEntry requestEntry = null;
			try {
				requestEntry = requestQueue.take();

				if(requestEntry.getRequest().isCompleted())
					dataWriter.processWriteRequest(
							requestEntry.getClientSession(), 
							requestEntry.getRequest());
				else
					sendRequestToSpecializedHandler(requestEntry.getClientSession(), requestEntry.getRequest());
				
			} catch (InterruptedException e) {
				LOG.severe("Request Handler thread interrupted");
			}			  	 
		}
	}

	private void sendRequestToSpecializedHandler(ClientSession client, Request request){
//send database request
		//Request currentRequest = new Request(RequestCodes.EMPTY_REQUEST, "", RequestCodes.REQUEST_PENDING);

		//request.setStatus(RequestStatus.REQUEST_COMPLETED);
	
		//switch(RequestCodes.requestTypeGetter(request)){
		//}	


	}

	public void addCompleteRequest(DatabaseRequest currentRequest){
		synchronized(this.requestQueue) {
			requestQueue.offer(new RequestEntry(currentRequest.getRequest(), currentRequest.getClientSession()));
		}
	}

}

