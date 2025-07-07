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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;

import com.otc.sdk.core.auth.signer.Signer;
import com.otc.sdk.core.http.HttpMethodName;

public class AccessServiceImpl extends AccessService {
   private static final String UTF8 = "UTF-8";
   private static final String CHAR_SET_NAME_ISO = "ISO-8859-1";

   public AccessServiceImpl(String ak, String sk) {
      super(ak, sk);
   }

   public AccessServiceImpl(String ak, String sk, String messageDigestAlgorithm) {
      super(ak, sk, messageDigestAlgorithm);
   }

   public HttpRequestBase access(String url, Map<String, String> headers, String content, HttpMethodName httpMethod) throws Exception {
      Request request = new Request();
      request.setAppKey(this.ak);
      request.setAppSecrect(this.sk);
      request.setMethod(httpMethod.name());
      request.setUrl(url);
      Iterator var6 = headers.entrySet().iterator();

      while(var6.hasNext()) {
         Entry<String, String> map = (Entry)var6.next();
         request.addHeader((String)map.getKey(), (String)map.getValue());
      }

      request.setBody(content);
      Signer signer = new Signer(this.messageDigestAlgorithm);
      signer.sign(request);
      HttpRequestBase httpRequestBase = createRequest(url, (Header)null, content, httpMethod);
      Iterator var8 = request.getHeaders().entrySet().iterator();

      while(var8.hasNext()) {
         Entry<String, String> map = (Entry)var8.next();
         if (map.getKey() != null && !((String)map.getKey()).equalsIgnoreCase("Content-Length") && map.getValue() != null) {
            httpRequestBase.addHeader((String)map.getKey(), new String(((String)map.getValue()).getBytes(UTF8), CHAR_SET_NAME_ISO));
         }
      }

      return httpRequestBase;
   }

   public HttpRequestBase access(String url, Map<String, String> headers, InputStream content, Long contentLength, HttpMethodName httpMethod) throws Exception {
      String body = "";
      if (content != null) {
         ByteArrayOutputStream result = new ByteArrayOutputStream();
         byte[] buffer = new byte[1024];

         int length;
         while((length = content.read(buffer)) != -1) {
            result.write(buffer, 0, length);
         }

         body = result.toString(UTF8);
      }

      return this.access(url, headers, body, httpMethod);
   }

   private static HttpRequestBase createRequest(String url, Header header, String content, HttpMethodName httpMethod) {
      Object httpRequest;
      StringEntity entity;
      if (httpMethod == HttpMethodName.POST) {
         HttpPost postMethod = new HttpPost(url);
         if (content != null) {
            entity = new StringEntity(content, StandardCharsets.UTF_8);
            postMethod.setEntity(entity);
         }

         httpRequest = postMethod;
      } else if (httpMethod == HttpMethodName.PUT) {
         HttpPut putMethod = new HttpPut(url);
         httpRequest = putMethod;
         if (content != null) {
            entity = new StringEntity(content, StandardCharsets.UTF_8);
            putMethod.setEntity(entity);
         }
      } else if (httpMethod == HttpMethodName.PATCH) {
         HttpPatch patchMethod = new HttpPatch(url);
         httpRequest = patchMethod;
         if (content != null) {
            entity = new StringEntity(content, StandardCharsets.UTF_8);
            patchMethod.setEntity(entity);
         }
      } else if (httpMethod == HttpMethodName.GET) {
         httpRequest = new HttpGet(url);
      } else if (httpMethod == HttpMethodName.DELETE) {
         httpRequest = new HttpDelete(url);
      } else if (httpMethod == HttpMethodName.OPTIONS) {
         httpRequest = new HttpOptions(url);
      } else {
         if (httpMethod != HttpMethodName.HEAD) {
            throw new UnknownHttpMethodException("Unknown HTTP method name: " + httpMethod);
         }

         httpRequest = new HttpHead(url);
      }

      ((HttpRequestBase)httpRequest).addHeader(header);
      return (HttpRequestBase)httpRequest;
   }
}
