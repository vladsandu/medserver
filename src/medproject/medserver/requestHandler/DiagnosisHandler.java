package medproject.medserver.requestHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import medproject.medlibrary.concurrency.Request;
import medproject.medlibrary.concurrency.RequestCodes;
import medproject.medlibrary.concurrency.RequestStatus;
import medproject.medlibrary.examination.Diagnosis;
import medproject.medlibrary.examination.Examination;
import medproject.medlibrary.examination.ExaminationType;
import medproject.medlibrary.logging.LogWriter;
import medproject.medserver.databaseHandler.DatabaseRequestTemplate;
import medproject.medserver.netHandler.ClientSession;

public class DiagnosisHandler {

	private final Logger LOG = LogWriter.getLogger(this.getClass().getName());

	private final DatabaseRequestTemplate databaseRequestTemplate;

	public DiagnosisHandler(DatabaseRequestTemplate databaseRequestTemplate) {
		this.databaseRequestTemplate = databaseRequestTemplate;
	}

	public void handleRequest(ClientSession session, Request request){
		switch(request.getREQUEST_CODE()){
		case RequestCodes.DIAGNOSIS_LIST_REQUEST:
			handleDiagnosisListRequest(session, request); break;
		}
	}

	private void handleDiagnosisListRequest(ClientSession session, Request request) {
		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeDiagnosisListRequest(session);
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("The database couldn't process the request");
				return;
			}

			ResultSet results = (ResultSet) request.getDATA();

			List<Diagnosis> diagnosisList = new ArrayList<Diagnosis>();
			request.setDATA(diagnosisList);
			try {	
				while(results.next()){

					Diagnosis diagnosis = new Diagnosis(
							results.getInt("id"), 
							results.getInt("consultatie_id"),
							results.getInt("catalog_diagnostic_id"), 
							results.getString("observatii"), 
							results.getBoolean("activ"), 
							results.getDate("data_terminare"));
					diagnosisList.add(diagnosis);
				}

				if(diagnosisList.size() > 0){
					request.setMessage("Diagnosis selection successful");
				}
				else{
					request.setMessage("No diagnosis found.");
				}

				request.setStatus(RequestStatus.REQUEST_COMPLETED);

			} catch (SQLException e) {
				LOG.severe("DiagnosisList Error: " + e.getMessage());
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("Database Exception Error");
			}
		}
	}

}
