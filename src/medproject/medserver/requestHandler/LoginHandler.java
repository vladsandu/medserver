package medproject.medserver.requestHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import medproject.medlibrary.account.LoginStructure;
import medproject.medlibrary.account.Operator;
import medproject.medlibrary.concurrency.Request;
import medproject.medlibrary.concurrency.RequestCodes;
import medproject.medlibrary.concurrency.RequestStatus;
import medproject.medlibrary.logging.LogWriter;
import medproject.medserver.databaseHandler.DatabaseRequestTemplate;
import medproject.medserver.netHandler.ClientSession;

public class LoginHandler {

	private final Logger LOG = LogWriter.getLogger(this.getClass().getName());

	private final DatabaseRequestTemplate databaseRequestTemplate;

	public LoginHandler(DatabaseRequestTemplate databaseRequestTemplate) {
		this.databaseRequestTemplate = databaseRequestTemplate;
	}

	public void handleRequest(ClientSession session, Request request){
		switch(request.getREQUEST_CODE()){
		case RequestCodes.LOGIN_REQUEST:
			handleLoginRequest(session, request); 			break;
		default: 											break;
		}
	}

	private void handleLoginRequest(ClientSession session, Request request){
		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			LoginStructure loginStructure = (LoginStructure) request.getDATA();
			
			databaseRequestTemplate.makeLoginRequest(session, loginStructure);
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			ResultSet results = (ResultSet) request.getDATA();
			request.setDATA(null);
			
			try {
				if(results.next()){
					session.setCertificateKey(results.getString("certificate_key"));
					session.setOperator(new Operator(results.getInt("id"),
													results.getString("username"),
													results.getInt("type")));
					request.setStatus(RequestStatus.REQUEST_COMPLETED);
					request.setMessage("Login successful");
				}
				else{
					request.setStatus(RequestStatus.REQUEST_FAILED);
					request.setMessage("Your login information is incorrect");
				}
			
			} catch (SQLException e) {
				LOG.severe("Operator Login Error: " + e.getMessage());
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("Database Exception Error");
			}
		}
	}
}
