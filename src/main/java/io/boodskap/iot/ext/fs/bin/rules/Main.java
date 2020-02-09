package io.boodskap.iot.ext.fs.bin.rules;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.MicroservicesRunner;

import io.boodskap.iot.ext.fs.bin.rules.api.FileUploadService;

public class Main implements FSWatcherService.Handler {

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	private static ExecutorService exec = Executors.newCachedThreadPool();
	private static MicroservicesRunner microServices;
	
	static {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				LOG.warn("Received kill, shutting down...");
				exec.shutdownNow();
				if(null != microServices) microServices.stop();
			}
		}));
	}

	public static void main(String[] args) {

		try {

			final Properties config = new Properties();

			File home = new File(System.getProperty("user.home", "/etc/fsbinrules"));
			File cfgFolder = new File(home, "config");
			File cfgFile = new File(cfgFolder, "fsbinrules.properties");

			if (!cfgFile.exists()) {
				LOG.error(String.format("Config file:%s does not exists, using defaults", cfgFile.getAbsolutePath()));
				config.load(Main.class.getResourceAsStream("/fsbinrules.properties"));
			} else {
				config.load(new FileReader(cfgFile));
			}
			
			File root = new File(config.getProperty("root_dir", System.getProperty("user.home")));
			File in = new File(root, "in");
			File out = new File(root, "out");
			
			if(!in.exists()) {
				LOG.warn(String.format("Creating incoming directory %s", in.getAbsolutePath()));
				in.mkdirs();
			}
			
			if(!out.exists()) {
				LOG.warn(String.format("Creating outgoing directory %s", out.getAbsolutePath()));
				out.mkdirs();
			}
			
			if(Boolean.valueOf(config.getProperty("out.enabled", "true"))) {
				FileTransfer.init(config);
			}
			
			exec.submit(new FSWatcherService(root.toPath(), new Main()));
			exec.submit(new InFileProcessor(config));
			exec.submit(new OutFileProcessor(config));
			
			microServices = new MicroservicesRunner(Integer.valueOf(config.getProperty("micro_service_port", "19091")));
			microServices.deploy(new FileUploadService(config));
			microServices.start();

		} catch (Exception ex) {
			LOG.error("Init failed", ex);
		}
	}

	@Override
	public void handle(String inout, String rule, Path path) {
		
		try {
			
			switch(inout.toUpperCase()) {
			case "IN":
				InOutQueue.getInq().offer(new QueuedFile(rule, path.toFile().getAbsolutePath()));
				break;
			case "OUT":
				InOutQueue.getOutq().offer(new QueuedFile(rule, path.toFile().getAbsolutePath()));
				break;
			default:
				throw new Exception("Unknown inout type:" + inout);
			}
			
		}catch(Exception ex) {
			LOG.error("Processing failed", ex);
		}
	}

}
