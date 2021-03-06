package medproject.medserver.databaseHandler;

import java.sql.Connection;
import java.util.concurrent.LinkedBlockingQueue;

import medproject.medlibrary.account.LoginStructure;
import medproject.medlibrary.concurrency.RequestCodes;
import medproject.medlibrary.diagnosis.Diagnosis;
import medproject.medlibrary.examination.Examination;
import medproject.medlibrary.patient.Address;
import medproject.medserver.netHandler.ClientSession;

public class DatabaseRequestTemplate {

	private final Connection databaseConnection;
	private final LinkedBlockingQueue<DatabaseRequest> databaseRequests;

	public DatabaseRequestTemplate(Connection databaseConnection, 
			LinkedBlockingQueue<DatabaseRequest> databaseRequests) {
		this.databaseConnection = databaseConnection;
		this.databaseRequests = databaseRequests;
	}

	public void makeLoginRequest(ClientSession session, LoginStructure loginStructure){
		
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.LOGIN_REQUEST, StoredProcedure.OperatorLogin);
		
		databaseRequest.addStringValue(2, loginStructure.getUsername());
		databaseRequest.addStringValue(3, loginStructure.getEncrypted_password());	
		databaseRequest.addIntValue(4, loginStructure.getOperatorType());	
		
		makeDatabaseRequest(databaseRequest);
	}
	
	public void makePatientRecordByCNPRequest(ClientSession session, String CNP){
		
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.PATIENT_RECORD_BY_CNP_REQUEST, StoredProcedure.PatientRecordByCNP);
	
		databaseRequest.addStringValue(2, CNP);
		
		makeDatabaseRequest(databaseRequest);
	}
	
	public void makePatientListRequest(ClientSession session){
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.PATIENT_LIST_REQUEST, StoredProcedure.LoadPatientList);
		databaseRequest.addIntValue(2, session.getOperator().getOperatorID());
		
		makeDatabaseRequest(databaseRequest);
	}

	public void makeAddPatientRequest(ClientSession session, int pid, int pin) {
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.ADD_PATIENT_REQUEST, StoredProcedure.AddPatient);
		databaseRequest.addIntValue(2, session.getOperator().getOperatorID());
		databaseRequest.addIntValue(3, pid);
		databaseRequest.addIntValue(4, pin);
		
		makeDatabaseRequest(databaseRequest);
	
	}
	
	public void makeUpdatePatientAddressRequest(ClientSession session, Address data) {
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.UPDATE_PATIENT_ADDRESS_REQUEST, StoredProcedure.UpdatePatientAddress);
		databaseRequest.addIntValue(2, session.getOperator().getOperatorID());
		databaseRequest.addIntValue(3, data.getPersonID());
		databaseRequest.addStringValue(4, data.getCounty());
		databaseRequest.addStringValue(5, data.getCity());
		databaseRequest.addStringValue(6, data.getStreet());
		
		makeDatabaseRequest(databaseRequest);
	}

	public void makeDeletePatientRequest(ClientSession session, int patientID) {
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.DELETE_PATIENT_REQUEST, StoredProcedure.DeletePatient);
		databaseRequest.addIntValue(2, session.getOperator().getOperatorID());
		databaseRequest.addIntValue(3, patientID);
		
		makeDatabaseRequest(databaseRequest);
	}
	
	private void makeDatabaseRequest(DatabaseRequest currentRequest){
		synchronized(databaseRequests){
			databaseRequests.offer(currentRequest);
		}
	}

	public void makeUnregisterPatientRequest(ClientSession session, int patientID) {
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.UNREGISTER_PATIENT_REQUEST, StoredProcedure.UnregisterPatient);
		databaseRequest.addIntValue(2, session.getOperator().getOperatorID());
		databaseRequest.addIntValue(3, patientID);
		
		makeDatabaseRequest(databaseRequest);
	}
	
	public void makeRegisterPatientRequest(ClientSession session, int patientID) {
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.REGISTER_PATIENT_REQUEST, StoredProcedure.RegisterPatient);
		databaseRequest.addIntValue(2, session.getOperator().getOperatorID());
		databaseRequest.addIntValue(3, patientID);
		
		makeDatabaseRequest(databaseRequest);
	}

	public void makeDeceasedPatientRequest(ClientSession session, int patientID) {
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.DECEASED_PATIENT_REQUEST, StoredProcedure.DeceasedPatient);
		databaseRequest.addIntValue(2, session.getOperator().getOperatorID());
		databaseRequest.addIntValue(3, patientID);
		
		makeDatabaseRequest(databaseRequest);	
	}

	public void makeExaminationListRequest(ClientSession session) {
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.EXAMINATION_LIST_REQUEST, StoredProcedure.LoadExaminationList);
		databaseRequest.addIntValue(2, session.getOperator().getOperatorID());
		
		makeDatabaseRequest(databaseRequest);		
	}

	public void makeDiagnosisListRequest(ClientSession session) {
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.DIAGNOSIS_LIST_REQUEST, StoredProcedure.LoadDiagnosisList);
		databaseRequest.addIntValue(2, session.getOperator().getOperatorID());
		
		makeDatabaseRequest(databaseRequest);			
	}
	
	public void makePrescriptionListRequest(ClientSession session) {
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.PRESCRIPTION_LIST_REQUEST, StoredProcedure.LoadPrescriptionList);
		databaseRequest.addIntValue(2, session.getOperator().getOperatorID());
		
		makeDatabaseRequest(databaseRequest);			
	}

	public void makeMedicationListRequest(ClientSession session) {
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.MEDICATION_LIST_REQUEST, StoredProcedure.LoadMedicationList);
		databaseRequest.addIntValue(2, session.getOperator().getOperatorID());
		
		makeDatabaseRequest(databaseRequest);				
	}

	public void makeAddExaminationRequest(ClientSession session, Examination data, int pin) {
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.ADD_EXAMINATION_REQUEST, StoredProcedure.AddExamination);
		databaseRequest.addIntValue(2, session.getOperator().getOperatorID());
		databaseRequest.addIntValue(3, data.getPatientID());
		databaseRequest.addIntValue(4, pin);
		databaseRequest.addIntValue(5, data.getExaminationType().getID());
		
		makeDatabaseRequest(databaseRequest);				
	}
	
	public void makeAddDiagnosisRequest(ClientSession session, Diagnosis diagnosis, int pin) {
		DatabaseRequest databaseRequest = new DatabaseRequest(
				session, RequestCodes.ADD_DIAGNOSIS_REQUEST, StoredProcedure.AddDiagnosis);
		databaseRequest.addIntValue(2, session.getOperator().getOperatorID());
		databaseRequest.addIntValue(3, pin);
		databaseRequest.addIntValue(4, diagnosis.getDiagnosisID());
		databaseRequest.addIntValue(5, diagnosis.getExaminationID());
		databaseRequest.addStringValue(6, diagnosis.getObservations());
		databaseRequest.addIntValue(7, (diagnosis.isActive()) ? 1 : 0);
		
		makeDatabaseRequest(databaseRequest);					
	}
	
	public Connection getDatabaseConnection() {
		return databaseConnection;
	}

}