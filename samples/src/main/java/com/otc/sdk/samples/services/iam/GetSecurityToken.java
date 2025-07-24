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
package com.otc.sdk.samples.services.iam;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.otc.sdk.core.util.Constant;
import com.otc.sdk.core.util.SSLCipherSuiteUtil;
import com.otc.sdk.samples.services.fg.HttpClientListFG;
import com.otc.sdk.service.Client;

/**
 * GetSecurityToken class demonstrates how to obtain a security token using the OTC SDK.
 * It shows how to create a request, sign it, and execute it using an HTTP client.
 * The security access key, secret key, and security token are printed to the console.
 */
public class GetSecurityToken {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientListFG.class);

  public static void main(String[] args) throws Exception {

    CloseableHttpClient client = null;
    try {
      String url = String.format("%s/v3.0/OS-CREDENTIAL/securitytokens", System.getenv("OTC_IAM_ENDPOINT"));

      String ak = System.getenv("OTC_SDK_AK");
      String sk = System.getenv("OTC_SDK_SK");
      String body = "{\"auth\":{\"identity\":{\"methods\":[\"token\"],\"token\":{\"duration_seconds\":900}}}}";

      Map<String, String> headers = new HashMap<>();
      headers.put("Content-type", "application/json;charset=utf8");

      HttpRequestBase postRequest = Client.post(ak, sk, url, headers, body);

      client = (CloseableHttpClient) SSLCipherSuiteUtil.createHttpClient(Constant.INTERNATIONAL_PROTOCOL);

      HttpResponse response = client.execute(postRequest);
      HttpEntity resEntity = response.getEntity();
      if (resEntity != null) {
        String jsonString = EntityUtils.toString(resEntity, "UTF-8");

        LOGGER.info(jsonString);
        JsonObject obj = JsonParser.parseString(jsonString).getAsJsonObject();

        JsonObject credetial = obj.get("credential").getAsJsonObject();

        String sak = credetial.get("access").getAsString();
        String ssk = credetial.get("secret").getAsString();
        String stoken = credetial.get("securitytoken").getAsString();

        LOGGER.info("SecurityAccessKey: " + sak);
        LOGGER.info("SecuritySecretKey: " + ssk);
        LOGGER.info("SecurityToken: " + stoken);

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
