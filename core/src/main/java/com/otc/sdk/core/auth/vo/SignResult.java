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

package com.otc.sdk.core.auth.vo;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * SignResult class represents the result of a signing operation.
 * It contains headers, URL, parameters, and an input stream associated with the
 * signed request.
 */
public class SignResult {
  private Map<String, String> headers = new HashMap<>();
  private URL url;
  private Map<String, String> parameters = new HashMap<>();
  private InputStream inputStream;

  /**
   * Gets the headers of the signed request.
   *
   * @return The headers of the signed request
   */
  public Map<String, String> getHeaders() {
    return this.headers;
  }

  /**
   * Sets the headers for the signed request.
   *
   * @param headers The headers to set
   */
  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  /**
   * Gets the URL of the signed request.
   *
   * @return The URL of the signed request
   */
  public URL getUrl() {
    return this.url;
  }

  /**
   * Sets the URL for the signed request.
   *
   * @param url The URL to set
   */
  public void setUrl(URL url) {
    this.url = url;
  }

  /**
   * Gets the parameters of the signed request.
   *
   * @return The parameters of the signed request
   */
  public Map<String, String> getParameters() {
    return this.parameters;
  }

  /**
   * Sets the parameters for the signed request.
   *
   * @param parameters The parameters to set
   */
  public void setParameters(Map<String, String> parameters) {
    this.parameters = parameters;
  }

  /**
   * Gets the input stream associated with the signed request.
   *
   * @return The input stream of the signed request
   */
  public InputStream getInputStream() {
    return this.inputStream;
  }

  /**
   * Sets the input stream for the signed request.
   *
   * @param inputStream The input stream to set
   */
  public void setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

}
