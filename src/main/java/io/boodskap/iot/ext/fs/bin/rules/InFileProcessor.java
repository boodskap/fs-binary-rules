package io.boodskap.iot.ext.fs.bin.rules;

import java.io.File;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InFileProcessor implements Runnable {
	
	private static final Logger LOG = LoggerFactory.getLogger(InFileProcessor.class);
	
	private final long process_delay;
	private final boolean delete_after_processing;
	
	protected InFileProcessor(Properties config) {
		process_delay = Long.valueOf(config.getProperty("process_delay", "180")) * 1000;
		delete_after_processing = Boolean.valueOf(config.getProperty("delete_after_processing", "true"));
	}

	@Override
	public void run() {
		
		LOG.info("Starting and running...");
		
		while(!Thread.currentThread().isInterrupted()) {
			
			QueuedFile file;
			
			try {
				LOG.info("Waiting for incoming file...");
				file = InOutQueue.getInq().take();
			} catch (InterruptedException e1) {
				break;
			}
			
			if(null != file) {
				
				long delay = System.currentTimeMillis() - file.getCreatedStamp();
				
				if(delay < process_delay) {
					
					delay = (process_delay - delay);
					
					LOG.info(String.format("Delaying %d millis before processing file:%s", delay, file.getFile()));
					
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						break;
					}
				}
				
				process(file);
			}
			
		}
		
		LOG.warn("Stopped.");
		
	}

	private void process(QueuedFile file) {
		
		try {
			
			File osFile = new File(file.getFile());
			
			LOG.info(String.format("Processing file:%s", file.getFile()));
			
			FileTransfer.getInClient().put(file.getRule(), osFile);

			if(delete_after_processing) {
				
				osFile.delete();
				
				if(osFile.exists()) {
					LOG.error(String.format("Unable to delete file:%s", file.getFile()));
				}else {
					LOG.info(String.format("Deleted processed file:%s", file.getFile()));
				}
			}
			
		}catch(Exception ex) {
			LOG.error("Processing Failed", ex);
		}
		
	}

}
