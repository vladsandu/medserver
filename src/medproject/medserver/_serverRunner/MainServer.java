package medproject.medserver._serverRunner;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.util.HashMap;

import medproject.medserver.databaseHandler.DatabaseThread;
import medproject.medserver.logging.LogWriter;
import medproject.medserver.netHandler.ClientSession;
import medproject.medserver.netHandler.NetServerThread;

public class MainServer {

	public static HashMap<SelectionKey, ClientSession> clientMap = new HashMap<SelectionKey, ClientSession>();
	 
	public static void main(String[] args) throws Throwable {
		LogWriter.useFileLogging();
		LogWriter.setDebugMode(true);
		
		//load configuration from config file
		NetServerThread netServerThread = new NetServerThread(new InetSocketAddress("localhost", 1338));
		
		netServerThread.start();
	}

	public static HashMap<SelectionKey, ClientSession> getClientMap() {
		return clientMap;
	}
	
}