package medproject.medserver.requestHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

import medproject.medlibrary.concurrency.Request;
import medproject.medserver.dataWriter.DataWriter;
import medproject.medserver.databaseHandler.DatabaseThread;
import medproject.medserver.netHandler.ClientSession;

public class RequestHandler implements Runnable{

	private final ConcurrentHashMap<ClientSession, Request> requestQueue = new ConcurrentHashMap<ClientSession, Request>();
	
	private final DataWriter dataWriter;
	
	private final Thread t;
	private volatile boolean shouldStop = false;
	
	private int bytesForMessageSize = 8;

	public RequestHandler(DataWriter dataWriter, DatabaseThread databaseThread) {
		this.dataWriter = dataWriter;
		
		this.t = new Thread(this);
	}
	
	public void start() {
	    t.start();
	}

	public void stop() {   
	    shouldStop = true;
	}

	public void processRequest(ClientSession client) throws IOException, ClassNotFoundException {
		
		Request currentRequest = new Request(RequestCodes.EMPTY_REQUEST, "", RequestCodes.REQUEST_SUCCESSFUL);
	   
		ByteBuffer clientBuffer = client.getBuffer();
	    
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
			   if(currentRequest.getREQUEST_CODE() != RequestCodes.EMPTY_REQUEST){
				   requestQueue.put(client, currentRequest);
			   }
	    }
	  }
	
	@Override
	public void run() {
		while(!shouldStop) {
	      
		      for(ConcurrentHashMap.Entry<ClientSession, Request> request : requestQueue.entrySet()){
			  	 	if(sendRequestToSpecializedHandler(request.getKey(), request.getValue()) == true){
			  	 		requestQueue.remove(request.getKey(), request.getValue());
			  	 	}
			  }
		      		     
		     try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean sendRequestToSpecializedHandler(ClientSession client, Request request){
		
		Request currentRequest = new Request(RequestCodes.EMPTY_REQUEST, "", RequestCodes.REQUEST_PENDING);
		
		//switch(RequestCodes.requestTypeGetter(request)){
		//}	
		
		if(currentRequest.getREQUEST_STATUS() == RequestCodes.REQUEST_TERMINATED){
			return true;
		}
		else if(currentRequest.getREQUEST_STATUS() != RequestCodes.REQUEST_PENDING){
			makeRequestComplete(client, currentRequest);
			return true;
		}
		
		return false;
			
	}
	
	public void makeRequestComplete(ClientSession client, Request currentRequest){
	}

}

