package io.boodskap.iot.ext.fs.bin.rules.api;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class FileUploadServlet extends HttpServlet {
	
    private static final Logger LOG = LoggerFactory.getLogger(FileUploadServlet.class);

    private static final long serialVersionUID = 6178443897065464665L;

    // configures upload settings
    private DiskFileItemFactory factory = new DiskFileItemFactory();
    
    
    public FileUploadServlet() {
        // sets memory threshold - beyond which files are stored in disk
        factory.setSizeThreshold(Config.get().getMemoryThreshold());
        // sets temporary location to store files
        factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
 
    }
 
    /**
     * Upon receiving file upload submission, parses the request to read
     * upload data and saves the file on disk.
     */
    @Override
    protected void doPost(HttpServletRequest request,  HttpServletResponse response) throws ServletException, IOException {
    	
        try {

        	// checks if the request actually contains upload file
            if (!ServletFileUpload.isMultipartContent(request)) {
            	LOG.warn("Ignoring invalid request " + request.getContentType());
            	response.sendError(400, "Error: Expected multipart/form-data.");
                return;
            }
            
            final String outDir = request.getPathInfo();
            if(null == outDir) {
            	LOG.warn("Ignoring invalid request, no outdir specified ");
            	response.sendError(400, "Error: outdir attribute is expected.");
                return;
            }
     
            ServletFileUpload upload = new ServletFileUpload(factory);
             
            // sets maximum size of upload file
            upload.setFileSizeMax(Config.get().getMaxFileSize());
             
            // sets maximum size of request (include file + form data)
            upload.setSizeMax(Config.get().getMaxRequestSize());
     
            // constructs the directory path to store upload file
            // this path is relative to application's directory
            String uploadPath = String.format("%s%s", Config.get().getUploadDirectory(), outDir);
             
            // creates the directory if it does not exist
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
            	LOG.info(String.format("Creating folder %s", uploadDir.getAbsolutePath()));
                uploadDir.mkdir();
            }
     
            // parses the request's content to extract file data
            List<FileItem> formItems = upload.parseRequest(request);
            
            int count = 0;
 
            if (formItems != null && formItems.size() > 0) {
                // iterates over form's fields
                for (FileItem item : formItems) {
                    // processes only fields that are not form fields
                    if (!item.isFormField()) {
                        File storeFile = new File(uploadDir, item.getName());
                    	LOG.info(String.format("Uploding %s", storeFile.getAbsolutePath()));
                        // saves the file on disk
                        item.write(storeFile);
                        ++count;
                    }
                }
            }
            
            LOG.info(String.format("Finished uploading %d file(s)", count));
            
            response.setStatus(200);
            response.flushBuffer();
            
        } catch (Exception ex) {
        	LOG.error("Upload Error", ex);
        	response.sendError(500, ExceptionUtils.getStackTrace(ex));
        }
        
    }
}