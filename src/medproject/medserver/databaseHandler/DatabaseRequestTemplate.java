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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import medproject.medlibrary.account.Account;
import medproject.medserver.netHandler.ClientSession;
import medproject.medserver.requestHandler.RequestCodes;
import medproject.medserver.utils.UtilMethods;

public class DatabaseRequestTemplate {

	private final Connection databaseConnection;
	private final LinkedBlockingQueue<DatabaseRequest> databaseRequests;

	public DatabaseRequestTemplate(Connection databaseConnection, 
			LinkedBlockingQueue<DatabaseRequest> databaseRequests2) {
		this.databaseConnection = databaseConnection;
		this.databaseRequests = databaseRequests2;

		createPreparedStatements();
	}

	private void createPreparedStatements(){

	}

	public void makeDatabaseRequest(ClientSession client, DatabaseRequest currentRequest){
		synchronized(databaseRequests){
		}
	}
}
