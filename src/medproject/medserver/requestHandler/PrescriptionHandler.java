package medproject.medserver.requestHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import medproject.medlibrary.concurrency.Request;
import medproject.medlibrary.concurrency.RequestCodes;
import medproject.medlibrary.concurrency.RequestStatus;
import medproject.medlibrary.examination.Examination;
import medproject.medlibrary.examination.ExaminationType;
import medproject.medlibrary.logging.LogWriter;
import medproject.medlibrary.prescription.Prescription;
import medproject.medlibrary.prescription.PrescriptionStatus;
import medproject.medlibrary.prescription.PrescriptionType;
import medproject.medserver.databaseHandler.DatabaseRequestTemplate;
import medproject.medserver.netHandler.ClientSession;

public class PrescriptionHandler extends AbstractHandler{

	private final DatabaseRequestTemplate databaseRequestTemplate;

	public PrescriptionHandler(DatabaseRequestTemplate databaseRequestTemplate) {
		this.databaseRequestTemplate = databaseRequestTemplate;
	}

	public void handleRequest(ClientSession session, Request request) throws SQLException{
		switch(request.getREQUEST_CODE()){
		case RequestCodes.PRESCRIPTION_LIST_REQUEST:
			handlePrescriptionListRequest(session, request); break;
		case RequestCodes.ADD_PRESCRIPTION_REQUEST:
			handleAddPrescriptionRequest(session, request); break;
		case RequestCodes.CANCEL_PRESCRIPTION_REQUEST:
			handleCancelPrescriptionRequest(session, request); break;
		}
	}


	private void handleCancelPrescriptionRequest(ClientSession session, Request request) throws SQLException{
		if(verifyNullRequestData(request))	return;

		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeCancelPrescriptionRequest(session, (int) request.getDATA(), request.getPIN());
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){

			ResultSet results = (ResultSet) request.getDATA();
			request.setDATA(null);
			if(results.next()){

				Prescription prescription = new Prescription(
						results.getInt("id"), 
						results.getInt("consultatie_id"), 
						results.getInt("numar_zile"), 
						PrescriptionType.getTypeByID(results.getInt("tip_reteta")), 
						PrescriptionStatus.getStatusByID(results.getInt("stare_reteta")));

				request.setDATA(prescription);
				request.setMessage("Prescription canceled successfully.");
				request.setStatus(RequestStatus.REQUEST_COMPLETED);
			}
			else
				throw new SQLException("Couldn't cancel prescription.");	
		}

	}

	private void handleAddPrescriptionRequest(ClientSession session, Request request) throws SQLException{
		if(verifyNullRequestData(request))	return;

		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makeaddPrescriptionRequest(session, (Prescription) request.getDATA(), request.getPIN());
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			ResultSet results = (ResultSet) request.getDATA();
			request.setDATA(null);

			if(results.next()){

				Prescription prescription = new Prescription(
						results.getInt("id"), 
						results.getInt("consultatie_id"), 
						results.getInt("numar_zile"), 
						PrescriptionType.getTypeByID(results.getInt("tip_reteta")), 
						PrescriptionStatus.getStatusByID(results.getInt("stare_reteta")));

				request.setDATA(prescription);
				request.setMessage("Prescription created successfully.");
				request.setStatus(RequestStatus.REQUEST_COMPLETED);
			}
			else
				throw new SQLException("Couldn't add prescription.");	
		}

	}

	private void handlePrescriptionListRequest(ClientSession session, Request request) throws SQLException {
		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makePrescriptionListRequest(session);
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			if(verifyNullRequestData(request))	return;

			ResultSet results = (ResultSet) request.getDATA();

			List<Prescription> prescriptionList = new ArrayList<Prescription>();
			request.setDATA(prescriptionList);

			while(results.next()){

				Prescription prescription = new Prescription(
						results.getInt("id"), 
						results.getInt("consultatie_id"), 
						results.getInt("numar_zile"), 
						PrescriptionType.getTypeByID(results.getInt("tip_reteta")), 
						PrescriptionStatus.getStatusByID(results.getInt("stare_reteta")));

				prescriptionList.add(prescription);
			}

			if(prescriptionList.size() > 0){
				request.setMessage("Prescription selection successful");
			}
			else{
				request.setMessage("No prescriptions found.");
			}

			request.setStatus(RequestStatus.REQUEST_COMPLETED);
		}
	}
}