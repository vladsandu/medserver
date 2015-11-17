package medproject.medserver.netHandler;

import java.nio.channels.SocketChannel;

public class ChangeRequest {
	public static final int CHANGEOPS = 1;
	
	private final SocketChannel socket;
	private final int ops;
	
	public ChangeRequest(SocketChannel socket, int ops) {
		this.socket = socket;
		this.ops = ops;
	}

	public SocketChannel getSocket() {
		return socket;
	}

	public int getOps() {
		return ops;
	}
	
}
