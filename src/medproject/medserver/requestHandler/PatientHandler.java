package medproject.medserver.requestHandler;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import medproject.medlibrary.concurrency.Request;
import medproject.medlibrary.concurrency.RequestCodes;
import medproject.medlibrary.concurrency.RequestStatus;
import medproject.medlibrary.logging.LogWriter;
import medproject.medlibrary.patient.Address;
import medproject.medlibrary.patient.BloodType;
import medproject.medlibrary.patient.Gender;
import medproject.medlibrary.patient.Patient;
import medproject.medlibrary.patient.PatientCategory;
import medproject.medlibrary.patient.PatientRecord;
import medproject.medlibrary.patient.PatientStatus;
import medproject.medlibrary.patient.RHType;
import medproject.medlibrary.patient.RegistrationRecord;
import medproject.medserver.databaseHandler.DatabaseRequestTemplate;
import medproject.medserver.netHandler.ClientSession;
//TODO: REFACTOR THIS!!!
public class PatientHandler {
	private final Logger LOG = LogWriter.getLogger(this.getClass().getName());

	private final DatabaseRequestTemplate databaseRequestTemplate;

	public PatientHandler(DatabaseRequestTemplate databaseRequestTemplate) {
		this.databaseRequestTemplate = databaseRequestTemplate;
	}

	public void handleRequest(ClientSession session, Request request){
		switch(request.getREQUEST_CODE()){
		case RequestCodes.PATIENT_LIST_REQUEST:
			handlePatientListRequest(session, request); 					break;
		case RequestCodes.PATIENT_RECORD_BY_CNP_REQUEST:
			handlePatientRecordByCNPRequest(session, request); 				break;
		case RequestCodes.ADD_PATIENT_REQUEST:
			handleAddPatientRequest(session, request); 						break;
		case RequestCodes.UPDATE_PATIENT_ADDRESS_REQUEST:
			handleUpdatePatientAddressRequest(session, request); 			break;
		case RequestCodes.DELETE_PATIENT_REQUEST:
			handleDeletePatientRequest(session, request); 					break;
		case RequestCodes.UNREGISTER_PATIENT_REQUEST:
			handleUnregisterPatientRequest(session, request); 				break;
		case RequestCodes.REGISTER_PATIENT_REQUEST:
			handleRegisterPatientRequest(session, request); 				break;

		default: 															break;
		}
	}

