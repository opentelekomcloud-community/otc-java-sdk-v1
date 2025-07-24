package com.otc.sdk.samples.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.otc.sdk.core.http.HttpMethodName;
import com.otc.sdk.core.util.Constant;
import com.otc.sdk.core.util.HostName;
import com.otc.sdk.core.util.SSLCipherSuiteUtil;
import com.otc.sdk.service.Client;
import com.otc.sdk.service.Request;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * OkHttpDemo class demonstrates how to use the OTC SDK to make HTTP requests
 * with authentication using OkHttp.
 * It shows how to create a request, sign it, and execute it using OkHttpClient.
 * The response body is printed to the console.
 */
public class OkHttpDemo {
  private static final Logger LOGGER = LoggerFactory.getLogger(OkHttpDemo.class);

  public static void main(String[] args) {
    // Create a new request.
    Request OkHttpRequest = new Request();
    try {
      // The ak and sk used for authentication are hard-coded into the code or plain
      // text storage, and it is recommended to store ciphertext in configuration
      // files or environment variables, decryption during use to ensure security;
      // This example takes ak and sk saved in environment variables as an example.
      // Before running this example, please set the environment variables
      // OTC_SDK_AK and OTC_SDK_SK in the local environment.
      
      OkHttpRequest.setKey(System.getenv("OTC_SDK_AK"));
      OkHttpRequest.setSecret(System.getenv("OTC_SDK_SK"));
      OkHttpRequest.setMethod(HttpMethodName.GET.toString());
      OkHttpRequest.setUrl("your url");
      OkHttpRequest.addHeader("Content-Type", "text/plain");
      OkHttpRequest.setBody("demo");
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
      return;
    }
    try {
      // Sign the request.
      okhttp3.Request signedRequest = Client.signOkhttp(OkHttpRequest, Constant.SIGNATURE_ALGORITHM_SDK_HMAC_SHA256);
      OkHttpClient client;
      if (Constant.DO_VERIFY) {
        // creat okhttpClient and verify ssl certificate
        HostName.setUrlHostName(OkHttpRequest.getHost());
        client = SSLCipherSuiteUtil.createOkHttpClientWithVerify(Constant.INTERNATIONAL_PROTOCOL);
      } else {
        // create okhttpClient and do not verify ssl certificate
        client = SSLCipherSuiteUtil.createOkHttpClient(Constant.INTERNATIONAL_PROTOCOL);
      }
      // Send the request.
      Response response = client.newCall(signedRequest).execute();
      // Print the status line of the response.
      LOGGER.info("status: " + response.code());
      // Print the body of the response.
      ResponseBody resEntity = response.body();
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
    }
  }
}