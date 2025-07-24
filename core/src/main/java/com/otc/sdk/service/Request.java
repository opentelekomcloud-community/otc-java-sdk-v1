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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.otc.sdk.core.http.HttpMethodName;
import com.otc.sdk.core.util.HttpUtils;

/**
 * Represents a request to be made to an API.
 * It contains details such as the key, secret, method, URL, body, headers,
 * query parameters, and fragment.
 */
public class Request {
  private String key = null;
  private String secret = null;
  private String method = null;
  private String url = null;
  private String body = null;
  private String fragment = null;
  private Map<String, String> headers = new Hashtable<>();
  private Map<String, List<String>> queryString = new Hashtable<>();
  private static final Pattern PATTERN = Pattern.compile("^(?i)(post|put|patch|delete|get|options|head)$");

  /** @deprecated */
  @Deprecated
  public String getRegion() {
    return "";
  }

  /** @deprecated */
  @Deprecated
  public String getServiceName() {
    return "";
  }

  /**
   * Default constructor for Request.
   * Initializes the request with default values.
   */
  public String getKey() {
    return this.key;
  }

  /**
   * Returns the secret associated with the request.
   *
   * @return The secret as a String
   */
  public String getSecrect() {
    return this.secret;
  }

  /**
   * Returns the method used for the request.
   *
   * @return The HTTP method as a String
   */
  public HttpMethodName getMethod() {
    return HttpMethodName.valueOf(this.method.toUpperCase(Locale.getDefault()));
  }

  /**
   * Returns the body of the request.
   *
   * @return The body as a String
   */
  public String getBody() {
    return this.body;
  }

  /**
   * Returns the headers of the request.
   *
   * @return A Map containing the headers
   */
  public Map<String, String> getHeaders() {
    return this.headers;
  }

  /** @deprecated */
  @Deprecated
  public void setRegion(String region) {
  }

  /** @deprecated */
  @Deprecated
  public void setServiceName(String serviceName) {
  }

  /**
   * Sets the access key for the request.
   *
   * @param appKey The access key to set
   * @throws EmptyStringException if the appKey is empty
   */
  public void setAppKey(String appKey) throws EmptyStringException {
    if (null != appKey && !appKey.trim().isEmpty()) {
      this.key = appKey;
    } else {
      throw new EmptyStringException("appKey can not be empty");
    }
  }

  /**
   * Sets the secret key for the request.
   *
   * @param appSecret The secret key to set
   * @throws EmptyStringException if the appSecret is empty
   */
  public void setAppSecrect(String appSecret) throws EmptyStringException {
    if (null != appSecret && !appSecret.trim().isEmpty()) {
      this.secret = appSecret;
    } else {
      throw new EmptyStringException("appSecrect can not be empty");
    }
  }

  
  public void setKey(String appKey) throws EmptyStringException {
    if (null != appKey && !appKey.trim().isEmpty()) {
      this.key = appKey;
    } else {
      throw new EmptyStringException("appKey can not be empty");
    }
  }

  /**
   * Sets the secret key for the request.
   *
   * @param appSecret The secret key to set
   * @throws EmptyStringException if the appSecret is empty
   */
  public void setSecret(String appSecret) throws EmptyStringException {
    if (null != appSecret && !appSecret.trim().isEmpty()) {
      this.secret = appSecret;
    } else {
      throw new EmptyStringException("appSecrect can not be empty");
    }
  }

  /**
   * Sets the HTTP method for the request.
   *
   * @param method The HTTP method to set
   * @throws EmptyStringException if the method is empty or unsupported
   */
  public void setMethod(String method) throws EmptyStringException {
    if (null == method) {
      throw new EmptyStringException("method can not be empty");
    } else {
      Matcher match = PATTERN.matcher(method);
      if (!match.matches()) {
        throw new EmptyStringException("unsupported method");
      } else {
        this.method = method;
      }
    }
  }

  
  public String getUrl() throws UnsupportedEncodingException {
    StringBuilder uri = new StringBuilder();
    uri.append(this.url);
    if (this.queryString.size() > 0) {
      uri.append("?");
      int loop = 0;
      Iterator var3 = this.queryString.entrySet().iterator();

      while (var3.hasNext()) {
        Entry<String, List<String>> entry = (Entry) var3.next();

        for (Iterator var5 = ((List) entry.getValue()).iterator(); var5.hasNext(); ++loop) {
          String value = (String) var5.next();
          if (loop > 0) {
            uri.append("&");
          }

          uri.append(HttpUtils.urlEncode((String) entry.getKey(), false));
          uri.append("=");
          uri.append(HttpUtils.urlEncode(value, false));
        }
      }
    }

    if (this.fragment != null) {
      uri.append("#");
      uri.append(this.fragment);
    }

    return uri.toString();
  }

