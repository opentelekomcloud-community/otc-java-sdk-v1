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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.otc.sdk.core.http.HttpMethodName;
import com.otc.sdk.core.util.Constant;
import com.otc.sdk.core.util.HostName;
import com.otc.sdk.core.util.SSLCipherSuiteUtil;
import com.otc.sdk.service.Client;
import com.otc.sdk.service.Request;

/**
 * HttpClientListFG class demonstrates how to list Function Graph functions using
 * the OTC SDK.
 * It shows how to create a request, sign it, and execute it using an HTTP client.
 * The response body is printed to the console.
 */
public class HttpClientListFG {
  private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientListFG.class);

  public static void main(String[] args) throws Exception {
    // Create a new request.
    Request httpClientRequest = new Request();
    try {
      // Set the request parameters.

      String ak = System.getenv("OTC_SDK_AK");
      String sk = System.getenv("OTC_SDK_SK");
      String projectId = System.getenv("OTC_SDK_PROJECTID");
      String region = System.getenv("OTC_SDK_REGION");

      httpClientRequest.setKey(ak);
      httpClientRequest.setSecret(sk);
      httpClientRequest.setMethod(HttpMethodName.GET.toString());

      String url = String.format("https://functiongraph.%s.otc.t-systems.com/v2/%s/fgs/functions",region, projectId);

      httpClientRequest.setUrl(url);
      httpClientRequest.addHeader("Content-type", "application/json;charset=utf8");

      httpClientRequest.addHeader("X-Project-Id", projectId);

      httpClientRequest.setBody(null);

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
        String jsonString = EntityUtils.toString(resEntity, "UTF-8");

        JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();

        LOGGER.info("function count: " + obj.get("functions").getAsJsonArray().size());

        JsonArray functions = obj.get("functions").getAsJsonArray();
        for (JsonElement jsonElement : functions) {
          LOGGER.info(jsonElement.getAsJsonObject().get("func_name").getAsString());

        }
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
