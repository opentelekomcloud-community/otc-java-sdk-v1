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

public class SSLCipherSuiteUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(SSLCipherSuiteUtil.class);
    private static CloseableHttpClient httpClient;
    private static OkHttpClient okHttpClient;

    private static final int CIPHER_LEN = 256;

    private static final int ENTROPY_BITS_REQUIRED = 384;

    public static HttpClient createHttpClient(String protocol) throws Exception {
        SSLContext sslContext = getSslContext(protocol);
        // create factory
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                new String[]{protocol}, Constant.SUPPORTED_CIPHER_SUITES, new TrustAllHostnameVerifier());

        httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
        return httpClient;
    }

    public static HttpClient createHttpClientWithVerify(String protocol) throws Exception {
        SSLContext sslContext = getSslContextWithVerify(protocol);
        // create factory
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
            new String[] {protocol}, Constant.SUPPORTED_CIPHER_SUITES, new TheRealHostnameVerifier());

        httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
        return httpClient;
    }

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

    private static SSLContext getSslContext(String protocol) throws UnsupportProtocolException,
            NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
        if (!Constant.GM_PROTOCOL.equals(protocol) && !Constant.INTERNATIONAL_PROTOCOL.equals(protocol)) {
            LOGGER.info("Unsupport protocol: {}, Only support GMTLS TLSv1.2", protocol);
            throw new UnsupportProtocolException("Unsupport protocol, Only support GMTLS TLSv1.2");
        }
        // Create a trust manager that does not validate certificate chains
        TrustAllManager[] trust = {new TrustAllManager()};
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

    // 不校验域名
    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    // 校验域名
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

    // 不校验服务端证书
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
        BlockCipher cipher = new AESEngine();
        boolean reSeed = false;
        return new SP800SecureRandomBuilder(source, predictionResistant).setEntropyBitsRequired(
                ENTROPY_BITS_REQUIRED).buildCTR(cipher, CIPHER_LEN, null, reSeed);
    }
}