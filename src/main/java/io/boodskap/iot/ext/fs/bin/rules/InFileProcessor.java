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
		process_delay = Long.valueOf(config.getProperty("process_delay", "10")) * 1000;
		delete_after_processing = Boolean.valueOf(config.getProperty("delete_after_processing", "true"));
	}

	@Override
	public void run() {
		
		boolean terminated = false;
		LOG.info("Starting and running...");
		
		try {
			
			while(!Thread.currentThread().isInterrupted()) {
				
				QueuedFile file = InOutQueue.getInq().take();
				
				if(null != file) {
					
					try {
						
						String oldMd5 = file.computeMD5();
						
						LOG.info(String.format("Delaying %d seconds before processing file:%s", process_delay/1000, file.getFile()));

						Thread.sleep(process_delay);
						
						String newMd5 = file.computeMD5();
						
						if(!oldMd5.equals(newMd5)) {
							LOG.warn(String.format("File %s is still being uploaded, waiting...", file.getFile()));
							InOutQueue.getInq().offer(file);
							Thread.sleep(process_delay);
						}else {
							process(file);
						}
						
					}catch(Exception ex) {
						LOG.info("Processing failed", ex);
					}
					
				}
				
			}
			
		}catch(Exception ex) {
			if(!(ex instanceof InterruptedException)) {
				terminated = true;
				LOG.error("Un-Handled", ex);
			}
		}
		
		if(!terminated) {
			LOG.warn("Stopped.");
		}else {
			LOG.error("Terminated.");
		}
		
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
