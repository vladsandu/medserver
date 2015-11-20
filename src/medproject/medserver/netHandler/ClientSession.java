package medproject.medserver.netHandler;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import medproject.medlibrary.account.Account;
import medproject.medlibrary.concurrency.Request;
import medproject.medserver._serverRunner.MainServer;
import medproject.medserver.constants.Settings;

public class ClientSession {

	SelectionKey selectionKey;
    SocketChannel channel;
    ByteBuffer readBuffer;
    ByteBuffer writeBuffer;

    private String certificateKey = null;
	
    private boolean finishedReading = true;
    private boolean finishedWriting = true;
	
    private int currentMessageByteSize = 0;
	
    Account account = new Account(-1, "unset", "unset");
    
    ClientSession(SelectionKey selkey, SocketChannel chan) throws Throwable {
            this.selectionKey = selkey;
            this.channel = (SocketChannel) chan.configureBlocking(false); // asynchronous/non-blocking
            readBuffer = ByteBuffer.allocateDirect(Settings.readBufferCapacity); // 64 kilobyte capacity
            writeBuffer = ByteBuffer.allocateDirect(Settings.writeBufferCapacity);
    }

    void disconnect() {
            MainServer.clientMap.remove(selectionKey);
            try {
                    if (selectionKey != null)
                    	selectionKey.cancel();

                    if (channel == null)
                            return;

                    //System.out.println("bye bye " + (InetSocketAddress) channel.getRemoteAddress());
                    channel.close();
            } catch (Throwable t) { /** quietly ignore  */ }
    }

	public SelectionKey getSelectionKey() {
		return selectionKey;
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public ByteBuffer getReadBuffer() {
		return readBuffer;
	}

	public ByteBuffer getWriteBuffer() {
		return writeBuffer;
	}

	public Account getOperator() {
		return account;
	}

	public void setOperator(Account account) {
		this.account = account;
	}

	public int getCurrentMessageByteSize() {
		return currentMessageByteSize;
	}

	public void setCurrentMessageByteSize(int currentMessageByteSize) {
		this.currentMessageByteSize = currentMessageByteSize;
	}

	public String getCertificateKey() {
		return certificateKey;
	}

	public void setCertificateKey(String certificateKey) {
		this.certificateKey = certificateKey;
	}

	public boolean isFinishedReading() {
		return finishedReading;
	}

	public void setFinishedReading(boolean finishedReading) {
		this.finishedReading = finishedReading;
	}

	public boolean isFinishedWriting() {
		return finishedWriting;
	}

	public void setFinishedWriting(boolean finishedWriting) {
		this.finishedWriting = finishedWriting;
	}
}
