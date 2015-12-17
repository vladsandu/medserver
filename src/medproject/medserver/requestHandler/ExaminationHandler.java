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

public class ExaminationHandler extends AbstractHandler{

	private final DatabaseRequestTemplate databaseRequestTemplate;

	public ExaminationHandler(DatabaseRequestTemplate databaseRequestTemplate) {
		this.databaseRequestTemplate = databaseRequestTemplate;
	}

	public void handleRequest(ClientSession session, Request request) throws SQLException{
		switch(request.getREQUEST_CODE()){
		case RequestCodes.EXAMINATION_LIST_REQUEST:
			handleExaminationListRequest(session, request); break;
		case RequestCodes.ADD_EXAMINATION_REQUEST:
			handleAddExaminationRequest(session, request); break;
		case RequestCodes.DELETE_EXAMINATION_REQUEST:
			handleDeleteExaminationRequest(session, request); break;
		}
	}

	private void handleDeleteExaminationRequest(ClientSession session, Request request) {
		if(verifyNullRequestData(request))
			return;

		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeDeleteExaminationRequest(session, (int) request.getDATA(), request.getPIN());
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			int affectedRows = (int) request.getDATA();

			if(affectedRows != 1){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("There are completed prescriptions. Cannot delete this examination.");
			}
			else{
				request.setStatus(RequestStatus.REQUEST_COMPLETED);
				request.setMessage("Examination deleted successfully.");
			}
		}		
	}

	private void handleAddExaminationRequest(ClientSession session, Request request) throws SQLException {
		if(verifyNullRequestData(request))
			return;

		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeAddExaminationRequest(session, (Examination) request.getDATA(), request.getPIN());
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			ResultSet results = (ResultSet) request.getDATA();

			if(results.next()){
				Examination examination = getExaminationFromResultSet(results);
				
				request.setDATA(examination);
				request.setMessage("Examination added");
				request.setStatus(RequestStatus.REQUEST_COMPLETED);
			}
			else{
				throw new SQLException("Result set empty");
			}
		}

	}

	private void handleExaminationListRequest(ClientSession session, Request request) throws SQLException {
		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeExaminationListRequest(session);
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			if(verifyNullRequestData(request))
				return;
		}

		ResultSet results = (ResultSet) request.getDATA();

		List<Examination> examinationList = new ArrayList<Examination>();
		request.setDATA(examinationList);

		while(results.next()){
			Examination examination = getExaminationFromResultSet(results);
			examinationList.add(examination);
		}

		if(examinationList.size() > 0){
			request.setMessage("Examination selection successful");
		}
		else{
			request.setMessage("No examinations found.");
		}

		request.setStatus(RequestStatus.REQUEST_COMPLETED);
	}
	
	private Examination getExaminationFromResultSet(ResultSet results) throws SQLException{
		Examination examination = new Examination(
				results.getInt("id"), 
				results.getInt("pacient_id"), 
				results.getDate("data_consultatie"), 
				results.getInt("diagnostic_id"),
				results.getString("observatii"), 
				ExaminationType.getExaminationTypeByInt(results.getInt("tip")));

		return examination;
	}
}