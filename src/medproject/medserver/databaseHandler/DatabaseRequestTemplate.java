package medproject.medserver.databaseHandler;

import java.sql.Connection;
import java.util.concurrent.LinkedBlockingQueue;

import medproject.medlibrary.concurrency.Request;
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

	public void makeOperatorLookupRequest(ClientSession session, String operatorName){
		
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.OPERATOR_LOOKUP_REQUEST, StoredProcedure.OperatorLookup);
		
		databaseRequest.addStringValue(2, operatorName);
		
		makeDatabaseRequest(databaseRequest);
	}
	
	private void makeDatabaseRequest(DatabaseRequest currentRequest){
		synchronized(databaseRequests){
			databaseRequests.offer(currentRequest);
		}
	}
}
