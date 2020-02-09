package io.boodskap.iot.ext.fs.bin.rules.sftp;

import java.io.File;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class JSchClient extends AbstractSFTPClient{
	
	private static final Logger LOG = LoggerFactory.getLogger(JSchClient.class);
	
	private final Properties config;
	private final Properties props;
	private Session session;
	private ChannelSftp channel;
	
	public JSchClient(Properties config, Properties props) {
		this.config = config;
		this.props = props;
	}
	
	@Override
	public void open() throws Exception {
		
		LOG.info(String.format("Opening channel Host:%s:%d, User:%s", getHost(), getPort(), getUserName()));
		
		JSch.setConfig(props);

	    JSch jsch = new JSch();
	    
	    String defKnownHosts = System.getProperty("user.home") + "/.ssh/known_hosts";
	    String knownHosts = config.getProperty("jsch.known_hosts", defKnownHosts);
	    
	    jsch.setKnownHosts(knownHosts);
	    
	    if(Boolean.valueOf(config.getProperty("jsch.keyfile_auth", "false"))) {
	    	String prvkey = config.getProperty("jsch.prvkey");
	    	String pubkey = config.getProperty("jsch.pubkey");
	    	String passphrase = config.getProperty("jsch.passphrase");
		    jsch.addIdentity(prvkey, pubkey, null != passphrase ? passphrase.getBytes() : "".getBytes());
	    }
	    
    	session = jsch.getSession(getUserName(), getHost(), getPort());
    	
    	if(!Boolean.valueOf(config.getProperty("jsch.keyfile_auth", "false"))) {
    		session.setPassword(getPassword());
    	}
    	
	    session.connect();
	    
	    channel = (ChannelSftp) session.openChannel("sftp");
	    channel.connect();
	}

	@Override
	public void close() throws Exception {
		
		if(null != channel) {
			LOG.info("Closing channel..");
			channel.disconnect();
			channel.exit();
		}
		
		if(null != session) {
			LOG.info("Closing session..");
			session.disconnect();
		}
		
		channel = null;
		session = null;
	}

	@Override
	public void put(String rule, File localFile) throws Exception {
		
		final String remoteFile = String.format("%s/%s", getRemoteDir(), localFile.getName());
		
		LOG.info(String.format("Uploading file rule:%s, local:%s, remote:%s", rule, localFile, remoteFile));
		
		try {
			open();
			channel.put(localFile.getAbsolutePath(), remoteFile);
		}finally {
			close();
		}
	}
	
}
