package io.boodskap.iot.ext.fs.bin.rules;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.boodskap.iot.ext.fs.bin.rules.api.FileUploadService;

public class Main implements FSWatcherService.Handler {

	private static File home = new File(System.getProperty("user.home", "/etc/fsbinrules"));
	private static File cfgFolder = new File(home, "config");
	private static File cfgFile = new File(cfgFolder, "fsbinrules.properties");
	
	static {
		File log4j = new File(cfgFolder, "log4j.properties");
		if(log4j.exists()) {
			System.err.format("Log4j:%s\n", log4j.getAbsolutePath());
			PropertyConfigurator.configure(log4j.getAbsolutePath());
		}
	}

	private static final Logger LOG = LoggerFactory.getLogger(Main.class);
	private static ExecutorService exec = Executors.newCachedThreadPool();
	private static FileUploadService microServices;
	
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
			
			exec.submit(new FSWatcherService(root.toPath(), new Main()));
			exec.submit(new InFileProcessor(config));
			exec.submit(new FileUploadService(config));

			if(Boolean.valueOf(config.getProperty("out.enabled", "true"))) {
				FileTransfer.init(config);
				exec.submit(new OutFileProcessor(config));
			}
			

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
