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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.otc.sdk.core.auth.signer.Signer;
import com.otc.sdk.core.http.HttpMethodName;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Request.Builder;

/**
 * Implementation of the AccessServiceOkhttp that provides methods to access
 * resources with authentication.
 * It extends the abstract AccessServiceOkhttp class and implements the access
 * methods for HTTP requests.
 */
public class AccessServiceOkhttpImpl extends AccessServiceOkhttp {
  private static final String UTF8 = "UTF-8";
  private static final String OPTIONS = "OPTIONS";

  /**
   * Constructor to initialize the AccessServiceOkhttpImpl with access key and
   * secret key.
   *
   * @param ak Access Key
   * @param sk Secret Key
   */
  public AccessServiceOkhttpImpl(String ak, String sk) {
    super(ak, sk);
  }

  /**
   * Constructor to initialize the AccessServiceOkhttpImpl with access key, secret
   * key, and message digest algorithm.
   *
   * @param ak                     Access Key
   * @param sk                     Secret Key
   * @param messageDigestAlgorithm Message Digest Algorithm (default is
   *                               "SDK-HMAC-SHA256")
   */
  public AccessServiceOkhttpImpl(String ak, String sk, String messageDigestAlgorithm) {
    super(ak, sk, messageDigestAlgorithm);
  }

  /**
   * Access a resource with the specified parameters.
   *
   * @param url        The URL of the resource
   * @param headers    The headers to include in the request
   * @param entity     The entity to send in the request body (can be null)
   * @param httpMethod The HTTP method to use for the request
   * @return An okhttp3.Request object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public okhttp3.Request access(String url, Map<String, String> headers, String entity, HttpMethodName httpMethod)
      throws Exception {
    Request request = new Request();
    request.setAppKey(this.ak);
    request.setAppSecrect(this.sk);
    request.setMethod(httpMethod.name());
    request.setUrl(url);
    Iterator var6 = headers.entrySet().iterator();

    while (var6.hasNext()) {
      Entry<String, String> map = (Entry) var6.next();
      request.addHeader((String) map.getKey(), (String) map.getValue());
    }

    request.setBody(entity);
    Signer signer = new Signer(this.messageDigestAlgorithm);
    signer.sign(request);
    return createRequest(url, request.getHeaders(), entity, httpMethod);
  }

  /**
   * Access a resource with the specified URL, headers, content, and HTTP method.
   *
   * @param url           URL of the resource
   * @param headers       Map of headers to include in the request
   * @param content       InputStream content to send with the request
   * @param contentLength Length of the content
   * @param httpMethod    HTTP method to use (GET, POST, etc.)
   * @return An okhttp3.Request object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public okhttp3.Request access(String url, Map<String, String> headers, InputStream content, Long contentLength,
      HttpMethodName httpMethod) throws Exception {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];

    int length;
    while ((length = content.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }

    String body = result.toString(UTF8);
    return this.access(url, headers, body, httpMethod);
  }

  /**
   * Create an okhttp3.Request object based on the provided parameters.
   *
   * @param url        URL of the resource
   * @param headers    Map of headers to include in the request
   * @param body       Body of the request (can be null)
   * @param httpMethod HTTP method to use (GET, POST, etc.)
   * @return An okhttp3.Request object representing the HTTP request
   * @throws Exception if an error occurs during request creation
   */
  private static okhttp3.Request createRequest(String url, Map<String, String> headers, String body,
      HttpMethodName httpMethod) throws Exception {
    if (body == null) {
      body = "";
    }

    RequestBody entity = RequestBody.create(MediaType.parse(""), body.getBytes(UTF8));
    okhttp3.Request httpRequest;
    if (httpMethod == HttpMethodName.POST) {
      httpRequest = (new Builder()).url(url).post(entity).build();
    } else if (httpMethod == HttpMethodName.PUT) {
      httpRequest = (new Builder()).url(url).put(entity).build();
    } else if (httpMethod == HttpMethodName.PATCH) {
      httpRequest = (new Builder()).url(url).patch(entity).build();
    } else if (httpMethod == HttpMethodName.DELETE) {
      httpRequest = (new Builder()).url(url).delete(entity).build();
    } else if (httpMethod == HttpMethodName.GET) {
      httpRequest = (new Builder()).url(url).get().build();
    } else if (httpMethod == HttpMethodName.HEAD) {
      httpRequest = (new Builder()).url(url).head().build();
    } else {
      if (httpMethod != HttpMethodName.OPTIONS) {
        throw new UnknownHttpMethodException("Unknown HTTP method name: " + httpMethod);
      }

      httpRequest = (new Builder()).url(url).method(OPTIONS, (RequestBody) null).build();
    }

    Entry<String, String> map;
    for (Iterator<Entry<String, String>> var6 = headers.entrySet().iterator(); var6
        .hasNext(); httpRequest = httpRequest.newBuilder()
            .addHeader(map.getKey(), map.getValue()).build()) {
      map = var6.next();
    }

    return httpRequest;
  }
}
