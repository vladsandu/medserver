package medproject.medserver.requestHandler;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import medproject.medlibrary.concurrency.Request;
import medproject.medlibrary.concurrency.RequestCodes;
import medproject.medlibrary.concurrency.RequestStatus;
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
public class PatientHandler extends AbstractHandler{

	private final DatabaseRequestTemplate databaseRequestTemplate;

	public PatientHandler(DatabaseRequestTemplate databaseRequestTemplate) {
		this.databaseRequestTemplate = databaseRequestTemplate;
	}

	public void handleRequest(ClientSession session, Request request) throws SQLException{
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
		case RequestCodes.DECEASED_PATIENT_REQUEST:
			handleDeceasedPatientRequest(session, request); 				break;

		default: 															break;
		}
	}

	private void handleDeletePatientRequest(ClientSession session, Request request) throws SQLException {
		if(verifyNullRequestData(request))	return;

		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeDeletePatientRequest(session, (int)request.getDATA());
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			int affectedRows = (int) request.getDATA();

			if(affectedRows == 1){
				request.setMessage("Patient delete successful");
				request.setStatus(RequestStatus.REQUEST_COMPLETED);
			}
			else
				throw new SQLException("Couldn't delete patient.");
		}

	}

	private void handleUnregisterPatientRequest(ClientSession session, Request request) throws SQLException {
		if(verifyNullRequestData(request))	return;

		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeUnregisterPatientRequest(session, (int)request.getDATA());

		}//TODO: refactor into smaller methods
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			ResultSet results = (ResultSet) request.getDATA();

			if(results.next()){
				Date unregistrationDate;

				unregistrationDate = results.getDate("data_iesire");

				request.setDATA(unregistrationDate);

				request.setMessage("Patient unregistration successful");
				request.setStatus(RequestStatus.REQUEST_COMPLETED);
			}
			else
				throw new SQLException("Couldn't unregister patient.");
		}
	}


	private void handleDeceasedPatientRequest(ClientSession session, Request request) throws SQLException {
		if(verifyNullRequestData(request))	return;

		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeDeceasedPatientRequest(session, (int)request.getDATA());

		}//TODO: refactor into smaller methods
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			ResultSet results = (ResultSet) request.getDATA();
			if(results.next()){
				Date deceaseDate;

				deceaseDate = results.getDate("data_deces");

				request.setDATA(deceaseDate);

				request.setMessage("Patient update successful");
				request.setStatus(RequestStatus.REQUEST_COMPLETED);
			}
			else
				throw new SQLException("Couldn't make patient deceased.");
		}

	}


	private void handleRegisterPatientRequest(ClientSession session, Request request) throws SQLException {
		if(verifyNullRequestData(request))	return;

		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeRegisterPatientRequest(session, (int)request.getDATA());

		}//TODO: refactor into smaller methods
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			ResultSet results = (ResultSet) request.getDATA();
			if(results.next()){
				Date registrationDate;

				registrationDate = results.getDate("data_inscriere");

				request.setDATA(registrationDate);

				request.setMessage("Patient registration successful");
				request.setStatus(RequestStatus.REQUEST_COMPLETED);
			}
			else
				throw new SQLException("Couldn't register patient.");
		}

	}


	private void handleUpdatePatientAddressRequest(ClientSession session, Request request) throws SQLException {
		if(verifyNullRequestData(request))	return;

		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeUpdatePatientAddressRequest(session, (Address)request.getDATA());

		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			int affectedRows = (int) request.getDATA();

			if(affectedRows == 1){
				request.setMessage("Address update successful");
				request.setStatus(RequestStatus.REQUEST_COMPLETED);
			}
			else
				throw new SQLException("Couldn't update patient address.");
		}
	}

	private void handleAddPatientRequest(ClientSession session, Request request) throws SQLException{
		if(verifyNullRequestData(request))	return;

		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeAddPatientRequest(session, (int)request.getDATA(), request.getPIN());
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			ResultSet results = (ResultSet) request.getDATA();

			Patient patient = null;

			if(results.next()){
				Address address = getAddressFromResultSet(results);
				PatientRecord patientRecord = getPatientRecordFromResultSet(results, address);
				RegistrationRecord registrationRecord = getRegistrationRecordFromResultSet(results);
				
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
		}		
	}

	private void handlePatientRecordByCNPRequest(ClientSession session, Request request) throws SQLException{
		if(verifyNullRequestData(request))	return;

		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makePatientRecordByCNPRequest(session, (String)request.getDATA());

		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			ResultSet results = (ResultSet) request.getDATA();

			List<PatientRecord> patientRecordList = new ArrayList<>();
			request.setDATA(patientRecordList);

			while(results.next()){
				Address address = getAddressFromResultSet(results);
				PatientRecord patientRecord = getPatientRecordFromResultSet(results, address);
				
				patientRecordList.add(patientRecord);
			}

			if(patientRecordList.size() > 0){
				request.setMessage("Person selection successful");
			}
			else{
				request.setMessage("No persons found.");
			}

			request.setStatus(RequestStatus.REQUEST_COMPLETED);
		}
	}

	private void handlePatientListRequest(ClientSession session, Request request) throws SQLException {
		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makePatientListRequest(session);
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			if(verifyNullRequestData(request))	return;

			ResultSet results = (ResultSet) request.getDATA();

			List<Patient> patientList = new ArrayList<Patient>();
			request.setDATA(patientList);
			while(results.next()){
				Address address = getAddressFromResultSet(results);
				PatientRecord patientRecord = getPatientRecordFromResultSet(results, address);
				RegistrationRecord registrationRecord = getRegistrationRecordFromResultSet(results);
				
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
		}
	}
	
	private Address getAddressFromResultSet(ResultSet results) throws SQLException{
		Address address = new Address(
				results.getInt("id"),
				results.getString("judet"),
				results.getString("localitate"),
				results.getString("strada"));
		
		return address;
	}
	
	private PatientRecord getPatientRecordFromResultSet(ResultSet results, Address address) throws SQLException{
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

		return patientRecord;
	}
	
	private RegistrationRecord getRegistrationRecordFromResultSet(ResultSet results) throws SQLException{
		RegistrationRecord registrationRecord = new RegistrationRecord(
				results.getDate("data_inscriere"), 
				results.getDate("data_iesire"), 
				results.getInt("inscris") == 1 ? true : false);

		return registrationRecord;
	}
}
