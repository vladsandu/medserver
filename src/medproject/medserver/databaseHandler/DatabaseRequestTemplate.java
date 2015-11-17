package medproject.medserver.databaseHandler;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import medproject.medlibrary.account.Account;
import medproject.medserver.netHandler.ClientSession;
import medproject.medserver.requestHandler.RequestCodes;
import medproject.medserver.utils.UtilMethods;

public class DatabaseRequestTemplate {

	private final Connection databaseConnection;
	private final ConcurrentHashMap<ClientSession, ArrayList<DatabaseRequest>> databaseRequests;
	private final ConcurrentHashMap<Integer, ArrayList<DatabaseRequest>> serverDatabaseRequests;
	
		
	public DatabaseRequestTemplate(Connection databaseConnection, 
			ConcurrentHashMap<ClientSession, ArrayList<DatabaseRequest>> databaseRequests, 
			ConcurrentHashMap<Integer, ArrayList<DatabaseRequest>> serverDatabaseRequests) {
		this.databaseConnection = databaseConnection;
		this.databaseRequests = databaseRequests;
		this.serverDatabaseRequests = serverDatabaseRequests;
		
		createPreparedStatements();
	}
	
	private void createPreparedStatements(){

	}
	
	public void makeDatabaseRequest(ClientSession client, DatabaseRequest currentRequest){
		synchronized(databaseRequests){
			if(databaseRequests.containsKey(client) && databaseRequests.get(client) != null)
				databaseRequests.get(client).add(currentRequest);
			else{
			ArrayList<DatabaseRequest> requestList = new ArrayList<DatabaseRequest>();
			requestList.add(currentRequest);
			
			databaseRequests.put(client, requestList);
			}
		}
	}
	
	public void makeServerDatabaseRequest(Integer client, DatabaseRequest currentRequest){
		synchronized(serverDatabaseRequests){
			if(serverDatabaseRequests.containsKey(client) && serverDatabaseRequests.get(client) != null)
				serverDatabaseRequests.get(client).add(currentRequest);
			else{
			ArrayList<DatabaseRequest> requestList = new ArrayList<DatabaseRequest>();
			requestList.add(currentRequest);
			
			serverDatabaseRequests.put(client, requestList);
			}
		}
	}
}
