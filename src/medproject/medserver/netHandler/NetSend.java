package medproject.medserver.netHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import medproject.medlibrary.concurrency.Request;
import medproject.medserver.dataWriter.DataWriter;
import medproject.medserver.logging.LogWriter;

public class NetSend {
	
	private final Logger LOG = LogWriter.getLogger(this.getClass().getName());
	private DataWriter dataWriter;
	private int bytesForMessageSize = 8;

	public NetSend(DataWriter dataWriter) {
		this.dataWriter = dataWriter;
	}

	void send(ClientSession session) throws IOException{

		synchronized(dataWriter.getWritingQueue()){
			ConcurrentHashMap<ClientSession, List<Request>> pendingData = dataWriter.getWritingQueue();

			if(!pendingData.containsKey(session)){
				session.getSelectionKey().interestOps(SelectionKey.OP_READ);
				return;
			}

			if(pendingData.get(session).isEmpty()){
				session.getSelectionKey().interestOps(SelectionKey.OP_READ);
				return;
			}

			ByteBuffer writeBuffer = session.getWriteBuffer();
			writeBuffer.clear();

			Object data = pendingData.get(session).remove(0);

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
			writeBuffer = ByteBuffer.wrap(finalMessage);

			WritableByteChannel ch = (WritableByteChannel)session.getChannel();

			int bytesOp = 0, bytesTotal = 0;

			while (writeBuffer.hasRemaining() && (bytesOp = ch.write(writeBuffer)) > 0) bytesTotal += bytesOp;

			if (bytesOp == -1) {
				//LOG.info("peer closed write channel");
				session.disconnect();
			}

			LOG.info("Pachet trimis");
			objectStream.close();

			if(!pendingData.get(session).isEmpty())
				session.getSelectionKey().interestOps(SelectionKey.OP_WRITE);
			else
				session.getSelectionKey().interestOps(SelectionKey.OP_READ);
		}
	}
}
