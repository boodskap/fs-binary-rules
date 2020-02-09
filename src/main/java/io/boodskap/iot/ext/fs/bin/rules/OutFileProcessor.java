package io.boodskap.iot.ext.fs.bin.rules;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutFileProcessor implements Runnable {
	
	private static final Logger LOG = LoggerFactory.getLogger(OutFileProcessor.class);
	
	private final long process_delay;
	private final boolean delete_after_processing;
	
	protected OutFileProcessor(Properties config) {
		process_delay = Long.valueOf(config.getProperty("process_delay", "10")) * 1000;
		delete_after_processing = Boolean.valueOf(config.getProperty("delete_after_processing", "true"));
	}

	@Override
	public void run() {
		
		boolean terminated = false;
		LOG.info("Starting and running...");
		
		try {
			
			while(!Thread.currentThread().isInterrupted()) {
				
				QueuedFile file = InOutQueue.getOutq().take();
				
				if(null != file) {
					
					try {
						
						String oldMd5 = file.computeMD5();
						
						LOG.info(String.format("Delaying %d seconds before processing file:%s", process_delay/1000, file.getFile()));

						Thread.sleep(process_delay);
						
						String newMd5 = file.computeMD5();
						
						if(!oldMd5.equals(newMd5)) {
							LOG.warn(String.format("File %s is still being uploaded, waiting...", file.getFile()));
							InOutQueue.getOutq().offer(file);
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

	private void process(QueuedFile file) throws Exception {
		
		Map<String, Object> map = new HashMap<>();
		map.put("file", new File(file.getFile()).getName());
		
		try {
			
			File osFile = new File(file.getFile());
			
			LOG.info(String.format("Processing file:%s", file.getFile()));
			
			map.put("status", "uploading");
			FileTransfer.getInClient().status(file.getRule(), map);
			
			FileTransfer.getOutClient().put(file.getRule(), osFile);

			map.put("status", "finished");
			
			if(delete_after_processing) {
				
				osFile.delete();
				
				if(osFile.exists()) {
					LOG.error(String.format("Unable to delete file:%s", file.getFile()));
				}else {
					LOG.info(String.format("Deleted processed file:%s", file.getFile()));
				}
			}
			
		}catch(Exception ex) {
			map.put("status", "failed");
			map.put("error", ExceptionUtils.getRootCauseMessage(ex));
			LOG.error("Processing Failed", ex);
		}finally {
			FileTransfer.getInClient().status(file.getRule(), map);
		}
		
	}

}
