package medproject.medserver.databaseHandler;

public enum StoredProcedure {
	OperatorLogin("{call operatorLogin(?,?,?,?)}", true),
	LoadPatientList("{call loadpatientlist(?,?)}", true),
	PatientRecordByCNP("{call patientRecordByCNP(?,?)}", true),
	AddPatient("{call addPatient(?,?,?,?)}", true),
	UpdatePatientAddress("{call updatePatientAddress(?,?,?,?,?,?)}", false), 
	DeletePatient("{call deletePatient(?,?,?)}", false), 
	UnregisterPatient("{call unregisterPatient(?,?,?)}", true),
	RegisterPatient("{call registerPatient(?,?,?)}", true),
	DeceasedPatient("{call deceasedPatient(?,?,?)}", true), 
	LoadExaminationList("{call loadExaminationList(?,?)}", true), 
	LoadDiagnosisList("{call loadDiagnosisList(?,?)}", true), 
	LoadPrescriptionList("{call loadPrescriptionList(?,?)}", true), 
	LoadMedicationList("{call loadMedicationList(?,?)}", true), 
	LoadDiagnosisInfoList("{call loadDiagnosisInfoList(?)}", true), 
	LoadDrugList("{call loadDrugList(?)}", true), 
	AddExamination("{call addExamination(?,?,?,?,?)}", true);

	private StoredProcedure(String sql, boolean selectionRequest) {
		this.sql = sql;
		this.selectionRequest = selectionRequest;
	}
	
	private final String sql;
	private final boolean selectionRequest;
	
	public boolean isSelectionRequest() {
		return selectionRequest;
	}
	
	public String getSQL(){
		return sql;
	}
	
}
