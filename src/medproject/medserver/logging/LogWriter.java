package medproject.medserver.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogWriter {

	private static FileHandler fileHandler = null;
	
	public static Logger getLogger(String name){
		Logger logger = Logger.getLogger(name);
		
		if(fileHandler != null){
			if(logger.getHandlers().length == 0)
				logger.addHandler(fileHandler);
		}
		
		return logger;
	}
	
	public static void useFileLogging(){
		try {
			fileHandler = new FileHandler("server_log");
			SimpleFormatter formatter = new SimpleFormatter();  
			fileHandler.setFormatter(formatter);  
		} catch (SecurityException | IOException e) {
			Logger.getAnonymousLogger().severe("Could not initialize log file.");
		}
	}
}
