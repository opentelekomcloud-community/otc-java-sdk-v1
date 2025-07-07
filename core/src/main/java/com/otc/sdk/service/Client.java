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

package com.otc.sdk.service;

import java.util.Map;
import org.apache.http.client.methods.HttpRequestBase;

import com.otc.sdk.core.http.HttpMethodName;

public class Client {
   public static HttpRequestBase sign(Request request, String messageDigestAlgorithm) throws Exception {
      String appKey = request.getKey();
      String appSecrect = request.getSecrect();
      AccessService accessService = new AccessServiceImpl(appKey, appSecrect, messageDigestAlgorithm);
      String url = request.getUrl();
      String body = request.getBody();
      if (body == null) {
         body = "";
      }

      Map<String, String> headers = request.getHeaders();
      switch(request.getMethod()) {
      case GET:
         return accessService.access(url, headers, "", HttpMethodName.GET);
      case POST:
         return accessService.access(url, headers, body, HttpMethodName.POST);
      case PUT:
         return accessService.access(url, headers, body, HttpMethodName.PUT);
      case PATCH:
         return accessService.access(url, headers, body, HttpMethodName.PATCH);
      case DELETE:
         return accessService.access(url, headers, "", HttpMethodName.DELETE);
      case HEAD:
         return accessService.access(url, headers, "", HttpMethodName.HEAD);
      case OPTIONS:
         return accessService.access(url, headers, "", HttpMethodName.OPTIONS);
      default:
         throw new IllegalArgumentException(String.format("unsupported method:%s", request.getMethod().name()));
      }
   }

   public static synchronized HttpRequestBase sign(Request request) throws Exception {
      return sign(request, "SDK-HMAC-SHA256");
   }

   public static okhttp3.Request signOkhttp(Request request, String messageDigestAlgorithm) throws Exception {
      Client.ParamsEntity pe = new Client.ParamsEntity(request.getKey(), request.getSecrect(), request.getUrl(), request.getBody(), messageDigestAlgorithm);
      return okhttpRequest(request.getMethod(), request.getHeaders(), pe);
   }

   public static okhttp3.Request signOkhttp(Request request) throws Exception {
      return okhttpRequest(request.getMethod(), request.getKey(), request.getSecrect(), request.getUrl(), request.getHeaders(), request.getBody());
   }

   public static HttpRequestBase put(String ak, String sk, String requestUrl, Map<String, String> headers, String putBody) throws Exception {
      if (putBody == null) {
         putBody = "";
      }

      AccessService accessService = new AccessServiceImpl(ak, sk);
      return accessService.access(requestUrl, headers, putBody, HttpMethodName.PUT);
   }

   public static HttpRequestBase patch(String ak, String sk, String requestUrl, Map<String, String> headers, String body) throws Exception {
      if (body == null) {
         body = "";
      }

      AccessService accessService = new AccessServiceImpl(ak, sk);
      return accessService.access(requestUrl, headers, body, HttpMethodName.PATCH);
   }

   public static HttpRequestBase delete(String ak, String sk, String requestUrl, Map<String, String> headers) throws Exception {
      AccessService accessService = new AccessServiceImpl(ak, sk);
      return accessService.access(requestUrl, headers, HttpMethodName.DELETE);
   }

   public static HttpRequestBase get(String ak, String sk, String requestUrl, Map<String, String> headers) throws Exception {
      AccessService accessService = new AccessServiceImpl(ak, sk);
      return accessService.access(requestUrl, headers, HttpMethodName.GET);
   }

   public static HttpRequestBase post(String ak, String sk, String requestUrl, Map<String, String> headers, String postbody) throws Exception {
      if (postbody == null) {
         postbody = "";
      }

      AccessService accessService = new AccessServiceImpl(ak, sk);
      return accessService.access(requestUrl, headers, postbody, HttpMethodName.POST);
   }

   public static HttpRequestBase head(String ak, String sk, String requestUrl, Map<String, String> headers) throws Exception {
      AccessService accessService = new AccessServiceImpl(ak, sk);
      return accessService.access(requestUrl, headers, HttpMethodName.HEAD);
   }

   public static HttpRequestBase options(String ak, String sk, String requestUrl, Map<String, String> headers) throws Exception {
      AccessService accessService = new AccessServiceImpl(ak, sk);
      return accessService.access(requestUrl, headers, HttpMethodName.OPTIONS);
   }

   private static okhttp3.Request okhttpRequest(HttpMethodName httpMethod, Map<String, String> headers, Client.ParamsEntity pe) throws Exception {
      switch(httpMethod) {
      case GET:
      case HEAD:
      case OPTIONS:
         pe.body = "";
      case POST:
      case PUT:
      case PATCH:
      case DELETE:
         if (pe.body == null) {
            pe.body = "";
         }

         AccessServiceOkhttp accessServiceOkhttp = new AccessServiceOkhttpImpl(pe.appKey, pe.secretKeyk, pe.algorithm);
         return accessServiceOkhttp.access(pe.requestUrl, headers, pe.body, httpMethod);
      default:
         throw new UnknownHttpMethodException("Unknown HTTP method name: " + httpMethod);
      }
   }

   public static okhttp3.Request okhttpRequest(HttpMethodName httpMethod, String ak, String sk, String requestUrl, Map<String, String> headers, String body) throws Exception {
      Client.ParamsEntity pe = new Client.ParamsEntity(ak, sk, requestUrl, body, "SDK-HMAC-SHA256");
      return okhttpRequest(httpMethod, headers, pe);
   }

   private static class ParamsEntity {
      String appKey;
      String secretKeyk;
      String requestUrl;
      String body;
      String algorithm;

      public ParamsEntity(String ak, String sk, String url, String bd, String algo) {
         this.appKey = ak;
         this.secretKeyk = sk;
         this.requestUrl = url;
         this.body = bd;
         this.algorithm = algo;
      }
   }
}
