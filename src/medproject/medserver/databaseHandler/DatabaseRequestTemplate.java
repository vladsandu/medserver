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
	
	public void makePatientRecordByCNPRequest(ClientSession session, String CNP){
		
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.PATIENT_RECORD_BY_CNP_REQUEST, StoredProcedure.PatientRecordByCNP);
	
		databaseRequest.addStringValue(2, CNP);
		
		makeDatabaseRequest(databaseRequest);
	}
	
	public void makePatientListRequest(ClientSession session){
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.PATIENT_LIST_REQUEST, StoredProcedure.LoadPatientList);
		databaseRequest.addIntValue(2, session.getOperator().getOperatorID());
		
		makeDatabaseRequest(databaseRequest);
	}

	public void makeAddPatientRequest(ClientSession session, int pid, int pin) {
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.ADD_PATIENT_REQUEST, StoredProcedure.AddPatient);
		databaseRequest.addIntValue(2, session.getOperator().getOperatorID());
		databaseRequest.addIntValue(3, pid);
		databaseRequest.addIntValue(4, pin);
		
		makeDatabaseRequest(databaseRequest);
	
	}
	
	private void makeDatabaseRequest(DatabaseRequest currentRequest){
		synchronized(databaseRequests){
			databaseRequests.offer(currentRequest);
		}
	}

}
