package medproject.medserver.requestHandler;

import medproject.medlibrary.concurrency.Request;
import medproject.medlibrary.concurrency.RequestCodes;
import medproject.medserver.databaseHandler.DatabaseThread;
import medproject.medserver.netHandler.ClientSession;

public class LoginHandler {

	private final DatabaseThread databaseThread;
	
	public LoginHandler(DatabaseThread databaseThread) {
		this.databaseThread = databaseThread;
	}

	public boolean handleRequest(ClientSession session, Request request){
		
		switch(request.getREQUEST_CODE()){
		case RequestCodes.OPERATOR_LOOKUP_REQUEST:
			return handleOperatorLookupRequest(session, request);
			break;
		case RequestCodes.LOGIN_REQUEST:
			return handleLoginRequest(session, request);
			break;
		default: break;
		}
		
		return false;
	}
	
	private boolean handleLoginRequest(ClientSession session, Request request) {
		
		return false;
	}

	private boolean handleOperatorLookupRequest(ClientSession session, Request request){
		String operatorName = (String) request.getData();
		
		
		return false;
	}
}
