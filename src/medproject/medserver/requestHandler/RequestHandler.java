package medproject.medserver.requestHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import medproject.medlibrary.concurrency.Request;
import medproject.medlibrary.concurrency.RequestCodes;
import medproject.medlibrary.concurrency.RequestStatus;
import medproject.medlibrary.logging.LogWriter;
import medproject.medserver.dataWriter.DataWriter;
import medproject.medserver.databaseHandler.DatabaseRequest;
import medproject.medserver.databaseHandler.DatabaseThread;
import medproject.medserver.netHandler.ClientSession;

public class RequestHandler implements Runnable{

	private final Logger LOG = LogWriter.getLogger("RequestHandler");

	private final LinkedBlockingQueue<RequestEntry> requestQueue = new LinkedBlockingQueue<RequestEntry>();
	private final DatabaseThread databaseThread;

	private final DataWriter dataWriter;

	private final Thread t;
	private volatile boolean shouldStop = false;
	//TODO : constants
	private int bytesForMessageSize = 8;

	private final LoginHandler loginHandler;
	private final PatientHandler patientHandler;
	private final ExaminationHandler examinationHandler;
	private final DiagnosisHandler diagnosisHandler;
	private final PrescriptionHandler prescriptionHandler;
	private final MedicationHandler medicationHandler;

	public RequestHandler(DataWriter dataWriter) throws SQLException {
		this.dataWriter = dataWriter;
		this.databaseThread = new DatabaseThread(this, "jdbc:oracle:thin:@localhost:1521/pdbmed", "medadmin", "test");

		loginHandler = new LoginHandler(databaseThread.getDatabaseRequestTemplate());
		patientHandler = new PatientHandler(databaseThread.getDatabaseRequestTemplate());
		examinationHandler = new ExaminationHandler(databaseThread.getDatabaseRequestTemplate());
		diagnosisHandler = new DiagnosisHandler(databaseThread.getDatabaseRequestTemplate());
		prescriptionHandler = new PrescriptionHandler(databaseThread.getDatabaseRequestTemplate());
		medicationHandler = new MedicationHandler(databaseThread.getDatabaseRequestTemplate());

		this.t = new Thread(this);
	}

	public void start() {
		databaseThread.start();
		t.start();
		LOG.info("Request handler thread started");
	}

	public void stop() {   
		shouldStop = true;
		databaseThread.stop();
	}

	public void processNewRequest(ClientSession client) throws IOException, ClassNotFoundException {

		Request currentRequest = null;

		ByteBuffer clientBuffer = client.getReadBuffer();

		clientBuffer.clear();
		byte[] packetBytes = new byte[client.getCurrentMessageByteSize()];
		clientBuffer.position(bytesForMessageSize);

		clientBuffer.get(packetBytes,0,packetBytes.length);		

		ByteArrayInputStream inputStream = new ByteArrayInputStream(packetBytes);
		ObjectInputStream objectStream = new ObjectInputStream(inputStream);

		currentRequest = (Request) objectStream.readObject();

		objectStream.close();

		clientBuffer.clear();
		LOG.info("Request deserialized");

		synchronized(this.requestQueue) {
			if(currentRequest != null){
				requestQueue.offer(new RequestEntry(currentRequest, client));
			}
		}
	}

	@Override
	public void run() {
		//TODO: implement shouldStop variables in all projects
		while(!shouldStop) {
			RequestEntry requestEntry = null;
			try {
				requestEntry = requestQueue.take();
				
				try {
					sendRequestToSpecializedHandler(requestEntry.getClientSession(), requestEntry.getRequest());
				} catch (SQLException e) {
					makeRequestFailed(e, requestEntry.getRequest());
				}
				
				if(requestEntry.getRequest().isCompleted())
					dataWriter.processWriteRequest(
							requestEntry.getClientSession(), 
							requestEntry.getRequest());	

			} catch (InterruptedException e) {
				LOG.severe("Request Handler thread interrupted");
			}			  	 
		}
	}
	
	private void makeRequestFailed(SQLException e, Request request){
		LOG.severe("Error: " + e.getMessage());
		request.setDATA(null);
		request.setStatus(RequestStatus.REQUEST_FAILED);
		request.setMessage("Database Exception Error: " + e.getMessage());

	}

	private void sendRequestToSpecializedHandler(ClientSession client, Request request) throws SQLException{
		switch(RequestCodes.getRequestType(request)){
		case RequestCodes.LOGIN_TYPE_REQUEST:
			loginHandler.handleRequest(client, request);			break;
		case RequestCodes.PATIENT_TYPE_REQUEST:
			patientHandler.handleRequest(client, request);			break;
		case RequestCodes.EXAMINATION_TYPE_REQUEST:
			examinationHandler.handleRequest(client, request);		break;
		case RequestCodes.DIAGNOSIS_TYPE_REQUEST:
			diagnosisHandler.handleRequest(client, request);		break;
		case RequestCodes.PRESCRIPTION_TYPE_REQUEST:
			prescriptionHandler.handleRequest(client, request);		break;
		case RequestCodes.MEDICATION_TYPE_REQUEST:
			medicationHandler.handleRequest(client, request);		break;
		}
	}

	public void addCompleteRequest(DatabaseRequest databaseRequest){
		Request request = new Request(databaseRequest.getRequestCode(), databaseRequest.getResultSet());
		request.setStatus(RequestStatus.REQUEST_PENDING);

		if(!databaseRequest.getProcedure().isSelectionRequest())
			request.setDATA(databaseRequest.getAffectedRows());

		synchronized(this.requestQueue) {
			requestQueue.offer(new RequestEntry(request, databaseRequest.getClientSession()));
		}
	}

}

