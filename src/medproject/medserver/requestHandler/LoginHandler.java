package medproject.medserver.requestHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import medproject.medlibrary.concurrency.Request;
import medproject.medlibrary.concurrency.RequestCodes;
import medproject.medlibrary.concurrency.RequestStatus;
import medproject.medserver.databaseHandler.DatabaseRequestTemplate;
import medproject.medserver.logging.LogWriter;
import medproject.medserver.netHandler.ClientSession;

public class LoginHandler {

	private final Logger LOG = LogWriter.getLogger(this.getClass().getName());
	
	private final DatabaseRequestTemplate databaseRequestTemplate;

	public LoginHandler(DatabaseRequestTemplate databaseRequestTemplate) {
		this.databaseRequestTemplate = databaseRequestTemplate;
	}

	public void handleRequest(ClientSession session, Request request){
		switch(request.getREQUEST_CODE()){
		case RequestCodes.OPERATOR_LOOKUP_REQUEST:
			handleOperatorLookupRequest(session, request);	break;
		case RequestCodes.LOGIN_REQUEST:
			handleLoginRequest(session, request); 			break;
		default: 											break;
		}
	}

	private void handleLoginRequest(ClientSession session, Request request) {
		if(request.getStatus() == RequestStatus.REQUEST_NEW){

		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){

		}
	}

	private void handleOperatorLookupRequest(ClientSession session, Request request){
		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			String operatorName = (String) request.getData();

			databaseRequestTemplate.makeOperatorLookupRequest(session, operatorName);
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			ResultSet results = (ResultSet) request.getData();
			try {
			if(results.next())
				System.out.println(results.getString("encrypted_password"));
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
