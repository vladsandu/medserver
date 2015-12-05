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

public class PrescriptionHandler {

	private final Logger LOG = LogWriter.getLogger(this.getClass().getName());

	private final DatabaseRequestTemplate databaseRequestTemplate;

	public PrescriptionHandler(DatabaseRequestTemplate databaseRequestTemplate) {
		this.databaseRequestTemplate = databaseRequestTemplate;
	}

	public void handleRequest(ClientSession session, Request request){
		switch(request.getREQUEST_CODE()){
		case RequestCodes.PRESCRIPTION_LIST_REQUEST:
			handlePrescriptionListRequest(session, request); break;
		}
	}

	private void handlePrescriptionListRequest(ClientSession session, Request request) {
		if(request.getStatus() == RequestStatus.REQUEST_NEW){
			databaseRequestTemplate.makePrescriptionListRequest(session);
		}
		else if(request.getStatus() == RequestStatus.REQUEST_PENDING){
			if(request.getDATA() == null){
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("The database couldn't process the request");
				return;
			}

			ResultSet results = (ResultSet) request.getDATA();

			List<Prescription> prescriptionList = new ArrayList<Prescription>();
			request.setDATA(prescriptionList);
			try {	
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

			} catch (SQLException e) {
				LOG.severe("PrescriptionList Error: " + e.getMessage());
				request.setStatus(RequestStatus.REQUEST_FAILED);
				request.setMessage("Database Exception Error");
			}
		}
	}

}
