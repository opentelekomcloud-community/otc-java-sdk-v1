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

/**
 * Client class that provides methods to sign requests and access resources
 * using the AccessService.
 * It supports both Apache HttpClient and OkHttp implementations.
 */
public class Client {
  /**
   * Signs a request using the specified message digest algorithm.
   *
   * @param request                The request to be signed
   * @param messageDigestAlgorithm The algorithm to use for signing (default is
   *                               "SDK-HMAC-SHA256")
   * @return The signed HttpRequestBase
   * @throws Exception if an error occurs during signing
   */
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
    switch (request.getMethod()) {
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

  /**
   * Signs a request using the default message digest algorithm "SDK-HMAC-SHA256".
   *
   * @param request The request to be signed
   * @return The signed HttpRequestBase
   * @throws Exception if an error occurs during signing
   */
  public static synchronized HttpRequestBase sign(Request request) throws Exception {
    return sign(request, "SDK-HMAC-SHA256");
  }

  /**
   * Signs an OkHttp request using the specified message digest algorithm.
   *
   * @param request                The request to be signed
   * @param messageDigestAlgorithm The algorithm to use for signing (default is
   *                               "SDK-HMAC-SHA256")
   * @return The signed okhttp3.Request
   * @throws Exception if an error occurs during signing
   */
  public static okhttp3.Request signOkhttp(Request request, String messageDigestAlgorithm) throws Exception {
    Client.ParamsEntity pe = new Client.ParamsEntity(request.getKey(), request.getSecrect(), request.getUrl(),
        request.getBody(), messageDigestAlgorithm);
    return okhttpRequest(request.getMethod(), request.getHeaders(), pe);
  }

  /**
   * Signs an OkHttp request using the default message digest algorithm
   * "SDK-HMAC-SHA256".
   *
   * @param request The request to be signed
   * @return The signed okhttp3.Request
   * @throws Exception if an error occurs during signing
   */
  public static okhttp3.Request signOkhttp(Request request) throws Exception {
    return okhttpRequest(request.getMethod(), request.getKey(), request.getSecrect(), request.getUrl(),
        request.getHeaders(), request.getBody());
  }

  /**
   * Access a resource with the specified URL, headers, and HTTP method.
   *
   * @param ak         Access Key
   * @param sk         Secret Key
   * @param requestUrl URL of the resource
   * @param headers    Map of headers to include in the request
   * @return HttpRequestBase object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public static HttpRequestBase put(String ak, String sk, String requestUrl, Map<String, String> headers,
      String putBody) throws Exception {
    if (putBody == null) {
      putBody = "";
    }

    AccessService accessService = new AccessServiceImpl(ak, sk);
    return accessService.access(requestUrl, headers, putBody, HttpMethodName.PUT);
  }

  /**
   * Access a resource with the specified URL, headers, and HTTP method.
   *
   * @param ak         Access Key
   * @param sk         Secret Key
   * @param requestUrl URL of the resource
   * @param headers    Map of headers to include in the request
   * @return HttpRequestBase object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public static HttpRequestBase patch(String ak, String sk, String requestUrl, Map<String, String> headers, String body)
      throws Exception {
    if (body == null) {
      body = "";
    }

    AccessService accessService = new AccessServiceImpl(ak, sk);
    return accessService.access(requestUrl, headers, body, HttpMethodName.PATCH);
  }

  /**
   * Access a resource with the specified URL, headers, and HTTP method.
   *
   * @param ak         Access Key
   * @param sk         Secret Key
   * @param requestUrl URL of the resource
   * @param headers    Map of headers to include in the request
   * @return HttpRequestBase object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public static HttpRequestBase delete(String ak, String sk, String requestUrl, Map<String, String> headers)
      throws Exception {
    AccessService accessService = new AccessServiceImpl(ak, sk);
    return accessService.access(requestUrl, headers, HttpMethodName.DELETE);
  }

  /**
   * Access a resource with the specified URL, headers, and HTTP method.
   *
   * @param ak         Access Key
   * @param sk         Secret Key
   * @param requestUrl URL of the resource
   * @param headers    Map of headers to include in the request
   * @return HttpRequestBase object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public static HttpRequestBase get(String ak, String sk, String requestUrl, Map<String, String> headers)
      throws Exception {
    AccessService accessService = new AccessServiceImpl(ak, sk);
    return accessService.access(requestUrl, headers, HttpMethodName.GET);
  }

  /**
   * Access a resource with the specified URL, headers, and HTTP method.
   *
   * @param ak         Access Key
   * @param sk         Secret Key
   * @param requestUrl URL of the resource
   * @param headers    Map of headers to include in the request
   * @return HttpRequestBase object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public static HttpRequestBase post(String ak, String sk, String requestUrl, Map<String, String> headers,
      String postbody) throws Exception {
    if (postbody == null) {
      postbody = "";
    }

    AccessService accessService = new AccessServiceImpl(ak, sk);
    return accessService.access(requestUrl, headers, postbody, HttpMethodName.POST);
  }

  /**
   * Access a resource with the specified URL, headers, and HTTP method.
   *
   * @param ak         Access Key
   * @param sk         Secret Key
   * @param requestUrl URL of the resource
   * @param headers    Map of headers to include in the request
   * @return HttpRequestBase object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public static HttpRequestBase head(String ak, String sk, String requestUrl, Map<String, String> headers)
      throws Exception {
    AccessService accessService = new AccessServiceImpl(ak, sk);
    return accessService.access(requestUrl, headers, HttpMethodName.HEAD);
  }

  /**
   * Access a resource with the specified URL, headers, and HTTP method.
   *
   * @param ak         Access Key
   * @param sk         Secret Key
   * @param requestUrl URL of the resource
   * @param headers    Map of headers to include in the request
   * @return HttpRequestBase object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public static HttpRequestBase options(String ak, String sk, String requestUrl, Map<String, String> headers)
      throws Exception {
    AccessService accessService = new AccessServiceImpl(ak, sk);
    return accessService.access(requestUrl, headers, HttpMethodName.OPTIONS);
  }

  /**
   * Create an okhttp3.Request object based on the provided parameters.
   * 
   * @param httpMethod HTTP method to use (GET, POST, etc.)
   * @param headers Map of headers to include in the request
   * @param pe Parameters entity containing access key, secret key, request URL, body, and algorithm
   * @return An okhttp3.Request object representing the HTTP request
   * @throws Exception if an error occurs during request creation
   */
  private static okhttp3.Request okhttpRequest(HttpMethodName httpMethod, Map<String, String> headers,
      Client.ParamsEntity pe) throws Exception {
    switch (httpMethod) {
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

  /**
   * Create an okhttp3.Request object based on the provided parameters.
   *
   * @param httpMethod HTTP method to use (GET, POST, etc.)
   * @param ak         Access Key
   * @param sk         Secret Key
   * @param requestUrl URL of the resource
   * @param headers    Map of headers to include in the request
   * @param body       Body of the request (can be null)
   * @return An okhttp3.Request object representing the HTTP request
   * @throws Exception if an error occurs during request creation
   */
  public static okhttp3.Request okhttpRequest(HttpMethodName httpMethod, String ak, String sk, String requestUrl,
      Map<String, String> headers, String body) throws Exception {
    Client.ParamsEntity pe = new Client.ParamsEntity(ak, sk, requestUrl, body, "SDK-HMAC-SHA256");
    return okhttpRequest(httpMethod, headers, pe);
  }

  /**
   * Parameters entity class to hold access key, secret key, request URL, body,
   * and algorithm.
   */
  private static class ParamsEntity {
    String appKey;
    String secretKeyk;
    String requestUrl;
    String body;
    String algorithm;

    /**
     * Constructor to initialize the ParamsEntity with access key, secret key,
     * request URL, body, and algorithm.
     *
     * @param ak   Access Key
     * @param sk   Secret Key
     * @param url  URL of the resource
     * @param bd   Body of the request
     * @param algo Algorithm to use for signing (default is "SDK-HMAC-SHA256")
     */
    public ParamsEntity(String ak, String sk, String url, String bd, String algo) {
      this.appKey = ak;
      this.secretKeyk = sk;
      this.requestUrl = url;
      this.body = bd;
      this.algorithm = algo;
    }
  }
}
