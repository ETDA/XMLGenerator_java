package controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogController {
	private Logger logger;
	
	public LogController() throws Exception {
		checkLogLocation();
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");	
		
		logger = Logger.getLogger("XMLGenerator");  
		logger.setUseParentHandlers(false);
		FileHandler fh = new FileHandler("log\\log_" + formatter.format(new Date()) + ".log");
		logger.addHandler(fh);
		
        fh.setFormatter(new SimpleFormatter() {
            private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(format,
                        new Date(lr.getMillis()),
                        lr.getLevel().getLocalizedName(),
                        lr.getMessage()
                );
            }
        });
	}
	
	private void checkLogLocation() {
		File directory = new File("Log");
	    if (! directory.exists()){
	        directory.mkdir();
	    }
	}
	
	public void writeInfoLog(String text) {
		logger.log(Level.INFO, text);
		writeConsole(text);
	}
	
	public void writeWarningLog(String text) {
		logger.log(Level.WARNING, text);
		writeConsole(text);
	}
	
	public void writeErrorLog(String text) {
		logger.log(Level.SEVERE, text);
		writeConsole(text);
	}
	
	public void writeConsole(String text) {
		System.out.println(LocalDate.now() + " - " + text);
	}

}
