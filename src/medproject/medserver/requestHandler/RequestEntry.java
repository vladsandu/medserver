package medproject.medserver.requestHandler;

import medproject.medlibrary.concurrency.Request;
import medproject.medserver.netHandler.ClientSession;

public class RequestEntry {

	private final Request request;
	private final ClientSession clientSession;
	
	public RequestEntry(Request request, ClientSession clientSession) {
		super();
		this.request = request;
		this.clientSession = clientSession;
	}
	public Request getRequest() {
		return request;
	}
	public ClientSession getClientSession() {
		return clientSession;
	}
	
	
}
