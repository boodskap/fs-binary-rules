package io.boodskap.iot.ext.fs.bin.rules.api;

public class Config {

	private static final Config instance = new Config();
	
    private String uploadDirectory;
    private int memoryThreshold;
    private int maxFileSize;
    private int maxRequestSize;

    private Config() {
	}
    
    public static final Config get() {
    	return instance;
    }

	public String getUploadDirectory() {
		return uploadDirectory;
	}

	public void setUploadDirectory(String uploadDirectory) {
		this.uploadDirectory = uploadDirectory;
	}

	public int getMemoryThreshold() {
		return memoryThreshold;
	}

	public void setMemoryThreshold(int memoryThreshold) {
		this.memoryThreshold = memoryThreshold;
	}

	public int getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(int maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public int getMaxRequestSize() {
		return maxRequestSize;
	}

	public void setMaxRequestSize(int maxRequestSize) {
		this.maxRequestSize = maxRequestSize;
	}
	
}