	private void handleDeletePatientRequest(ClientSession session, Request request) {
		if(request.getStatus() == RequestStatus.REQUEST_NEW){

			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("Invalid Data");
			}
			else
				databaseRequestTemplate.makeDeletePatientRequest(session, (int)request.getDATA());

		}//TODO: refactor into smaller methods
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("The database couldn't process the request");
				return;
			}

			int affectedRows = (int) request.getDATA();

			if(affectedRows == 1){
				request.setMessage("Patient delete successful");
				request.setStatus(RequestStatus.REQUEST_COMPLETED);
			}
			else{
				request.setMessage("Patient delete failed.");
				request.setStatus(RequestStatus.REQUEST_FAILED);
			}
		}

	}

	private void handleUnregisterPatientRequest(ClientSession session, Request request) {
		if(request.getStatus() == RequestStatus.REQUEST_NEW){

			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("Invalid Data");
			}
			else
				databaseRequestTemplate.makeUnregisterPatientRequest(session, (int)request.getDATA());

		}//TODO: refactor into smaller methods
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("The database couldn't process the request");
				return;
			}

			if(request.getDATA() != null){

				try {
					ResultSet results = (ResultSet) request.getDATA();
					if(results.next()){
						Date unregistrationDate;

						unregistrationDate = results.getDate("data_iesire");

						request.setDATA(unregistrationDate);

						request.setMessage("Patient unregistration successful");
						request.setStatus(RequestStatus.REQUEST_COMPLETED);
					}
					else{
						request.setDATA(null);
						request.setMessage("Patient unregistration failed.");
						request.setStatus(RequestStatus.REQUEST_FAILED);
					}
				} catch (SQLException e) {
					request.setDATA(null);
					request.setMessage("Patient unregistration failed.");
					request.setStatus(RequestStatus.REQUEST_FAILED);
				}
			}
		}

	}


	private void handleRegisterPatientRequest(ClientSession session, Request request) {
		if(request.getStatus() == RequestStatus.REQUEST_NEW){

			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("Invalid Data");
			}
			else
				databaseRequestTemplate.makeRegisterPatientRequest(session, (int)request.getDATA());

		}//TODO: refactor into smaller methods
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("The database couldn't process the request");
				return;
			}

			if(request.getDATA() != null){

				try {
					ResultSet results = (ResultSet) request.getDATA();
					if(results.next()){
						Date registrationDate;

						registrationDate = results.getDate("data_inscriere");

						request.setDATA(registrationDate);

						request.setMessage("Patient registration successful");
						request.setStatus(RequestStatus.REQUEST_COMPLETED);
					}
					else{
						request.setDATA(null);
						request.setMessage("Patient registration failed.");
						request.setStatus(RequestStatus.REQUEST_FAILED);
					}
				} catch (SQLException e) {
					request.setDATA(null);
					request.setMessage("Patient registration failed.");
					request.setStatus(RequestStatus.REQUEST_FAILED);
				}
			}
		}

	}

	
	private void handleUpdatePatientAddressRequest(ClientSession session, Request request) {
		if(request.getStatus() == RequestStatus.REQUEST_NEW){

			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("Invalid Data");
			}
			else
				databaseRequestTemplate.makeUpdatePatientAddressRequest(session, (Address)request.getDATA());

		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("The database couldn't process the request");
				return;
			}

			int affectedRows = (int) request.getDATA();

			if(affectedRows == 1){
				request.setMessage("Address update successful");
				request.setStatus(RequestStatus.REQUEST_COMPLETED);
			}
			else{
				request.setMessage("Update failed.");
				request.setStatus(RequestStatus.REQUEST_FAILED);
			}
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
							results.getInt("id"),
							results.getString("judet"),
							results.getString("localitate"),
							results.getString("strada"));

					PatientRecord patientRecord = new PatientRecord(results.getInt("id"),
							address,
							results.getString("cnp"), 
							results.getString("nume"), 
							results.getString("prenume"), 
							(results.getInt("sex") == 1 ? Gender.MASCULIN : Gender.FEMININ), 
							results.getDate("data_nastere"),
							results.getDate("data_deces"),
							results.getString("cetatenie"),
							PatientCategory.getCategoryByID(results.getInt("categorie")),
							PatientStatus.getStatusByID(results.getInt("stare_asigurat")), 
							BloodType.getBloodTypeFromInt(results.getInt("grupa_sanguina")), 
							(results.getInt("rh") == 1 ? RHType.POZITIV : RHType.NEGATIV));
					//TODO: Refactor
					RegistrationRecord registrationRecord = new RegistrationRecord(
							results.getDate("data_inscriere"), 
							results.getDate("data_iesire"), 
							results.getBoolean("inscris"));

					patient = new Patient(
							results.getInt("pacient_id"),
							patientRecord, 
							registrationRecord);
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
							results.getInt("id"),
							results.getString("judet"),
							results.getString("localitate"),
							results.getString("strada"));

					PatientRecord patientRecord = new PatientRecord(results.getInt("id"),
							address,
							results.getString("cnp"), 
							results.getString("nume"), 
							results.getString("prenume"), 
							(results.getInt("sex") == 1 ? Gender.MASCULIN : Gender.FEMININ), 
							results.getDate("data_nastere"),
							results.getDate("data_deces"),
							results.getString("cetatenie"),
							PatientCategory.getCategoryByID(results.getInt("categorie")),
							PatientStatus.getStatusByID(results.getInt("stare_asigurat")),
							BloodType.getBloodTypeFromInt(results.getInt("grupa_sanguina")), 
							(results.getInt("rh") == 1 ? RHType.POZITIV : RHType.NEGATIV));

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
							results.getInt("id"),
							results.getString("judet"),
							results.getString("localitate"),
							results.getString("strada"));

					PatientRecord patientRecord = new PatientRecord(
							results.getInt("id"),
							address,
							results.getString("cnp"), 
							results.getString("nume"), 
							results.getString("prenume"), 
							(results.getInt("sex") == 1 ? Gender.MASCULIN : Gender.FEMININ), 
							results.getDate("data_nastere"),
							results.getDate("data_deces"),
							results.getString("cetatenie"),
							PatientCategory.getCategoryByID(results.getInt("categorie")),
							PatientStatus.getStatusByID(results.getInt("stare_asigurat")), 
							BloodType.getBloodTypeFromInt(results.getInt("grupa_sanguina")), 
							(results.getInt("rh") == 1 ? RHType.POZITIV : RHType.NEGATIV));

					RegistrationRecord registrationRecord = new RegistrationRecord(
							results.getDate("data_inscriere"), 
							results.getDate("data_iesire"), 
							results.getBoolean("inscris"));

					Patient patient = new Patient(
							results.getInt("pacient_id"),
							patientRecord, 
							registrationRecord);

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
