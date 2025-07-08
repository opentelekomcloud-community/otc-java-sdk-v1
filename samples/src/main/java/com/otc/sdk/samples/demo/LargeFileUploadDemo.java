package com.otc.sdk.samples.demo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.otc.sdk.core.auth.vo.SignResult;
import com.otc.sdk.core.http.HttpMethodName;
import com.otc.sdk.core.util.BinaryUtils;
import com.otc.sdk.core.util.Constant;
import com.otc.sdk.core.util.HostName;
import com.otc.sdk.core.util.SSLCipherSuiteUtil;
import com.otc.sdk.core.util.SignUtils;
import com.otc.sdk.service.Request;

public class LargeFileUploadDemo {
  private static final Logger LOGGER = LoggerFactory.getLogger(LargeFileUploadDemo.class);
  private static final String UTF8 = "UTF-8";

  public static void main(String[] args) {
    String fname = "fileName";
    // Create a new request.
    Request fileUploadRequest = new Request();
    try {
      // Set the request parameters.
      // AppKey, AppSecrect, Method and Url are required parameters.
      // The ak and sk used for authentication are hard-coded into the code or plain
      // text storage, and it is recommended to store ciphertext in configuration
      // files or environment variables, decryption during use to ensure security;
      // This example takes ak and sk saved in environment variables as an example.
      // Before running this example, please set the environment variables
      // OTC_SDK_AK and OTC_SDK_SK in the local environment.
      
      fileUploadRequest.setKey(System.getenv("OTC_SDK_AK"));
      fileUploadRequest.setSecret(System.getenv("OTC_SDK_SK"));
      fileUploadRequest.setMethod(HttpMethodName.POST.toString());
      fileUploadRequest.setUrl("your url");
      fileUploadRequest.addHeader("Content-Type", "plain/text");
      String fileHash = calcSha256Hex(fname);
      fileUploadRequest.addHeader("x-sdk-content-sha256", fileHash);
      // if it was published in other envs(except for Release),you need to add the
      // information x-stage and the value is env's name
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
      return;
    }

    sendPostStream(fname, fileUploadRequest);
  }

  private static void sendPostStream(String fname, Request request) {
    HttpURLConnection conn = null;
    OutputStream outputStream = null;
    BufferedReader inputStreamReader = null;
    InputStream inputStream = null;
    try {
      conn = initConnAndSendContent(request, conn, fname);
      // Print the status line of the response.
      if (conn.getResponseCode() > 400) {
        inputStream = conn.getErrorStream();
      } else {
        inputStream = conn.getInputStream();
      }
      if (inputStream == null) {
        return;
      }

      StringBuilder result = new StringBuilder();
      inputStreamReader = new BufferedReader(new InputStreamReader(inputStream, UTF8));
      
      String line = "";
      while ((line = inputStreamReader.readLine()) != null) {
        result.append(line);
      }
      LOGGER.info("{}", result.toString());
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
    } finally {
      try {
        if (outputStream != null) {
          outputStream.close();
        }
        if (inputStream != null) {
          inputStream.close();
        }
        if (inputStreamReader != null) {
          inputStreamReader.close();
        }
      } catch (IOException e) {
        LOGGER.error(e.getMessage());
      }
      if (conn != null) {
        conn.disconnect();
      }
    }
  }

  private static HttpURLConnection initConnAndSendContent(Request request, HttpURLConnection conn, String fname)
      throws Exception {
    // Sign the request.
    SignResult signRet = SignUtils.sign(request);
    if (Constant.DO_VERIFY) {
      // initial connection and verify ssl certificate
      HostName.setUrlHostName(request.getHost());
      conn = SSLCipherSuiteUtil.createHttpsOrHttpURLConnectionWithVerify(signRet.getUrl(),
          Constant.INTERNATIONAL_PROTOCOL);
    } else {
      // initial connection and do not verify ssl certificate
      conn = SSLCipherSuiteUtil.createHttpsOrHttpURLConnection(signRet.getUrl(), Constant.INTERNATIONAL_PROTOCOL);
    }
    conn.setRequestMethod(request.getMethod().name());
    conn.setDoOutput(true);
    conn.setDoInput(true);

    // Send the request.
    if (signRet.getHeaders().size() > 0) {
      Set<String> headerSet = signRet.getHeaders().keySet();
      for (String key : headerSet) {
        String value = signRet.getHeaders().get(key);
        conn.setRequestProperty(key, value);
      }
    }
    // Send a file.
    setFileConent(fname, conn.getOutputStream());
    return conn;
  }

  public static void setFileConent(String filePath, OutputStream out) throws IOException {
    File file = new File(filePath);
    int count = 0;
    byte[] buffer = new byte[1024];

    try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
      while ((count = bis.read(buffer)) > 0) {
        out.write(buffer, 0, count);
      }
    }
  }

  private static String calcSha256Hex(String fileName) throws Exception {
    byte[] buffer = new byte[8192];
    int count;
    MessageDigest digest;
    BufferedInputStream bis = null;
    try {
      digest = MessageDigest.getInstance("SHA-256");
      bis = new BufferedInputStream(new FileInputStream(fileName));
      while ((count = bis.read(buffer)) > 0) {
        digest.update(buffer, 0, count);
      }
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error("NoSuchAlgorithmException");
      throw e;
    } catch (FileNotFoundException e) {
      LOGGER.error("FileNotFoundException");
      throw e;
    } finally {
      bis.close();
    }

    byte[] hash = digest.digest();
    return BinaryUtils.toHex(hash);
  }

}