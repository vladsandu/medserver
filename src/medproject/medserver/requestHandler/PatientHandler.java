package medproject.medserver.requestHandler;

import java.util.logging.Logger;

import medproject.medlibrary.concurrency.Request;
import medproject.medlibrary.concurrency.RequestCodes;
import medproject.medserver.databaseHandler.DatabaseRequestTemplate;
import medproject.medserver.logging.LogWriter;
import medproject.medserver.netHandler.ClientSession;

public class PatientHandler {
	private final Logger LOG = LogWriter.getLogger(this.getClass().getName());

	private final DatabaseRequestTemplate databaseRequestTemplate;

	public PatientHandler(DatabaseRequestTemplate databaseRequestTemplate) {
		this.databaseRequestTemplate = databaseRequestTemplate;
	}

	public void handleRequest(ClientSession session, Request request){
		switch(request.getREQUEST_CODE()){
		case RequestCodes.PATIENT_LIST_REQUEST:
			handlePacientListRequest(session, request); 			break;
		default: 												break;
		}
	}

	private void handlePacientListRequest(ClientSession session, Request request) {
		// TODO Auto-generated method stub
		
	}

}
