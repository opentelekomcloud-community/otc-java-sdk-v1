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

import java.io.InputStream;
import java.util.Map;
import org.apache.http.client.methods.HttpRequestBase;

import com.otc.sdk.core.http.HttpMethodName;

/**
 * Abstract class for access services that handle HTTP requests with
 * authentication.
 * It provides methods to access resources with different parameters and
 * configurations.
 */
public abstract class AccessService {
  protected String ak;
  protected String sk;
  protected String messageDigestAlgorithm = "SDK-HMAC-SHA256";

  /**
   * Constructor to initialize the AccessService with access key and secret key.
   *
   * @param ak Access Key
   * @param sk Secret Key
   */
  public AccessService(String ak, String sk) {
    this.ak = ak;
    this.sk = sk;
  }

  /**
   * Constructor to initialize the AccessService with access key, secret key, and
   * message digest algorithm.
   *
   * @param ak                     Access Key
   * @param sk                     Secret Key
   * @param messageDigestAlgorithm Message Digest Algorithm (default is
   *                               "SDK-HMAC-SHA256")
   */
  public AccessService(String ak, String sk, String messageDigestAlgorithm) {
    this.ak = ak;
    this.sk = sk;
    this.messageDigestAlgorithm = messageDigestAlgorithm;
  }

  /**
   * Abstract method to access a resource with the specified parameters.
   *
   * @param url           URL of the resource
   * @param header        Map of headers to include in the request
   * @param content       InputStream content to send with the request
   * @param contentLength Length of the content
   * @param httpMethod    HTTP method to use (GET, POST, etc.)
   * @return HttpRequestBase object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public abstract HttpRequestBase access(String url, Map<String, String> header, InputStream content, Long contentLength,
      HttpMethodName httpMethod) throws Exception;

  /**
   * Abstract method to access a resource with the specified parameters.
   *
   * @param url           URL of the resource
   * @param header        Map of headers to include in the request
   * @param content       InputStream content to send with the request   
   * @param httpMethod    HTTP method to use (GET, POST, etc.)
   * @throws Exception if an error occurs during access
   */
  public abstract HttpRequestBase access(String url, Map<String, String> header, String content, HttpMethodName httpMethod)
      throws Exception;

  /**
   * Access a resource with the specified URL, headers, and HTTP method.
   *
   * @param url        URL of the resource
   * @param header     Map of headers to include in the request
   * @param httpMethod HTTP method to use (GET, POST, etc.)
   * @return HttpRequestBase object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public HttpRequestBase access(String url, Map<String, String> header, HttpMethodName httpMethod) throws Exception {
    return this.access(url, header, (InputStream) null, 0L, httpMethod);
  }

  /**
   * Access a resource with the specified URL, content, content length, and HTTP
   * method.
   *
   * @param url           URL of the resource
   * @param content       InputStream content to send with the request
   * @param contentLength Length of the content
   * @param httpMethod    HTTP method to use (GET, POST, etc.)
   * @return HttpRequestBase object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public HttpRequestBase access(String url, InputStream content, Long contentLength, HttpMethodName httpMethod)
      throws Exception {
    return this.access(url, (Map<String, String>) null, content, contentLength, httpMethod);
  }

  /**
   * Access a resource with the specified URL and HTTP method.
   *
   * @param url        URL of the resource
   * @param httpMethod HTTP method to use (GET, POST, etc.)
   * @return HttpRequestBase object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public HttpRequestBase access(String url, HttpMethodName httpMethod) throws Exception {
    return this.access(url, (Map<String, String>) null, (InputStream) null, 0L, httpMethod);
  }

  /**
   * Get the access key.
   *
   * @return Access Key
   */
  public String getAk() {
    return this.ak;
  }

  /**
   * Set the access key.
   *
   * @param ak Access Key
   */
  public void setAk(String ak) {
    this.ak = ak;
  }

  /**
   * Get the secret key.
   *
   * @return Secret Key
   */
  public String getSk() {
    return this.sk;
  }

  /**
   * Set the secret key.
   *
   * @param sk Secret Key
   */
  public void setSk(String sk) {
    this.sk = sk;
  }
}
