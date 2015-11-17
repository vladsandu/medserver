package medproject.medserver.databaseHandler;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import medproject.medserver.logging.LogWriter;
import medproject.medserver.netHandler.ClientSession;
import medproject.medserver.requestHandler.RequestCodes;
import oracle.jdbc.pool.OracleDataSource;

public class DatabaseThread implements Runnable{

	private final Logger logger = LogWriter.getLogger(DatabaseThread.class.getName());
	private final OracleDataSource dataSource;
	private final Connection databaseConnection;
	private final ConcurrentHashMap<ClientSession, ArrayList<DatabaseRequest>> databaseRequests;
	private final ConcurrentHashMap<Integer, ArrayList<DatabaseRequest>> serverDatabaseRequests;
	
	private final DatabaseRequestTemplate databaseRequestTemplate;
	
	private final Thread databaseThread = new Thread(this);
	private volatile boolean shouldStop = false;

	public DatabaseThread(String address, String username, String password) throws SQLException{
		this.dataSource = new OracleDataSource();
		dataSource.setURL(address);
		dataSource.setUser(username);
		dataSource.setPassword(password);
		
		this.databaseConnection = dataSource.getConnection();
		this.databaseRequests  = new ConcurrentHashMap<ClientSession, ArrayList<DatabaseRequest>>();	
		this.serverDatabaseRequests  = new ConcurrentHashMap<Integer, ArrayList<DatabaseRequest>>();	
		this.databaseRequestTemplate = new DatabaseRequestTemplate(databaseConnection, databaseRequests, serverDatabaseRequests);
		this.start();    
	}

	public void start() {
		databaseThread.start();
		logger.info("Database thread started");
	}

	public void stop() {   
	    shouldStop = true;
	}

	@Override
	public void run() {
		while(!shouldStop) {
			synchronized(this.databaseRequests){
				for(ConcurrentHashMap.Entry<ClientSession, ArrayList<DatabaseRequest>> requestSet : databaseRequests.entrySet()){
			  	 	for(DatabaseRequest currentRequest : requestSet.getValue()){
						if(currentRequest.getRequestStatus() == RequestCodes.REQUEST_PENDING){
							PreparedStatement statement = currentRequest.getPreparedStatement(); 	
							try {
								statement.clearParameters();
									
								for(HashMap.Entry<Integer, String> parameter : currentRequest.getStringValues().entrySet()){
									statement.setString(parameter.getKey(), parameter.getValue());
								}
								for(HashMap.Entry<Integer, Integer> parameter : currentRequest.getIntValues().entrySet()){
									statement.setInt(parameter.getKey(), parameter.getValue());
								}
								for(Entry<Integer, Blob> parameter : currentRequest.getBlobValues().entrySet()){
									statement.setBlob(parameter.getKey(), parameter.getValue());
								}
						  	 	
								if(currentRequest.isUpdatingRequest()){
									int affectedRows = statement.executeUpdate();
									currentRequest.setAffectedRows(affectedRows);
								}
								else{
									ResultSet results = statement.executeQuery();
									currentRequest.setResultSet(results);
								}
								currentRequest.setRequestStatus(RequestCodes.REQUEST_COMPLETE);
								
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					
			  	 	}
				}
			}
			
			
			synchronized(this.serverDatabaseRequests){
				for(ConcurrentHashMap.Entry<Integer, ArrayList<DatabaseRequest>> requestSet : serverDatabaseRequests.entrySet()){
			  	 	for(DatabaseRequest currentRequest : requestSet.getValue()){
						if(currentRequest.getRequestStatus() == RequestCodes.REQUEST_PENDING){
							PreparedStatement statement = currentRequest.getPreparedStatement(); 	
							try {
								statement.clearParameters();
									
								for(HashMap.Entry<Integer, String> parameter : currentRequest.getStringValues().entrySet()){
									statement.setString(parameter.getKey(), parameter.getValue());
								}
								for(HashMap.Entry<Integer, Integer> parameter : currentRequest.getIntValues().entrySet()){
									statement.setInt(parameter.getKey(), parameter.getValue());
								}
								for(Entry<Integer, Blob> parameter : currentRequest.getBlobValues().entrySet()){
									statement.setBlob(parameter.getKey(), parameter.getValue());
								}
						  	 	
								if(currentRequest.isUpdatingRequest()){
									int affectedRows = statement.executeUpdate();
									currentRequest.setAffectedRows(affectedRows);
								}
								else{
									ResultSet results = statement.executeQuery();
									currentRequest.setResultSet(results);
								}
								currentRequest.setRequestStatus(RequestCodes.REQUEST_COMPLETE);
							
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					
			  	 	}
				}
			
			}
		}
	}

	public Connection getDatabaseConnection() {
		return databaseConnection;
	}

	public ConcurrentHashMap<ClientSession, ArrayList<DatabaseRequest>> getDatabaseRequests() {
		return databaseRequests;
	}

	public DatabaseRequestTemplate getDatabaseRequestTemplate() {
		return databaseRequestTemplate;
	}

	public ConcurrentHashMap<Integer, ArrayList<DatabaseRequest>> getServerDatabaseRequests() {
		return serverDatabaseRequests;
	}
	
}
