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
import medproject.medlibrary.patient.PatientRecord;
import medproject.medlibrary.patient.PatientStatus;
import medproject.medlibrary.patient.RegistrationRecord;
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
		case RequestCodes.PATIENT_RECORD_BY_CNP_REQUEST:
			handlePatientRecordByCNPRequest(session, request); 			break;
		case RequestCodes.ADD_PATIENT_REQUEST:
			handleAddPatientRequest(session, request); 			break;

		default: 													break;
		}
	}

	private void handleAddPatientRequest(ClientSession session, Request request){
		if(request.getStatus() == RequestStatus.REQUEST_NEW){

			if(request.getDATA() == null)
				request.setStatus(RequestStatus.REQUEST_FAILED);
			else{
				int PID = (int) request.getDATA();
				
				databaseRequestTemplate.makeAddPatientRequest(session, PID, request.getPIN());
			}
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("The database couldn't process the request");
				return;
			}
			
			ResultSet results = (ResultSet) request.getDATA();

			try {	
				Patient patient = null;
				
				if(results.next()){
					Address address = new Address(
							results.getString("judet"),
							results.getString("localitate"),
							results.getString("strada"));

					PatientRecord patientRecord = new PatientRecord(results.getInt("id"),
							address,
							results.getString("cnp"), 
							results.getString("nume"), 
							results.getString("prenume"), 
							(results.getInt("sex") == 1 ? "M" : "F"), 
							results.getDate("data_nastere"),
							results.getDate("data_deces"),
							results.getString("cetatenie"), 
							results.getInt("grupa_sanguina"), 
							results.getInt("rh"));

					RegistrationRecord registrationRecord = new RegistrationRecord(
							results.getDate("data_inscriere"), 
							results.getDate("data_iesire"), 
							results.getBoolean("inscris"));

					patient = new Patient(
							results.getInt("pacient_id"),
							patientRecord, 
							registrationRecord,
							PatientCategory.getCategoryByID(results.getInt("categorie")),
							PatientStatus.getStatusByID(results.getInt("stare_asigurat")));
				}
				
				if(patient != null){
					request.setMessage("Person selection successful");
					request.setStatus(RequestStatus.REQUEST_COMPLETED);
				}
				else{
					request.setMessage("PIN incorrect");
					request.setStatus(RequestStatus.REQUEST_FAILED);
				}
				
				request.setDATA(patient);

			} catch (SQLException e) {
				LOG.severe("PatientAdd Error: " + e.getMessage());
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setDATA(null);
				request.setMessage("Database Exception Error");
			}
		}		
	}
	
	private void handlePatientRecordByCNPRequest(ClientSession session, Request request){
		if(request.getStatus() == RequestStatus.REQUEST_NEW){

			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("Invalid CNP");
			}
			else
				databaseRequestTemplate.makePatientRecordByCNPRequest(session, (String)request.getDATA());

		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("The database couldn't process the request");
				return;
			}
			
			ResultSet results = (ResultSet) request.getDATA();

			List<PatientRecord> patientRecordList = new ArrayList<>();
			request.setDATA(patientRecordList);

			try {	
				while(results.next()){
					Address address = new Address(
							results.getString("judet"),
							results.getString("localitate"),
							results.getString("strada"));

					PatientRecord patientRecord = new PatientRecord(results.getInt("id"),
							address,
							results.getString("cnp"), 
							results.getString("nume"), 
							results.getString("prenume"), 
							(results.getInt("sex") == 1 ? "M" : "F"), 
							results.getDate("data_nastere"),
							results.getDate("data_deces"),
							results.getString("cetatenie"), 
							results.getInt("grupa_sanguina"), 
							results.getInt("rh"));

					patientRecordList.add(patientRecord);
				}

				if(patientRecordList.size() > 0){
					request.setMessage("Person selection successful");
				}
				else{
					request.setMessage("No persons found.");
				}

				request.setStatus(RequestStatus.REQUEST_COMPLETED);

			} catch (SQLException e) {
				LOG.severe("PatientRecordList Error: " + e.getMessage());
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("Database Exception Error");
			}

		}
	}

	private void handlePatientListRequest(ClientSession session, Request request) {
		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makePatientListRequest(session);
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("The database couldn't process the request");
				return;
			}
			
			ResultSet results = (ResultSet) request.getDATA();

			List<Patient> patientList = new ArrayList<Patient>();
			request.setDATA(patientList);
			try {	
				while(results.next()){
					Address address = new Address(
							results.getString("judet"),
							results.getString("localitate"),
							results.getString("strada"));

					PatientRecord patientRecord = new PatientRecord(
							results.getInt("id"),
							address,
							results.getString("cnp"), 
							results.getString("nume"), 
							results.getString("prenume"), 
							(results.getInt("sex") == 1 ? "M" : "F"), 
							results.getDate("data_nastere"),
							results.getDate("data_deces"),
							results.getString("cetatenie"), 
							results.getInt("grupa_sanguina"), 
							results.getInt("rh"));

					RegistrationRecord registrationRecord = new RegistrationRecord(
							results.getDate("data_inscriere"), 
							results.getDate("data_iesire"), 
							results.getBoolean("inscris"));

					Patient patient = new Patient(
							results.getInt("pacient_id"),
							patientRecord, 
							registrationRecord,
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
				LOG.severe("PatientList Error: " + e.getMessage());
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("Database Exception Error");
			}
		}

	}

}
