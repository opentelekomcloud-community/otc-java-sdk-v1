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

package com.otc.sdk.core.util;

import okhttp3.OkHttpClient;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.prng.SP800SecureRandomBuilder;
import org.openeuler.BGMProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.*;

/* * Utility class for creating SSL connections with various configurations.
 * It supports both HTTP and HTTPS protocols, with options for SSL verification.
 */
public class SSLCipherSuiteUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(SSLCipherSuiteUtil.class);
  private static CloseableHttpClient httpClient;
  private static OkHttpClient okHttpClient;

  private static final int CIPHER_LEN = 256;

  private static final int ENTROPY_BITS_REQUIRED = 384;

  /**
   * Creates an HTTP client with the specified SSL protocol.
   *
   * @param protocol The SSL protocol to use (e.g., "GMTLS", "TLSv1.2")
   * @return An instance of HttpClient configured with the specified protocol
   * @throws Exception If an error occurs while creating the SSL context
   */
  public static HttpClient createHttpClient(String protocol) throws Exception {
    SSLContext sslContext = getSslContext(protocol);
    // create factory
    SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
        new String[] { protocol }, Constant.SUPPORTED_CIPHER_SUITES, new TrustAllHostnameVerifier());

    httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
    return httpClient;
  }

  /**
   * Creates an HTTP client with SSL verification enabled for the specified
   * protocol.
   *
   * @param protocol The SSL protocol to use (e.g., "GMTLS", "TLSv1.2")
   * @return An instance of HttpClient configured with SSL verification
   * @throws Exception If an error occurs while creating the SSL context
   */
  public static HttpClient createHttpClientWithVerify(String protocol) throws Exception {
    SSLContext sslContext = getSslContextWithVerify(protocol);
    // create factory
    SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
        new String[] { protocol }, Constant.SUPPORTED_CIPHER_SUITES, new TheRealHostnameVerifier());

    httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
    return httpClient;
  }

  /**
   * Creates an OkHttpClient with the specified SSL protocol.
   *
   * @param protocol The SSL protocol to use (e.g., "GMTLS", "TLSv1.2")
   * @return An instance of OkHttpClient configured with the specified protocol
   * @throws Exception If an error occurs while creating the SSL context
   */
  public static OkHttpClient createOkHttpClient(String protocol) throws Exception {
    SSLContext sslContext = getSslContext(protocol);
    // Create an ssl socket factory with our all-trusting manager
    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
    OkHttpClient.Builder builder = new OkHttpClient.Builder()
        .sslSocketFactory(sslSocketFactory, new TrustAllManager())
        .hostnameVerifier(new TrustAllHostnameVerifier());
    okHttpClient = builder.connectTimeout(10, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build();
    return okHttpClient;
  }

  /**
   * Creates an OkHttpClient with SSL verification enabled for the specified
   * protocol.
   *
   * @param protocol The SSL protocol to use (e.g., "GMTLS", "TLSv1.2")
   * @return An instance of OkHttpClient configured with SSL verification
   * @throws Exception If an error occurs while creating the SSL context
   */
  public static OkHttpClient createOkHttpClientWithVerify(String protocol) throws Exception {
    SSLContext sslContext = getSslContextWithVerify(protocol);
    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

    TrustManagerFactory tmf = TrustManagerFactory.getInstance(Constant.TRUST_MANAGER_FACTORY);
    tmf.init((KeyStore) null);
    TrustManager[] verify = tmf.getTrustManagers();
    OkHttpClient.Builder builder = new OkHttpClient.Builder().sslSocketFactory(sslSocketFactory,
        (X509TrustManager) verify[0]).hostnameVerifier(new TheRealHostnameVerifier());

    okHttpClient = builder.connectTimeout(10, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build();
    return okHttpClient;
  }

  /**
   * Creates an HttpURLConnection for the specified URL and protocol.
   *
   * @param uUrl     The URL to connect to
   * @param protocol The SSL protocol to use (e.g., "GMTLS", "TLSv1.2")
   * @return An HttpURLConnection object for the specified URL and protocol
   * @throws Exception If an error occurs while creating the connection
   */
  public static HttpURLConnection createHttpsOrHttpURLConnection(URL uUrl, String protocol) throws Exception {
    // initial connection
    if (uUrl.getProtocol().toUpperCase(Locale.getDefault()).equals(Constant.HTTPS)) {
      SSLContext sslContext = getSslContext(protocol);
      HttpsURLConnection.setDefaultHostnameVerifier(new TrustAllHostnameVerifier());
      HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
      return (HttpsURLConnection) uUrl.openConnection();
    }
    return (HttpURLConnection) uUrl.openConnection();
  }

  /**
   * Creates an HttpURLConnection for the specified URL and protocol with SSL
   * verification.
   *
   * @param uUrl     The URL to connect to
   * @param protocol The SSL protocol to use (e.g., "GMTLS", "TLSv1.2")
   * @return An HttpURLConnection object for the specified URL and protocol with
   *         SSL verification
   * @throws Exception If an error occurs while creating the connection
   */
  public static HttpURLConnection createHttpsOrHttpURLConnectionWithVerify(URL uUrl, String protocol) throws Exception {
    // initial connection
    if (uUrl.getProtocol().toUpperCase(Locale.getDefault()).equals(Constant.HTTPS)) {
      SSLContext sslContext = getSslContextWithVerify(protocol);
      HttpsURLConnection.setDefaultHostnameVerifier(new TheRealHostnameVerifier());
      HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
      return (HttpsURLConnection) uUrl.openConnection();
    }
    return (HttpURLConnection) uUrl.openConnection();
  }

  /**
   * Access a resource with the specified URL, headers, and HTTP method.
   *
   * @param url        URL of the resource
   * @param header     Map of headers to include in the request
   * @param httpMethod HTTP method to use (GET, POST, etc.)
   * @return HttpRequestBase object representing the HTTP request
   * @throws Exception if an error occurs during access
   */
  private static SSLContext getSslContext(String protocol) throws UnsupportProtocolException,
      NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
    if (!Constant.GM_PROTOCOL.equals(protocol) && !Constant.INTERNATIONAL_PROTOCOL.equals(protocol)) {
      LOGGER.info("Unsupport protocol: {}, Only support GMTLS TLSv1.2", protocol);
      throw new UnsupportProtocolException("Unsupport protocol, Only support GMTLS TLSv1.2");
    }
    // Create a trust manager that does not validate certificate chains
    TrustAllManager[] trust = { new TrustAllManager() };
    KeyManager[] kms = null;
    SSLContext sslContext;

    sslContext = SSLContext.getInstance(Constant.INTERNATIONAL_PROTOCOL, "SunJSSE");

    if (Constant.GM_PROTOCOL.equals(protocol)) {
      Security.insertProviderAt(new BGMProvider(), 1);
      sslContext = SSLContext.getInstance(Constant.GM_PROTOCOL, "BGMProvider");
    }
    SecureRandom secureRandom = getSecureRandom();
    sslContext.init(kms, trust, secureRandom);
    sslContext.getServerSessionContext().setSessionCacheSize(8192);
    sslContext.getServerSessionContext().setSessionTimeout(3600);
    return sslContext;
  }

  /**
   * Creates an SSL context with verification for the specified protocol.
   *
   * @param protocol The SSL protocol to use (e.g., "GMTLS", "TLSv1.2")
   * @return An instance of SSLContext configured with the specified protocol and
   *         verification
   * @throws UnsupportProtocolException If the protocol is not supported
   * @throws NoSuchAlgorithmException   If the specified algorithm is not
   *                                    available
   * @throws NoSuchProviderException    If the specified provider is not available
   * @throws KeyManagementException     If there is an error initializing the key
   *                                    management
   * @throws KeyStoreException          If there is an error initializing the key
   *                                    store
   */
  private static SSLContext getSslContextWithVerify(String protocol)
      throws UnsupportProtocolException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException,
      KeyStoreException {
    if (!Constant.GM_PROTOCOL.equals(protocol) && !Constant.INTERNATIONAL_PROTOCOL.equals(protocol)) {
      LOGGER.info("Unsupport protocol: {}, Only support GMTLS TLSv1.2", protocol);
      throw new UnsupportProtocolException("Unsupport protocol, Only support GMTLS TLSv1.2");
    }
    KeyManager[] kms = null;
    SSLContext sslContext = SSLContext.getInstance(Constant.INTERNATIONAL_PROTOCOL, "SunJSSE");
    SecureRandom secureRandom = getSecureRandom();

    if (Constant.GM_PROTOCOL.equals(protocol)) {
      Security.insertProviderAt(new BGMProvider(), 1);
      sslContext = SSLContext.getInstance(Constant.GM_PROTOCOL, "BGMProvider");
    }

    TrustManagerFactory tmf = TrustManagerFactory.getInstance(Constant.TRUST_MANAGER_FACTORY);
    tmf.init((KeyStore) null);
    TrustManager[] verify = tmf.getTrustManagers();
    sslContext.init(kms, verify, secureRandom);

    sslContext.getServerSessionContext().setSessionCacheSize(8192);
    sslContext.getServerSessionContext().setSessionTimeout(3600);
    return sslContext;
  }

  /**
   * HostnameVerifier that trusts all hostnames.
   * This is used to bypass hostname verification in SSL connections.
   */
  private static class TrustAllHostnameVerifier implements HostnameVerifier {
    public boolean verify(String hostname, SSLSession session) {
      return true;
    }
  }

  /**
   * HostnameVerifier that checks the hostname against a set URL host name.
   * If the hostname does not match, it falls back to the default hostname
   * verifier.
   */
  private static class TheRealHostnameVerifier implements HostnameVerifier {
    public boolean verify(String hostname, SSLSession session) {
      if (HostName.checkHostName(hostname)) {
        return true;
      } else {
        HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
        return hv.verify(hostname, session);
      }
    }
  }

  /**
   * TrustManager that trusts all certificates.
   * This is used to bypass certificate validation in SSL connections.
   */
  private static class TrustAllManager implements X509TrustManager {
    private X509Certificate[] issuers;

    public TrustAllManager() {
      this.issuers = new X509Certificate[0];
    }

    public X509Certificate[] getAcceptedIssuers() {
      return issuers;
    }

    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    public void checkServerTrusted(X509Certificate[] chain, String authType) {
    }
  }

  /**
   * Returns a SecureRandom instance with a specific algorithm.
   * If the algorithm is not available, it falls back to a strong SecureRandom
   * instance.
   *
   * @return An instance of SecureRandom
   */
  private static SecureRandom getSecureRandom() {
    SecureRandom source;
    try {
      source = SecureRandom.getInstance(Constant.SECURE_RANDOM_ALGORITHM_NATIVE_PRNG_NON_BLOCKING);
    } catch (NoSuchAlgorithmException e) {
      try {
        source = SecureRandom.getInstanceStrong();
      } catch (NoSuchAlgorithmException ex) {
        LOGGER.error("get SecureRandom failed", e);
        throw new RuntimeException("get SecureRandom failed");
      }
    }
    boolean predictionResistant = true;
    BlockCipher cipher = AESEngine.newInstance();
    boolean reSeed = false;
    return new SP800SecureRandomBuilder(source, predictionResistant).setEntropyBitsRequired(
        ENTROPY_BITS_REQUIRED).buildCTR(cipher, CIPHER_LEN, null, reSeed);
  }
}