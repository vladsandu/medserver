package medproject.medserver.netHandler;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import medproject.medlibrary.account.Account;
import medproject.medlibrary.concurrency.Request;
import medproject.medserver._serverRunner.MainServer;

public class ClientSession {

	SelectionKey selectionKey;
    SocketChannel channel;
    ByteBuffer buffer;

    Request currentRequest;
    
    private boolean finishedReading = true;
	private int currentMessageByteSize = 0;
	
    Account account = new Account(-1, "unset", "unset");
    
    ClientSession(SelectionKey selkey, SocketChannel chan) throws Throwable {
            this.selectionKey = selkey;
            this.channel = (SocketChannel) chan.configureBlocking(false); // asynchronous/non-blocking
            buffer = ByteBuffer.allocateDirect(65000); // 64 kilobyte capacity
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

	public ByteBuffer getBuffer() {
		return buffer;
	}

	public Request getCurrentRequest() {
		return currentRequest;
	}

	public void setCurrentRequest(Request currentRequest) {
		this.currentRequest = currentRequest;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public boolean isFinishedReading() {
		return finishedReading;
	}

	public void setFinishedReading(boolean finishedReading) {
		this.finishedReading = finishedReading;
	}

	public int getCurrentMessageByteSize() {
		return currentMessageByteSize;
	}

	public void setCurrentMessageByteSize(int currentMessageByteSize) {
		this.currentMessageByteSize = currentMessageByteSize;
	}
}
