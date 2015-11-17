package medproject.medserver.databaseHandler;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class DatabaseRequest {

	private final PreparedStatement preparedStatement;
	private final boolean updatingRequest;
	private final boolean notificationRequest;
	private int requestStatus;
	private final int requestType;
	
	private final Map<Integer,Integer> intValues = new HashMap<Integer, Integer>();
	private final Map<Integer,String> stringValues = new HashMap<Integer, String>();
	private final Map<Integer,Blob> blobValues = new HashMap<Integer, Blob>();
	
	private ResultSet resultSet;
	private int affectedRows = 0;
	
	public DatabaseRequest(PreparedStatement preparedStatement, boolean updatingRequest, boolean notificationRequest, int requestType, int requestStatus) {
		this.preparedStatement = preparedStatement;
		this.updatingRequest = updatingRequest;
		this.requestStatus = requestStatus;
		this.notificationRequest = notificationRequest;
		this.requestType = requestType;
	}

	public int getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(int requestStatus) {
		this.requestStatus = requestStatus;
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

	public boolean isNotificationRequest() {
		return notificationRequest;
	}

	public int getRequestType() {
		return requestType;
	}

}
