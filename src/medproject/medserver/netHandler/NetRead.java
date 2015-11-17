package medproject.medserver.netHandler;

import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import medproject.medserver.requestHandler.RequestHandler;

public class NetRead {

	RequestHandler handlerThread;
	private int bytesForMessageSize = 8;
	
	public NetRead(RequestHandler handlerThread) {
		this.handlerThread = handlerThread;
	}

	public void read(ClientSession client) {
		
		SocketChannel channel = client.getChannel();
	    ByteBuffer buffer = client.getBuffer();
		
		try {
			ReadableByteChannel ch = (ReadableByteChannel)channel;
		    
			int bytesOp = 0, bytesTotal = 0;
		    
			if(client.isFinishedReading())
				buffer.clear();
		    
		    while (buffer.hasRemaining() && (bytesOp = ch.read((ByteBuffer) buffer)) > 0) bytesTotal += bytesOp;
		    /*
		    if (bytesOp == -1) {
		      if(client.getAccount().getAccountID() != -1)
		    		handlerThread.makeServerRequest(client.getAccount().getAccountID(), new Request(RequestCodes.LOG_OFF_REQUEST, 
		    				"", RequestCodes.REQUEST_PENDING));
		      client.disconnect();
		      return;
		    }
*/
		    if(client.getCurrentMessageByteSize() == 0){
		    	int packetSize = 0;
			    for(int i=0; i<bytesForMessageSize; i++){
			    	packetSize = packetSize * 10 + buffer.get(i);
				}
			    client.setCurrentMessageByteSize(packetSize);
		    }
		    
		    if(client.getCurrentMessageByteSize() == buffer.position() - bytesForMessageSize)
		    	client.setFinishedReading(true);
		    else
		    	client.setFinishedReading(false);
		    
		   
            System.out.println("Received " + buffer.position() + " bytes");
            
            if(client.isFinishedReading()){
                handlerThread.processRequest(client);
                client.setCurrentMessageByteSize(0);
            }
            else{
            	client.getSelectionKey().interestOps(SelectionKey.OP_READ);
            }
        } catch (Throwable t) {
        	//if(client.getAccount().getAccountID() != -1)
	    		//handlerThread.makeServerRequest(client.getAccount().getAccountID(), new Request(RequestCodes.LOG_OFF_REQUEST, 
	    			//	"", RequestCodes.REQUEST_PENDING));
	    	client.disconnect();
    		
    		t.printStackTrace();
		}

	}

}
