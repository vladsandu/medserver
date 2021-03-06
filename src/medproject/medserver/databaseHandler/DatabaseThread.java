package medproject.medserver.databaseHandler;

import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import medproject.medlibrary.logging.LogWriter;
import medproject.medserver.requestHandler.RequestHandler;
import oracle.jdbc.internal.OracleTypes;
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
				
				if(currentRequest == null)
					return;

				processDatabaseRequest(currentRequest);
				requestHandler.addCompleteRequest(currentRequest);
			} catch (InterruptedException e) {
				LOG.severe("Request Handler thread interrupted" + e.getMessage());
			}

		}
	}

	private void processDatabaseRequest(DatabaseRequest currentRequest){
		try {
			CallableStatement statement = databaseConnection.prepareCall(currentRequest.getProcedure().getSQL());

			for(HashMap.Entry<Integer, String> parameter : currentRequest.getStringValues().entrySet()){
				statement.setString(parameter.getKey(), parameter.getValue());
			}
			for(HashMap.Entry<Integer, Integer> parameter : currentRequest.getIntValues().entrySet()){
				statement.setInt(parameter.getKey(), parameter.getValue());

			}
			for(Entry<Integer, Blob> parameter : currentRequest.getBlobValues().entrySet()){
				statement.setBlob(parameter.getKey(), parameter.getValue());
			}

			if(!currentRequest.getProcedure().isSelectionRequest()){
				statement.registerOutParameter(1, OracleTypes.NUMBER);
				statement.execute();
		       	currentRequest.setAffectedRows(statement.getInt(1));
			}
			else{
				statement.registerOutParameter(1, OracleTypes.CURSOR);
	            
				statement.execute();
				ResultSet results = (ResultSet) statement.getObject(1);
				
				currentRequest.setResultSet(results);
			}	
		} catch (SQLException e) {
			LOG.severe("Database couldn't process request" + e.getMessage());
			currentRequest.setAffectedRows(0);
			currentRequest.setResultSet(null);
		//TODO: Do a status
		}
	}


	public Connection getDatabaseConnection() {
		return databaseConnection;
	}

	public DatabaseRequestTemplate getDatabaseRequestTemplate() {
		return databaseRequestTemplate;
	}
}
