package io.boodskap.iot.ext.fs.bin.rules.api;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Properties;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUploadService implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(FileUploadService.class);

	private Server server;

	public FileUploadService(final Properties config) throws Exception {
		
		int port = Integer.valueOf(config.getProperty("micro_service_port", "19091"));
    	Config.get().setUploadDirectory(String.format("%s/out", config.getProperty("root_dir")));
    	Config.get().setMemoryThreshold(Integer.valueOf(config.getProperty("out_mem_threshold", String.valueOf(1024 * 1024 * 10)))); //10MB
    	Config.get().setMaxFileSize(Integer.valueOf(config.getProperty("out_max_file_size", String.valueOf(1024 * 1024 * 40)))); //40MB
    	Config.get().setMaxRequestSize(Integer.valueOf(config.getProperty("out_max_req_size", String.valueOf(1024 * 1024 * 50)))); //50MB
    	
    	
    	String outDirs = config.getProperty("out_folders", "");
    	String[] outFolders = outDirs.split(",");
    	for(String outFolder : outFolders) {
    		if(!outFolder.trim().equals("")) {
    			new File(String.format("%s/%s", Config.get().getUploadDirectory(), outFolder)).mkdirs();
    		}
    	}
    	
    	
		
		server = new Server(port);
        
        ServletHandler handler = new ServletHandler();
        
        handler.addFilterWithMapping(FilterTraceTrack.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        
        server.setHandler(handler);
        server.setDumpAfterStart(false);
        server.setDumpBeforeStop(false);
        
        
        handler.addServletWithMapping(FileUploadServlet.class, "/upload/*");
        
        LOG.info(String.format("Listening at 0.0.0.0:%d", port));

	}

	@Override
	public void run() {
		
		try {
			
			server.start();
			
			LOG.info("Started and running...");
			
	        server.join();
			
			LOG.info("stopped.");
			
		}catch(Exception ex) {
			LOG.error("Service terminated", ex);
		}
		
	}

	public void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			LOG.error("Error while stopping", e);
		}
	}
	
	public static class FilterTraceTrack implements Filter{

		@Override
		public void init(FilterConfig filterConfig) throws ServletException {
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
			
			HttpServletRequest req = (HttpServletRequest) request;
			HttpServletResponse res = (HttpServletResponse) response;
			final String method = req.getMethod().toUpperCase();
			
			switch(method) {
			case "TRACE":
			case "TRACK":
				LOG.warn(String.format("Rejecting %s method from:%s", method, request.getRemoteAddr()));
	            res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	            res.flushBuffer();
	            break;
			default:
				chain.doFilter(request, response);
				break;
			}
		}

		@Override
		public void destroy() {
		}
		
	}

}