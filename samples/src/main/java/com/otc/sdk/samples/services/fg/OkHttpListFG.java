/*
 * Copyright (c) 2025 T-Systems International GmbH.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.otc.sdk.samples.services.fg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.otc.sdk.core.util.Constant;
import com.otc.sdk.core.util.HostName;
import com.otc.sdk.core.util.SSLCipherSuiteUtil;
import com.otc.sdk.service.Client;
import com.otc.sdk.service.Request;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpListFG {
  private static final Logger LOGGER = LoggerFactory.getLogger(OkHttpListFG.class);

  public static void main(String[] args) throws Exception {
    // Create a new request.
    Request OkHttpRequest = new Request();

    try {
      // Set the request parameters.

      String ak = System.getenv("OTC_SDK_AK");
      String sk = System.getenv("OTC_SDK_SK");
      String projectId = System.getenv("OTC_SDK_PROJECTID");
      OkHttpRequest.setKey(ak);
      OkHttpRequest.setSecret(sk);
      OkHttpRequest.setMethod("GET");

      String url = String.format("https://functiongraph.eu-de.otc.t-systems.com/v2/%s/fgs/functions", projectId);
      OkHttpRequest.setUrl(url);

      OkHttpRequest.addHeader("Content-type", "application/json;charset=utf8");
      OkHttpRequest.addHeader("X-Project-Id", System.getenv("OTC_SDK_PROJECTID"));

    } catch (Exception e) {
      LOGGER.error("fail to contain request: {}", e.getMessage());
      throw e;
    }

    try {
      // Sign the request.
      okhttp3.Request signedRequest = Client.signOkhttp(OkHttpRequest, Constant.SIGNATURE_ALGORITHM_SDK_HMAC_SHA256);
      OkHttpClient client;
      if (Constant.DO_VERIFY) {
        // create okhttpClient and verify ssl certificate
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
      LOGGER.info("body: " + resEntity.string());
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
    }

  }
}
