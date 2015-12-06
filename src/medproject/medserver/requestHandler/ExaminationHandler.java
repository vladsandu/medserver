package medproject.medserver.requestHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import medproject.medlibrary.concurrency.Request;
import medproject.medlibrary.concurrency.RequestCodes;
import medproject.medlibrary.concurrency.RequestStatus;
import medproject.medlibrary.examination.Examination;
import medproject.medlibrary.examination.ExaminationType;
import medproject.medlibrary.logging.LogWriter;
import medproject.medlibrary.patient.Patient;
import medproject.medserver.databaseHandler.DatabaseRequestTemplate;
import medproject.medserver.netHandler.ClientSession;

public class ExaminationHandler {

	private final Logger LOG = LogWriter.getLogger(this.getClass().getName());

	private final DatabaseRequestTemplate databaseRequestTemplate;

	public ExaminationHandler(DatabaseRequestTemplate databaseRequestTemplate) {
		this.databaseRequestTemplate = databaseRequestTemplate;
	}

	public void handleRequest(ClientSession session, Request request){
		switch(request.getREQUEST_CODE()){
		case RequestCodes.EXAMINATION_LIST_REQUEST:
			handleExaminationListRequest(session, request); break;
		case RequestCodes.ADD_EXAMINATION_REQUEST:
			handleAddExaminationRequest(session, request); break;
		}
	}

	private void handleAddExaminationRequest(ClientSession session, Request request) {
		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeAddExaminationRequest(session, (Examination) request.getDATA(), request.getPIN());
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("The database couldn't process the request");
				return;
			}

			ResultSet results = (ResultSet) request.getDATA();
			
			try {
				if(results.next()){
					Examination examination = new Examination(
							results.getInt("id"), 
							results.getInt("pacient_id"), 
							results.getDate("data_consultatie"), 
							results.getInt("diagnostic_id"),
							results.getString("observatii"), 
							ExaminationType.getExaminationTypeByInt(results.getInt("tip")));
					
					request.setDATA(examination);
					request.setMessage("Examination added");
					request.setStatus(RequestStatus.REQUEST_COMPLETED);
				}
				else{
					throw new SQLException("Result set empty");
				}
			} catch (SQLException e) {
				LOG.severe("Add Examination Error: " + e.getMessage());
				request.setDATA(null);
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("Database Exception Error");
			}
		}

	}

	private void handleExaminationListRequest(ClientSession session, Request request) {
		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeExaminationListRequest(session);
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("The database couldn't process the request");
				return;
			}

			ResultSet results = (ResultSet) request.getDATA();

			List<Examination> examinationList = new ArrayList<Examination>();
			request.setDATA(examinationList);
			try {	
				while(results.next()){

					Examination examination = new Examination(
							results.getInt("id"), 
							results.getInt("pacient_id"), 
							results.getDate("data_consultatie"), 
							results.getInt("diagnostic_id"),
							results.getString("observatii"), 
							ExaminationType.getExaminationTypeByInt(results.getInt("tip")));

					examinationList.add(examination);
				}

				if(examinationList.size() > 0){
					request.setMessage("Examination selection successful");
				}
				else{
					request.setMessage("No examinations found.");
				}

				request.setStatus(RequestStatus.REQUEST_COMPLETED);

			} catch (SQLException e) {
				LOG.severe("ExaminationList Error: " + e.getMessage());
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("Database Exception Error");
			}
		}
	}
}