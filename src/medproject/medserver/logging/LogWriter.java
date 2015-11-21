package medproject.medserver.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogWriter {

	private static FileHandler fileHandler = null;
	private static boolean debugMode = false;
	
	public static Logger getLogger(String name){
		Logger logger = Logger.getLogger(name);
		if(fileHandler != null){
			if(logger.getHandlers().length == 0)
				logger.addHandler(fileHandler);
		}
		if(debugMode)
			logger.setLevel(Level.ALL);
		else
			logger.setLevel(Level.SEVERE);
		
		return logger;
	}
	
	public static void setDebugMode(boolean value){
		debugMode = value;
	}
	
	public static void useFileLogging(){
		try {
			fileHandler = new FileHandler("medserver_log");
			SimpleFormatter formatter = new SimpleFormatter();  
			fileHandler.setFormatter(formatter);  
		} catch (SecurityException | IOException e) {
			Logger.getAnonymousLogger().severe("Could not initialize log file.");
		}
	}
}
