package io.boodskap.iot.ext.fs.bin.rules;

import java.io.File;
import java.util.Map;

public interface ApiClient {

	public void put(String rule, File localFile) throws Exception;
	
	public void status(String rule, Map<String, Object> map) throws Exception;
}
