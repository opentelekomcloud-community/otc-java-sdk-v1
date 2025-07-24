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

import com.otc.sdk.core.http.HttpMethodName;
import java.util.Map;

/**
 * Abstract class for access services that handle HTTP requests with
 * authentication.
 * It provides methods to access resources with different parameters and
 * configurations.
 */
public abstract class AccessServiceOkhttp {
  protected String ak;
  protected String sk;
  protected String messageDigestAlgorithm = "SDK-HMAC-SHA256";

  /**
   * Constructor to initialize the AccessServiceOkhttp with access key and secret
   * key.
   *
   * @param ak Access Key
   * @param sk Secret Key
   */
  public AccessServiceOkhttp(String ak, String sk) {
    this.ak = ak;
    this.sk = sk;
  }

  /**
   * Constructor to initialize the AccessServiceOkhttp with access key, secret
   * key, and message digest algorithm.
   *
   * @param ak                     Access Key
   * @param sk                     Secret Key
   * @param messageDigestAlgorithm Message Digest Algorithm (default is
   *                               "SDK-HMAC-SHA256")
   */
  public AccessServiceOkhttp(String ak, String sk, String messageDigestAlgorithm) {
    this.ak = ak;
    this.sk = sk;
    this.messageDigestAlgorithm = messageDigestAlgorithm;
  }

  /**
   * Abstract method to access a resource with the specified parameters.
   *
   * @param url        The URL of the resource
   * @param header     The headers to include in the request
   * @param entity     The entity to send in the request body (can be null)
   * @param httpMethod The HTTP method to use for the request
   * @return A Request object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public abstract okhttp3.Request access(String url, Map<String, String> header, String entity,
      HttpMethodName httpMethod)
      throws Exception;

  /**
   * Access a resource with the specified URL, headers, and HTTP method.
   *
   * @param url        URL of the resource
   * @param header     Map of headers to include in the request
   * @param httpMethod HTTP method to use (GET, POST, etc.)
   * @return A Request object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public okhttp3.Request access(String url, Map<String, String> header, HttpMethodName httpMethod) throws Exception {
    return this.access(url, header, (String) null, httpMethod);
  }

  /**
   * Access a resource with the specified URL, headers, and entity.
   *
   * @param url        URL of the resource
   * @param entity     Entity to send in the request body (can be null)
   * @param httpMethod HTTP method to use (GET, POST, etc.)
   * @return A Request object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public okhttp3.Request access(String url, String entity, HttpMethodName httpMethod) throws Exception {
    return this.access(url, (Map<String, String>) null, entity, httpMethod);
  }

  /**
   * Access a resource with the specified URL and HTTP method.
   *
   * @param url        URL of the resource
   * @param httpMethod HTTP method to use (GET, POST, etc.)
   * @return A Request object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  public okhttp3.Request access(String url, HttpMethodName httpMethod) throws Exception {
    return this.access(url, (Map<String, String>) null, (String) null, httpMethod);
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
