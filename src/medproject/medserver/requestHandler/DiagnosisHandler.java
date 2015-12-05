package medproject.medserver.requestHandler;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import medproject.medlibrary.concurrency.Request;
import medproject.medlibrary.concurrency.RequestCodes;
import medproject.medlibrary.concurrency.RequestStatus;
import medproject.medlibrary.diagnosis.Diagnosis;
import medproject.medlibrary.diagnosis.DiagnosisInfo;
import medproject.medlibrary.logging.LogWriter;
import medproject.medserver.databaseHandler.DatabaseRequestTemplate;
import medproject.medserver.databaseHandler.StoredProcedure;
import medproject.medserver.netHandler.ClientSession;
import oracle.jdbc.internal.OracleTypes;

public class DiagnosisHandler {

	private final Logger LOG = LogWriter.getLogger(this.getClass().getName());

	private final DatabaseRequestTemplate databaseRequestTemplate;

	private final List<DiagnosisInfo> diagnosisInfoList;

	public DiagnosisHandler(DatabaseRequestTemplate databaseRequestTemplate) {
		this.databaseRequestTemplate = databaseRequestTemplate;
		this.diagnosisInfoList = new ArrayList<DiagnosisInfo>();
		initializeDiagnosisInfoList();
	}

	public void handleRequest(ClientSession session, Request request){
		switch(request.getREQUEST_CODE()){
		case RequestCodes.DIAGNOSIS_LIST_REQUEST:
			handleDiagnosisListRequest(session, request); break;
		case RequestCodes.DIAGNOSIS_INFO_LIST_REQUEST:
			handleDiagnosisInfoListRequest(session, request); break;
		}
	}

	private void initializeDiagnosisInfoList(){
		try {
			CallableStatement statement = databaseRequestTemplate.getDatabaseConnection().prepareCall(
					StoredProcedure.LoadDiagnosisInfoList.getSQL());

			statement.registerOutParameter(1, OracleTypes.CURSOR);
			statement.execute();
			
			ResultSet results = (ResultSet) statement.getObject(1);

			while(results.next()){
				DiagnosisInfo diagnosis = new DiagnosisInfo(
						results.getInt("id"), 
						results.getString("denumire")
						);

				diagnosisInfoList.add(diagnosis);
			}

			if(diagnosisInfoList.size() > 0)
				LOG.fine("Diagnosis information loaded");
			else
				throw new IOException("List empty");
			
			results.close();
		} catch (SQLException | IOException e) {
			LOG.severe("DiagnosisList Error: " + e.getMessage());
		}
	}

	private void handleDiagnosisInfoListRequest(ClientSession session, Request request) {
		request.setDATA(diagnosisInfoList);
		
		if(diagnosisInfoList.size() > 0){
			request.setMessage("Diagnosis information found.");
			request.setStatus(RequestStatus.REQUEST_COMPLETED);
		}
		else{
			request.setMessage("Diagnosis information missing.");
			request.setStatus(RequestStatus.REQUEST_FAILED);
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
