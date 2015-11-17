package medproject.medserver.netHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import medproject.medserver.dataWriter.DataWriter;

public class NetSend {

	private DataWriter dataWriter;
    private int bytesForMessageSize = 8;
	
	public NetSend(DataWriter dataWriter) {
		this.dataWriter = dataWriter;
	}
	
	void send(ClientSession session) throws IOException{
		
		synchronized(dataWriter.getWritingQueue()){
			ConcurrentHashMap<SocketChannel, ArrayList<Object>> pendingData = this.dataWriter.getWritingQueue();
		
		if(!pendingData.containsKey(session.channel)){
			session.getSelectionKey().interestOps(SelectionKey.OP_READ);
			return;
		}
		
		if(pendingData.get(session.getChannel()).isEmpty()){
			session.getSelectionKey().interestOps(SelectionKey.OP_READ);
			return;
		}
		
		Object data = pendingData.get(session.getChannel()).get(0);
		pendingData.get(session.getChannel()).remove(0);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(outputStream);
        objectStream.writeObject(data);
        objectStream.flush();
        
        byte[] finalMessage = new byte[outputStream.size() + 8];
        
        int messageSize = outputStream.size();
        
        for(int i=bytesForMessageSize - 1; i>= 0; i--){
        	if(messageSize != 0){
        		finalMessage[i] = (byte) (messageSize % 10);
        		messageSize /= 10;
        	}
        	else
        		finalMessage[i] = (byte) 0;
        }
        
        System.arraycopy(outputStream.toByteArray(), 0, finalMessage, 8, outputStream.size());
		final ByteBuffer finalBuffer = ByteBuffer.wrap(finalMessage);
		
		WritableByteChannel ch = (WritableByteChannel)session.getChannel();
	      
	    int bytesOp = 0, bytesTotal = 0;
	    
	    while (finalBuffer.hasRemaining() && (bytesOp = ch.write(finalBuffer)) > 0) bytesTotal += bytesOp;
	    
	    if (bytesOp == -1) {
	      //LOG.info("peer closed write channel");
	      ch.close();
	    }
		
		System.out.println("Pachet trimis");
		objectStream.close();
		
		if(!pendingData.get(session.getChannel()).isEmpty())
			session.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
		else
			session.getSelectionKey().interestOps(SelectionKey.OP_READ);
		
	}
	}

}
