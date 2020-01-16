package io.boodskap.iot.ext.fs.bin.rules.http;

import java.io.File;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.boodskap.iot.ext.fs.bin.rules.FileTransferClient;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

public class BoodskapApiClient implements FileTransferClient {
	
	private static final Logger LOG = LoggerFactory.getLogger(BoodskapApiClient.class);
	
	private Properties config;
	
	public BoodskapApiClient(Properties config) {
		this.config = config;
	}

	@Override
	public void put(String rule, File localFile) throws Exception {
		
		LOG.info(String.format("Uploading binary rule:%s file:%s", rule, localFile));

		final String url = String.format("%s/push/bin/file/{dkey}/{akey}/{did}/{dmdl}/{fwver}/{type}", config.getProperty("api_base_path"));
		
		final String device = config.getProperty("out.protocol");
		final String model = config.getProperty(String.format("%s.impl", device));
		
		JsonNode res = Unirest.post(url)
		.routeParam("dkey", config.getProperty("domain_key"))
		.routeParam("akey", config.getProperty("api_key"))
		.routeParam("did", device)
		.routeParam("dmdl", model)
		.routeParam("fwver", "1.0.0")
		.routeParam("type", rule)
		.field("binfile", localFile)
		.asJson().getBody();
		
		LOG.info(res.toString());
	}

}
