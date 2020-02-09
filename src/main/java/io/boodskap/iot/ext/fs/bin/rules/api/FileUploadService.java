package io.boodskap.iot.ext.fs.bin.rules.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;


@Path("/upload")
public class FileUploadService {

    private static final Logger LOG = LoggerFactory.getLogger(FileUploadService.class);
    
    private final String outDir;
    
    public FileUploadService(final Properties config) {
    	this.outDir = String.format("%s/out", config.getProperty("root_dir"));
    	String outDirs = config.getProperty("out_folders", "");
    	String[] outFolders = outDirs.split(",");
    	for(String outFolder : outFolders) {
    		if(!outFolder.trim().equals("")) {
    			new File(String.format("%s/%s", outDir, outFolder)).mkdirs();
    		}
    	}
    }

    @POST
    @Path("/files")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response multipleFiles(@FormDataParam("outdir") String outDir, @FormDataParam("files") List<File> files) {
        StringBuilder response = new StringBuilder();
        files.forEach(file -> {
            try {
            	final String outPath = String.format("%s/%s", this.outDir, outDir);
            	LOG.info(String.format("Uploding %s/%s", outPath, file.getName()));
            	new File(outPath).mkdirs();
                Files.copy(file.toPath(), Paths.get(outPath, file.getName()));
            } catch (IOException e) {
                response.append("Unable to upload the file ").append(e.getMessage());
                LOG.error("Error while Copying the file " + e.getMessage(), e);
            }
        });
        if (!response.toString().isEmpty()) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response.toString()).build();
        }
        return Response.ok().entity("Request completed").build();
    }

    @POST
    @Path("/stream")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response multipleFiles(@FormDataParam("outdir") String outDir, @FormDataParam("file") FileInfo fileInfo,
                                  @FormDataParam("file") InputStream inputStream) {
        try {
        	final String outPath = String.format("%s/%s", this.outDir, outDir);
        	LOG.info(String.format("Uploding %s/%s", outPath, fileInfo.getFileName()));
        	new File(outPath).mkdirs();
            Files.copy(inputStream, Paths.get(outPath, fileInfo.getFileName()));
        } catch (IOException e) {
            LOG.error("Error while Copying the file " + e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return Response.ok().entity("Request completed").build();
    }
    
}