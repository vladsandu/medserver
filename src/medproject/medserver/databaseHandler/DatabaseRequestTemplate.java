package medproject.medserver.databaseHandler;

import java.sql.Connection;
import java.util.concurrent.LinkedBlockingQueue;

import medproject.medlibrary.account.LoginStructure;
import medproject.medlibrary.concurrency.RequestCodes;
import medproject.medserver.netHandler.ClientSession;

public class DatabaseRequestTemplate {

	private final Connection databaseConnection;
	private final LinkedBlockingQueue<DatabaseRequest> databaseRequests;

	public DatabaseRequestTemplate(Connection databaseConnection, 
			LinkedBlockingQueue<DatabaseRequest> databaseRequests) {
		this.databaseConnection = databaseConnection;
		this.databaseRequests = databaseRequests;
	}

	public void makeLoginRequest(ClientSession session, LoginStructure loginStructure){
		
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.LOGIN_REQUEST, StoredProcedure.OperatorLogin);
		
		databaseRequest.addStringValue(2, loginStructure.getUsername());
		databaseRequest.addStringValue(3, loginStructure.getEncrypted_password());	
		databaseRequest.addIntValue(4, loginStructure.getOperatorType());	
		
		makeDatabaseRequest(databaseRequest);
	}
	
	private void makeDatabaseRequest(DatabaseRequest currentRequest){
		synchronized(databaseRequests){
			databaseRequests.offer(currentRequest);
		}
	}
}