  /**
   * Sets the URL for the request.
   * 
   * @param urlRet The URL to set
   * @throws EmptyStringException         if the URL is empty
   * @throws UnsupportedEncodingException if the URL encoding fails
   */
  public void setUrl(String urlRet) throws EmptyStringException, UnsupportedEncodingException {
    if (urlRet != null && !urlRet.trim().isEmpty()) {
      int i = urlRet.indexOf(35);
      if (i >= 0) {
        urlRet = urlRet.substring(0, i);
      }

      i = urlRet.indexOf(63);
      this.url = urlRet;
      if (i >= 0) {
        String query = urlRet.substring(i + 1, urlRet.length());
        String[] var4 = query.split("&");
        int var5 = var4.length;

        for (int var6 = 0; var6 < var5; ++var6) {
          String item = var4[var6];
          String[] spl = item.split("=", 2);
          String keyRet = spl[0];
          String value = "";
          if (spl.length > 1) {
            value = spl[1];
          }

          if (!keyRet.trim().isEmpty()) {
            keyRet = URLDecoder.decode(keyRet, "UTF-8");
            value = URLDecoder.decode(value, "UTF-8");
            this.addQueryStringParam(keyRet, value);
          }
        }

        urlRet = urlRet.substring(0, i);
        this.url = urlRet;
      }
    } else {
      throw new EmptyStringException("url can not be empty");
    }
  }

  /**
   * Returns the path of the URL.
   * 
   * @return The path as a String
   */
  public String getPath() {
    String urlRet = this.url;
    int i = urlRet.indexOf("://");
    if (i >= 0) {
      urlRet = urlRet.substring(i + 3);
    }

    i = urlRet.indexOf(47);
    return i >= 0 ? urlRet.substring(i) : "/";
  }

  /**
   * Returns the host of the URL.
   * 
   * @return The host as a String
   */
  public String getHost() {
    String urlRet = this.url;
    int i = urlRet.indexOf("://");
    if (i >= 0) {
      urlRet = urlRet.substring(i + 3);
    }

    i = urlRet.indexOf(47);
    if (i >= 0) {
      urlRet = urlRet.substring(0, i);
    }

    return urlRet;
  }

  /**
   * Sets the body of the request.
   * 
   * @param body The body to set
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * Adds a query string parameter to the request.
   * 
   * @param name  The name of the query string parameter
   * @param value The value of the query string parameter
   * @throws EmptyStringException if the name is empty
   */
  public void addQueryStringParam(String name, String value) {
    List<String> paramList = (List) this.queryString.get(name);
    if (paramList == null) {
      paramList = new ArrayList();
      this.queryString.put(name, paramList);
    }

    ((List) paramList).add(value);
  }

  /**
   * Returns the query string parameters of the request.
   * 
   * @return A Map containing the query string parameters
   */
  public Map<String, List<String>> getQueryStringParams() {
    return this.queryString;
  }

  /**
   * Returns the fragment of the URL.
   * 
   * @return The fragment as a String
   */
  public String getFragment() {
    return this.fragment;
  }

  /**
   * Sets the fragment of the URL.
   * 
   * @param fragment The fragment to set
   * @throws EmptyStringException         if the fragment is empty
   * @throws UnsupportedEncodingException if the fragment encoding fails
   */
  public void setFragment(String fragment) throws EmptyStringException, UnsupportedEncodingException {
    if (fragment != null && !fragment.trim().isEmpty()) {
      this.fragment = URLEncoder.encode(fragment, "UTF-8");
    } else {
      throw new EmptyStringException("fragment can not be empty");
    }
  }

  /**
   * Adds a header to the request.
   * 
   * @param name  The name of the header
   * @param value The value of the header
   * @throws EmptyStringException if the name is empty
   */
  public void addHeader(String name, String value) {
    if (name != null && !name.trim().isEmpty()) {
      this.headers.put(name, value);
    }
  }
}
