package io.boodskap.iot.ext.fs.bin.rules;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.boodskap.iot.ext.fs.bin.rules.http.BoodskapApiClient;
import io.boodskap.iot.ext.fs.bin.rules.sftp.AbstractSFTPClient;
import io.boodskap.iot.ext.fs.bin.rules.sftp.JSchClient;

public final class FileTransfer {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);

	private static FileTransferClient outClient;
	private static BoodskapApiClient inClient;
	
	private FileTransfer() {
		
	}
	
	protected static void init(Properties config) throws Exception {
		
		final File home = new File(System.getProperty("user.home", "/etc/fsbinrules"));
		final File cfgFolder = new File(home, "config");
		
		if(Boolean.valueOf(config.getProperty("out.enabled", "true"))) {
			switch(config.getProperty("out.protocol")) {
			case "sftp":
				initSFTP(config, cfgFolder);
				break;
			default:
				throw new Exception("Unimplemented protocol:" + config.getProperty("out.protocol"));
			}
			
		}

		inClient = new BoodskapApiClient(config);
		
	}

	private static void initSFTP(Properties config, File cfgFolder) throws Exception {
		
		final Properties props = new Properties();
		final String impl = config.getProperty("sftp.impl", "jsch");
		final String host = config.getProperty("sftp.host", "localhost");
		final int port = Integer.valueOf(config.getProperty("sftp.port", "22"));
		final String userName = config.getProperty("sftp.user_name");
		final String password = config.getProperty("sftp.password");
		final String remoteDir = config.getProperty("sftp.remote_dir");

		switch (impl) {
		case "jsch":
			File implCfgFile = new File(cfgFolder, "jsch.properties");
			if (!implCfgFile.exists()) {
				LOG.warn(String.format("Config file:%s does not exists, using defaults", implCfgFile.getAbsolutePath()));
				props.load(Main.class.getResourceAsStream("/jsch.properties"));
			} else {
				props.load(new FileReader(implCfgFile));
			}
			outClient = new JSchClient(config, props);
			break;
		default:
			throw new Exception("Unknown SFTP implementation:" + impl);

		}

		((AbstractSFTPClient)outClient).setHost(host);
		((AbstractSFTPClient)outClient).setPort(port);
		((AbstractSFTPClient)outClient).setRemoteDir(remoteDir);
		((AbstractSFTPClient)outClient).setUserName(userName);
		((AbstractSFTPClient)outClient).setPassword(password);
		
	}
	
	public static final FileTransferClient getOutClient() {
		return outClient;
	}
	
	public static final BoodskapApiClient getInClient() {
		return inClient;
	}
}
