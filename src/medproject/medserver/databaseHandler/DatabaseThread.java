package medproject.medserver.databaseHandler;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import medproject.medserver.logging.LogWriter;
import medproject.medserver.netHandler.ClientSession;
import medproject.medserver.requestHandler.RequestCodes;
import medproject.medserver.requestHandler.RequestHandler;
import oracle.jdbc.pool.OracleDataSource;

public class DatabaseThread implements Runnable{

	private final Logger LOG = LogWriter.getLogger(DatabaseThread.class.getName());

	private final OracleDataSource dataSource;
	private final Connection databaseConnection;

	private final LinkedBlockingQueue<DatabaseRequest> databaseRequests;

	private final DatabaseRequestTemplate databaseRequestTemplate;

	private final Thread databaseThread = new Thread(this);

	private final RequestHandler requestHandler;

	private volatile boolean shouldStop = false;

	public DatabaseThread(RequestHandler requestHandler, String address, String username, String password) throws SQLException{
		this.dataSource = new OracleDataSource();
		this.requestHandler = requestHandler;

		dataSource.setURL(address);
		dataSource.setUser(username);
		dataSource.setPassword(password);

		this.databaseConnection = dataSource.getConnection();
		this.databaseRequests = new LinkedBlockingQueue<DatabaseRequest>();
		this.databaseRequestTemplate = new DatabaseRequestTemplate(databaseConnection, databaseRequests);
	}

	public void start() {
		databaseThread.start();
		LOG.info("Database thread started");
	}

	public void stop() {   
		shouldStop = true;
	}

	@Override
	public void run() {
		while(!shouldStop) {

			DatabaseRequest currentRequest = null;

			try {
				currentRequest = databaseRequests.take();
				processDatabaseRequest(currentRequest);
				
				requestHandler.addCompleteRequest(currentRequest);
			} catch (InterruptedException e) {
				LOG.severe("Request Handler thread interrupted" + e.getMessage());
			} catch (SQLException e) {
				LOG.severe("Database couldn't process request" + e.getMessage());
			}

		}
	}

	private void processDatabaseRequest(DatabaseRequest currentRequest) throws SQLException{
		PreparedStatement statement = currentRequest.getPreparedStatement(); 	
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
	}


	public Connection getDatabaseConnection() {
		return databaseConnection;
	}

	public DatabaseRequestTemplate getDatabaseRequestTemplate() {
		return databaseRequestTemplate;
	}
}
