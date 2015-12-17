package medproject.medserver.requestHandler;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import medproject.medlibrary.account.MedicalDegree;
import medproject.medlibrary.concurrency.Request;
import medproject.medlibrary.concurrency.RequestCodes;
import medproject.medlibrary.concurrency.RequestStatus;
import medproject.medlibrary.medication.Drug;
import medproject.medlibrary.medication.DrugPresentation;
import medproject.medlibrary.medication.DrugProducer;
import medproject.medlibrary.medication.DrugType;
import medproject.medlibrary.medication.Medication;
import medproject.medlibrary.medication.MedicationAdministrationType;
import medproject.medlibrary.medication.TreatmentScheme;
import medproject.medserver.databaseHandler.DatabaseRequestTemplate;
import medproject.medserver.databaseHandler.StoredProcedure;
import medproject.medserver.netHandler.ClientSession;
import oracle.jdbc.internal.OracleTypes;

public class MedicationHandler extends AbstractHandler{

	private final DatabaseRequestTemplate databaseRequestTemplate;

	private final List<Drug> drugList;

	public MedicationHandler(DatabaseRequestTemplate databaseRequestTemplate) {
		this.databaseRequestTemplate = databaseRequestTemplate;
		this.drugList = new ArrayList<Drug>();
		initializeDrugList();
	}

	public void handleRequest(ClientSession session, Request request) throws SQLException{
		switch(request.getREQUEST_CODE()){
		case RequestCodes.DRUG_LIST_REQUEST:
			handleDrugListRequest(session, request);	break;
		case RequestCodes.MEDICATION_LIST_REQUEST:
			handleMedicationListRequest(session, request); break;
		case RequestCodes.ADD_MEDICATION_REQUEST:
			handleAddMedicationRequest(session, request); break;
		case RequestCodes.DELETE_MEDICATION_REQUEST:
			handleDeleteMedicationRequest(session, request); break;
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

				DrugType drugType = new DrugType(
						results.getBoolean("fractionabil"), 
						results.getBoolean("compensat"), 
						results.getBoolean("psihotrop"));
				DrugProducer drugProducer = new DrugProducer(
						results.getString("denumire_producator"), 
						results.getString("tara"));

				Drug drug = new Drug(
						results.getInt("id"), 
						results.getString("denumire"),
						results.getString("denumire_substanta"), 
						results.getString("concentratie"), 
						DrugPresentation.getDrugPresentationByID(results.getInt("prezentare")),
						drugType, 
						results.getDouble("pret_referinta"), 
						MedicalDegree.getMedicalDegreeByID(results.getInt("grad_necesar")), 
						drugProducer,
						results.getString("atentionare")
						);

				drugList.add(drug);
			}

			if(drugList.size() > 0)
				LOG.info("Drug list loaded");
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

	private void handleDeleteMedicationRequest(ClientSession session, Request request) {
		if(verifyNullRequestData(request))
			return;

		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeDeleteMedicationRequest(session, (int) request.getDATA());
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			int affectedRows = (int)request.getDATA();

			if(affectedRows == 0){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("The medication couldn't be deleted.");
			}
			else{
				request.setStatus(RequestStatus.REQUEST_COMPLETED);
				request.setMessage("The medication was deleted.");
			}
		}

	}

	private void handleAddMedicationRequest(ClientSession session, Request request) throws SQLException{
		if(verifyNullRequestData(request))
			return;

		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeaddOrUpdateMedicationRequest(session, (Medication) request.getDATA(), request.getPIN());
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			ResultSet results = (ResultSet) request.getDATA();

			request.setDATA(null);

			if(results.next()){
				Medication medication = getMedicationFromResultSet(results);

				request.setDATA(medication);
				request.setMessage("Medication added successfully");
				request.setStatus(RequestStatus.REQUEST_COMPLETED);
			}
			else{
				throw new SQLException();
			}

		}
	}

	private void handleMedicationListRequest(ClientSession session, Request request) throws SQLException {
		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeMedicationListRequest(session);
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){			
			if(verifyNullRequestData(request))
				return;

			ResultSet results = (ResultSet) request.getDATA();

			List<Medication> medicationList = new ArrayList<Medication>();
			request.setDATA(medicationList);

			while(results.next()){
				Medication medication = getMedicationFromResultSet(results);
				medicationList.add(medication);
			}

			if(medicationList.size() > 0){
				request.setMessage("Medication selection successful");
			}
			else{
				request.setMessage("No medication found.");
			}

			request.setStatus(RequestStatus.REQUEST_COMPLETED);
		}
	}
	
	private Medication getMedicationFromResultSet(ResultSet results) throws SQLException{
		Medication medication = new Medication(
				results.getInt("id"),
				results.getInt("reteta_id"),
				results.getInt("diagnostic_id"),
				results.getInt("medicament_id"), 
				results.getInt("cantitate"),
				results.getInt("nr_zile"),
				MedicationAdministrationType.getTypeByID(results.getInt("mod_administrare")), 
				TreatmentScheme.getTypeByID(results.getInt("schema_tratament")),
				results.getString("observatii"));

		return medication;
	}

}
