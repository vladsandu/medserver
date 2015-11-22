package medproject.medserver.requestHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import medproject.medlibrary.concurrency.Request;
import medproject.medlibrary.concurrency.RequestCodes;
import medproject.medlibrary.concurrency.RequestStatus;
import medproject.medlibrary.patient.Address;
import medproject.medlibrary.patient.Patient;
import medproject.medlibrary.patient.PatientCategory;
import medproject.medlibrary.patient.PatientStatus;
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
			handlePatientListRequest(session, request); 			break;
		default: 													break;
		}
	}

	private void handlePatientListRequest(ClientSession session, Request request) {
		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makePatientListRequest(session);
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			ResultSet results = (ResultSet) request.getDATA();

			List<Patient> patientList = new ArrayList<Patient>();
			request.setDATA(patientList);
			try {	
				while(results.next()){
					Patient patient = new Patient(
							results.getInt("id"),
							results.getInt("cnp"), 
							results.getString("nume"), 
							results.getString("prenume"), 
							(results.getInt("gen") == 1 ? "Masculin" : "Feminin"), 
							results.getDate("data_nastere").toString(),
							results.getString("cetatenie"),
							new Address(null, null, null), 
							PatientCategory.getCategoryByID(results.getInt("categorie")),
							results.getDate("data_inscriere").toString(), 
							PatientStatus.getStatusByID(results.getInt("stare_asigurat")));
					
					patientList.add(patient);
				}
				
				if(patientList.size() > 0){
					request.setStatus(RequestStatus.REQUEST_COMPLETED);
					request.setMessage("Patient selection successful");
				}
				else{
					request.setStatus(RequestStatus.REQUEST_FAILED);
					request.setMessage("No patients found.");
				}
			
			} catch (SQLException e) {
				LOG.severe("Patient Handle Error: " + e.getMessage());
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("Database Exception Error");
			}
		}

	}

}
