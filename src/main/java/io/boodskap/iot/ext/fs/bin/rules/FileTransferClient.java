package io.boodskap.iot.ext.fs.bin.rules;

import java.io.File;

public interface FileTransferClient {
	
	public void put(String rule, File localFile) throws Exception;
	
}
