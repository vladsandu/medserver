package medproject.medserver.databaseHandler;

import java.sql.Blob;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import medproject.medserver.netHandler.ClientSession;

public class DatabaseRequest {

	private final StoredProcedure procedure;
	private final ClientSession clientSession;
	private final int requestCode;
	

	private final Map<Integer,Integer> intValues = new HashMap<Integer, Integer>();
	private final Map<Integer,String> stringValues = new HashMap<Integer, String>();
	private final Map<Integer,Blob> blobValues = new HashMap<Integer, Blob>();
	
	private ResultSet resultSet = null;
	private int affectedRows = 0;
	
	public DatabaseRequest(ClientSession clientSession, int requestCode, StoredProcedure procedure) {
		this.clientSession = clientSession;
		this.requestCode = requestCode;
		this.procedure = procedure;
		
	}
	
	
	public int getRequestCode() {
		return requestCode;
	}

	public boolean isUpdatingRequest() {
		return procedure.isUpdatingRequest();
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	
	public void addIntValue(int position, int value){
		intValues.put(position, value);
	}
	
	public void addStringValue(int position, String value){
		stringValues.put(position, value);
	}
	
	public void addBlobValue(int position, Blob value){
		blobValues.put(position, value);
	}
	
	public Map<Integer, Integer> getIntValues() {
		return intValues;
	}

	public Map<Integer, String> getStringValues() {
		return stringValues;
	}

	public Map<Integer, Blob> getBlobValues() {
		return blobValues;
	}

	
	public StoredProcedure getProcedure() {
		return procedure;
	}

	public int getAffectedRows() {
		return affectedRows;
	}

	public void setAffectedRows(int affectedRows) {
		this.affectedRows = affectedRows;
	}

	
	public ClientSession getClientSession() {
		return clientSession;
	}
}
