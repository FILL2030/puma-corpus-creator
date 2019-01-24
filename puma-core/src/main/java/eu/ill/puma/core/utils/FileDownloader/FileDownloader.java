/*
 * Copyright 2019 Institut Laue–Langevin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.ill.puma.core.utils.FileDownloader;

import eu.ill.puma.core.error.PumaException;
import eu.ill.puma.core.utils.ResourceLoader;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Created by letreguilly on 06/07/17.
 */
public class FileDownloader {

	private static final Logger log = LoggerFactory.getLogger(FileDownloader.class);

	//user agent string from ressource file
	private static String userAgentRessource = null;

	//default user agent, at position 0 in the list
	private String defaultUserAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36";

	//useragent list
	private static List<String> userAgentList;

	//random umber generator used to get random user agent
	private Random randomGenerator;

	boolean randomUserAgent;

	public FileDownloader(boolean randomUserAgent) {
		//init random generator
		randomGenerator = new Random();

		//setup
		this.randomUserAgent = randomUserAgent;

		//read useragent file
		if (userAgentRessource == null) {
			synchronized (FileDownloader.class) {
				if (userAgentRessource == null) {
					try {
						userAgentRessource = ResourceLoader.readString("userAgent.txt");
					} catch (UnsupportedEncodingException e) {
						log.error("can not read useragent file", e);
					}
				}
			}
		}
		//init list
		userAgentList = new ArrayList();
		userAgentList.add(defaultUserAgent);

		//add userAgent from file to the list
		if (userAgentRessource != null && userAgentRessource.length() > 10) {
			userAgentList.addAll(Arrays.asList(userAgentRessource.split("\\r?\\n")));
		}
	}

	/**
	 * download a file from an url using apache http client
	 *
	 * @param urlString the url of the file to download
	 * @return a FileDownloaderResponse which contains the file data and the minetype
	 * @throws PumaException
	 */

	public FileDownloaderResponse downloadWithRandomDelay(String urlString, int minDelay, int maxDelay) throws PumaException, InterruptedException {
		try {
			Thread.sleep(randomGenerator.nextInt(maxDelay - minDelay) + minDelay);
		} finally {
			return this.downloadFileFrom(urlString);
		}
	}

	public FileDownloaderResponse downloadFileFrom(String urlString) throws PumaException {
		//init
		FileDownloaderResponse webClientResponse = new FileDownloaderResponse();
		CloseableHttpClient httpClient = null;

		try {
			//build client
			httpClient = HttpClientBuilder.create().useSystemProperties().build();


			//build request
			HttpGet request = this.buildRequest(urlString);

			//do query
			CloseableHttpResponse response = httpClient.execute(request);

			//get file data
			InputStream inputStream = response.getEntity().getContent();
			webClientResponse.setFileData(IOUtils.toByteArray(inputStream));

			//get minetype
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				Header header = entity.getContentType();
				if (header == null) {
					header = response.getFirstHeader("Content-Type");
				}
				if (header != null) {
					String mimeTypes = header.getValue();
					String[] allMimeTypes = mimeTypes.split(";");
					webClientResponse.setMimeType(allMimeTypes[0]);
				} else {
					if (webClientResponse.isPdf()) {
						webClientResponse.setMimeType("application/pdf");
					}
				}
			}

		} catch (IOException e) {
			throw new PumaException("Download failed from URL " + urlString + ": + " + e.getMessage(), e);
		} finally {
			try {
				// Close client
				httpClient.close();
			} catch (IOException ioe) {
				// log as warning
				log.warn("Error closing http client after request to " + urlString, ioe);
			}
		}

		return webClientResponse;
	}

	/**
	 * build the request
	 *
	 * @param urlString the url of the request
	 * @return an object representing the request
	 */
	private HttpGet buildRequest(String urlString) {
		RequestConfig requestConfig = RequestConfig.custom()
				.setCookieSpec(CookieSpecs.STANDARD)
				.build();

		HttpGet request = new HttpGet(urlString);
		request.setConfig(requestConfig);

		this.setRequestHeader(request);

		return request;
	}

	/**
	 * set the request header
	 *
	 * @param request
	 */
	private void setRequestHeader(HttpGet request) {
		this.setUserAgent(request);
	}

	/**
	 * set a random user agent into the request header
	 *
	 * @param request
	 */
	private void setUserAgent(HttpGet request) {
		String useragent;

		if (randomUserAgent) {
			useragent = this.randomUserAgent();
		} else {
			useragent = defaultUserAgent;
		}

		request.setHeader("User-Agent", useragent);
	}

	/**
	 * return a random user agent
	 *
	 * @return
	 */
	private String randomUserAgent() {
		int index = randomGenerator.nextInt(userAgentList.size());
		return userAgentList.get(index);
	}
}