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
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * WebSocketDemo class demonstrates how to use the OTC SDK to make WebSocket
 * requests with authentication.
 * It shows how to create a request, sign it, and execute it using OkHttpClient
 * for WebSocket communication.
 * The response messages are printed to the console.
 */
public class WebSocketDemo {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketDemo.class);

  public static void main(String[] args) throws Exception {
    // Create a new request.
    Request webSocketRequest = new Request();
    try {
      // The ak and sk used for authentication are hard-coded into the code or plain
      // text storage, and it is recommended to store ciphertext in configuration
      // files or environment variables, decryption during use to ensure security;
      // This example takes ak and sk saved in environment variables as an example.
      // Before running this example, please set the environment variables
      // OTC_SDK_AK and OTC_SDK_SK in the local environment.
      
      webSocketRequest.setKey(System.getenv("OTC_SDK_AK"));
      webSocketRequest.setSecret(System.getenv("OTC_SDK_SK"));
      webSocketRequest.setMethod(HttpMethodName.GET.toString());
      webSocketRequest.setUrl("your url");
    } catch (Exception e) {
      LOGGER.error("fail to contain request: {}", e.getMessage());
      throw e;
    }
    try {
      // Sign the request.
      okhttp3.Request signedRequest = Client.signOkhttp(webSocketRequest, Constant.SIGNATURE_ALGORITHM_SDK_HMAC_SHA256);
      OkHttpClient webSocketClient;
      if (Constant.DO_VERIFY) {
        // creat okhttpClient and verify ssl certificate
        HostName.setUrlHostName(webSocketRequest.getHost());
        webSocketClient = SSLCipherSuiteUtil.createOkHttpClientWithVerify(Constant.INTERNATIONAL_PROTOCOL);
      } else {
        // creat okhttpClient and do not verify ssl certificate
        webSocketClient = SSLCipherSuiteUtil.createOkHttpClient(Constant.INTERNATIONAL_PROTOCOL);
      }
      WebSocketListener webSocketListener = new WebSocketListener() {
        @Override
        public void onMessage(WebSocket webSocket, String text) {
          LOGGER.info("receive: " + text);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
          LOGGER.info("fail   : " + t.getMessage());
        }
      };
      WebSocket webSocket = webSocketClient.newWebSocket(signedRequest, webSocketListener);
      for (int i = 0; i < 10; i++) {
        String msg = "hello," + System.currentTimeMillis();
        LOGGER.info("send   : " + msg);
        webSocket.send(msg);
        Thread.sleep(50);
      }
      webSocket.close(1000, null);
      webSocketClient.dispatcher().executorService().shutdown();
    } catch (Exception e) {
      LOGGER.error("fail to send the request: {}", e.getMessage());
      throw e;
    }
  }
}