package medproject.medserver.databaseHandler;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import medproject.medlibrary.concurrency.Request;
import medproject.medserver.netHandler.ClientSession;

public class DatabaseRequest {

	private final PreparedStatement preparedStatement;
	private final ClientSession clientSession;
	private final boolean updatingRequest;
	private final Request request;
	
	private final Map<Integer,Integer> intValues = new HashMap<Integer, Integer>();
	private final Map<Integer,String> stringValues = new HashMap<Integer, String>();
	private final Map<Integer,Blob> blobValues = new HashMap<Integer, Blob>();
	
	private ResultSet resultSet;
	private int affectedRows = 0;
	
	public DatabaseRequest(ClientSession clientSession, Request request, PreparedStatement preparedStatement, boolean updatingRequest) {
		this.clientSession = clientSession;
		this.request = request;
		this.preparedStatement = preparedStatement;
		this.updatingRequest = updatingRequest;
	}
	
	public Request getRequest() {
		return request;
	}

	public boolean isUpdatingRequest() {
		return updatingRequest;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}

	public void setResultSet(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public Map<Integer, Integer> getIntValues() {
		return intValues;
	}

	public Map<Integer, String> getStringValues() {
		return stringValues;
	}

	public PreparedStatement getPreparedStatement() {
		return preparedStatement;
	}

	public int getAffectedRows() {
		return affectedRows;
	}

	public void setAffectedRows(int affectedRows) {
		this.affectedRows = affectedRows;
	}

	public Map<Integer, Blob> getBlobValues() {
		return blobValues;
	}

	public ClientSession getClientSession() {
		return clientSession;
	}
}
