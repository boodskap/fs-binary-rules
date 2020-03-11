package io.boodskap.iot.ext.fs.bin.rules.http;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.boodskap.iot.ext.fs.bin.rules.ApiClient;

public class HttpApiClient implements ApiClient {
	
	private static final Logger LOG = LoggerFactory.getLogger(HttpApiClient.class);
	
	private Properties config;
	
	public HttpApiClient(Properties config) {
		this.config = config;
	}

	@Override
	public void put(String rule, File localFile) throws Exception {
		
	    CloseableHttpClient client = HttpClientBuilder.create().build();
	    
	    try {
	    	
			LOG.info(String.format("Uploading binary rule:%s file:%s", rule, localFile));

			final String dkey = config.getProperty("domain_key");
			final String akey = config.getProperty("api_key");
			final String device = config.getProperty("out.protocol");
			final String model = config.getProperty(String.format("%s.impl", device));

			final String url = String.format("%s/push/bin/file/%s/%s/%s/%s/-/%s", config.getProperty("api_base_path"), dkey, akey, device, model, rule);
			

			HttpPost post = new HttpPost(url);
		    MultipartEntityBuilder builder = MultipartEntityBuilder.create();

		    builder.addPart("binfile", new FileBody(localFile));
		    post.setEntity(builder.build());
		    HttpResponse response = client.execute(post);;
		    int httpStatus = response.getStatusLine().getStatusCode();
		    String responseMsg = EntityUtils.toString(response.getEntity(), "UTF-8");
		    
		    LOG.info(responseMsg);

		    // If the returned HTTP response code is not in 200 series then
		    // throw the error
		    if (httpStatus < 200 || httpStatus > 300) {
		        throw new IOException("HTTP " + httpStatus + " - Error during upload of file: " + responseMsg);
		    }
		    
	    }finally {
		    client.close();
	    }

	}

	@Override
	public void status(String rule, Map<String, Object> map) throws Exception {
		
		CloseableHttpClient client = HttpClientBuilder.create().build();
		
		try {
			
			LOG.info(String.format("Sending webhook status rule:%s-webhook data:%s", rule, map));

			final String dkey = config.getProperty("domain_key");
			final String akey = config.getProperty("api_key");
			final String device = config.getProperty("out.protocol");
			final String model = config.getProperty(String.format("%s.impl", device));

			final String url = String.format("%s/push/bin/json/%s/%s/%s/%s/-/%s", config.getProperty("api_base_path"),  dkey, akey, device, model, rule);
			
			HttpPost post = new HttpPost(url);
		    post.setEntity(new StringEntity(new JSONObject(map).toString()));
		    HttpResponse response = client.execute(post);;
		    int httpStatus = response.getStatusLine().getStatusCode();
		    String responseMsg = EntityUtils.toString(response.getEntity(), "UTF-8");
		    
		    LOG.info(responseMsg);

		    // If the returned HTTP response code is not in 200 series then
		    // throw the error
		    if (httpStatus < 200 || httpStatus > 300) {
		        throw new IOException("HTTP " + httpStatus + " - Error during upload of file: " + responseMsg);
		    }
		}finally {
			client.close();
		}
		
	}

}
