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
import medproject.medlibrary.patient.ListingRecord;
import medproject.medlibrary.patient.Patient;
import medproject.medlibrary.patient.PatientCategory;
import medproject.medlibrary.patient.PatientRecord;
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
					Address address = new Address(
							results.getString("judet"),
							results.getString("localitate"),
							results.getString("strada"));
					
					PatientRecord patientRecord = new PatientRecord(address,
							results.getString("cnp"), 
							results.getString("nume"), 
							results.getString("prenume"), 
							(results.getInt("sex") == 1 ? "M" : "F"), 
							results.getDate("data_nastere"),
							results.getDate("data_deces"),
							results.getString("cetatenie"), 
							results.getInt("grupa_sanguina"), 
							results.getInt("rh"));
					
					ListingRecord listingRecord = new ListingRecord(
							results.getDate("data_inscriere"), 
							results.getDate("data_iesire"), 
							results.getBoolean("inscris"));
					
					Patient patient = new Patient(
							results.getInt("pacient_id"),
							patientRecord, 
							listingRecord,
							PatientCategory.getCategoryByID(results.getInt("categorie")),
							PatientStatus.getStatusByID(results.getInt("stare_asigurat")));
					
					patientList.add(patient);
				}
				
				if(patientList.size() > 0){
					request.setMessage("Patient selection successful");
				}
				else{
					request.setMessage("No patients found.");
				}
				
				request.setStatus(RequestStatus.REQUEST_COMPLETED);
				
			} catch (SQLException e) {
				LOG.severe("Patient Handle Error: " + e.getMessage());
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("Database Exception Error");
			}
		}

	}

}
