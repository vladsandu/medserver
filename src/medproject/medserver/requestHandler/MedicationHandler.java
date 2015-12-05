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
import medproject.medlibrary.logging.LogWriter;
import medproject.medlibrary.medication.Drug;
import medproject.medlibrary.medication.Medication;
import medproject.medserver.databaseHandler.DatabaseRequestTemplate;
import medproject.medserver.databaseHandler.StoredProcedure;
import medproject.medserver.netHandler.ClientSession;
import oracle.jdbc.internal.OracleTypes;

public class MedicationHandler {

	private final Logger LOG = LogWriter.getLogger(this.getClass().getName());

	private final DatabaseRequestTemplate databaseRequestTemplate;

	private final List<Drug> drugList;

	public MedicationHandler(DatabaseRequestTemplate databaseRequestTemplate) {
		this.databaseRequestTemplate = databaseRequestTemplate;
		this.drugList = new ArrayList<Drug>();
		initializeDrugList();
	}

	public void handleRequest(ClientSession session, Request request){
		switch(request.getREQUEST_CODE()){
		case RequestCodes.DRUG_LIST_REQUEST:
			handleDrugListRequest(session, request);	break;
		case RequestCodes.MEDICATION_LIST_REQUEST:
			handleMedicationListRequest(session, request); break;
		}
	}

	private void initializeDrugList(){
		try {
			CallableStatement statement = databaseRequestTemplate.getDatabaseConnection().prepareCall(
					StoredProcedure.LoadDrugList.getSQL());

			statement.registerOutParameter(1, OracleTypes.CURSOR);
			statement.execute();

			ResultSet results = (ResultSet) statement.getObject(1);

			while(results.next()){
				Drug drug = new Drug(
						0, null, null, 0, null, false, false, false, 0, null, null, null, null
						);

				drugList.add(drug);
			}

			if(drugList.size() > 0)
				LOG.fine("Drug list loaded");
			else
				throw new IOException("List empty");

			results.close();
		} catch (SQLException | IOException e) {
			LOG.severe("DrugList Error: " + e.getMessage());
		}
	}


	private void handleDrugListRequest(ClientSession session, Request request) {
		request.setDATA(drugList);

		if(drugList.size() > 0){
			request.setMessage("Drug list found.");
			request.setStatus(RequestStatus.REQUEST_COMPLETED);
		}
		else{
			request.setMessage("Drug list missing.");
			request.setStatus(RequestStatus.REQUEST_FAILED);
		}
	}

	private void handleMedicationListRequest(ClientSession session, Request request) {
		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeMedicationListRequest(session);
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("The database couldn't process the request");
				return;
			}

			ResultSet results = (ResultSet) request.getDATA();

			List<Medication> medicationList = new ArrayList<Medication>();
			request.setDATA(medicationList);
			try {	
				while(results.next()){

					Medication medication = new Medication(0, 0, 0, 0, 0, 0, null, null);
					medicationList.add(medication);
				}

				if(medicationList.size() > 0){
					request.setMessage("Medication selection successful");
				}
				else{
					request.setMessage("No medication found.");
				}

				request.setStatus(RequestStatus.REQUEST_COMPLETED);

			} catch (SQLException e) {
				LOG.severe("MedicationList Error: " + e.getMessage());
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("Database Exception Error");
			}
		}
	}

}
