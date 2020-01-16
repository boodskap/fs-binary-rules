package io.boodskap.iot.ext.fs.bin.rules.sftp;

import io.boodskap.iot.ext.fs.bin.rules.FileTransferClient;

public abstract class AbstractSFTPClient implements FileTransferClient{
	
	private int port = 22;
	private String host;
	private String userName;
	private String password;
	private String remoteDir;
	
	
	public abstract void open() throws Exception;
	
	public abstract void close() throws Exception;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getRemoteDir() {
		return remoteDir;
	}

	public void setRemoteDir(String remoteDir) {
		this.remoteDir = remoteDir;
	}

}
