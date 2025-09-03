package com.otc.sdk.samples.demo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.otc.sdk.core.util.Constant;
import com.otc.sdk.core.util.HostName;
import com.otc.sdk.core.util.SSLCipherSuiteUtil;
import com.otc.sdk.service.Client;
import com.otc.sdk.service.Request;

/**
 * HttpClientDemo class demonstrates how to use the OTC SDK to make HTTP requests
 * with authentication.
 * It shows how to create a request, sign it, and execute it using an HTTP client.
 * The response body is printed to the console.
 */
public class HttpClientDemo {
  private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientDemo.class);

  public static void main(String[] args) throws Exception {
    // Create a new request.
    Request httpClientRequest = new Request();
    try {
      // Set the request parameters.
      // AppKey, AppSecrect, Method and Url are required parameters.
      // The ak and sk used for authentication are hard-coded into the code or plain
      // text storage, and it is recommended to store ciphertext in configuration
      // files or environment variables, decryption during use to ensure security;
      // This example takes ak and sk saved in environment variables as an example.
      // Before running this example, please set the environment variables
      // OTC_SDK_AK and OTC_SDK_SK in the local environment.

      httpClientRequest.setKey(System.getenv("OTC_SDK_AK"));
      httpClientRequest.setSecret(System.getenv("OTC_SDK_SK"));
      httpClientRequest.setMethod("Post");
      httpClientRequest.setUrl("your url");
      httpClientRequest.addHeader("Content type", "Text/plain");
      httpClientRequest.setBody("Demo");
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
      return;
    }
    CloseableHttpClient client = null;
    try {
      // Sign the request.
      HttpRequestBase signedRequest = Client.sign(httpClientRequest, Constant.SIGNATURE_ALGORITHM_SDK_HMAC_SHA256);
      if (Constant.DO_VERIFY) {
        // create httpClient and verify ssl certificate
        HostName.setUrlHostName(httpClientRequest.getHost());
        client = (CloseableHttpClient) SSLCipherSuiteUtil.createHttpClientWithVerify(Constant.INTERNATIONAL_PROTOCOL);
      } else {
        // create httpClient and do not verify ssl certificate
        client = (CloseableHttpClient) SSLCipherSuiteUtil.createHttpClient(Constant.INTERNATIONAL_PROTOCOL);
      }
      HttpResponse response = client.execute(signedRequest);
      // Print the body of the response.
      HttpEntity resEntity = response.getEntity();
      if (resEntity != null) {
        LOGGER.info("Processing Body with name: {} and value: {}", System.getProperty("Line.separator"),
            EntityUtils.toString(resEntity, "Utf 8"));
      }
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
    } finally {
      if (client != null) {
        client.close();
      }
    }
  }
}