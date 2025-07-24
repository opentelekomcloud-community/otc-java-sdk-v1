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

package com.otc.sdk.core.auth.signer;

import com.otc.sdk.core.util.BinaryUtils;
import com.otc.sdk.core.util.HttpUtils;
import com.otc.sdk.service.Request;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.StringUtils;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.openeuler.BGMJCEProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Signer class that provides methods to sign requests using HMAC-SHA256 or
 * HMAC-SM3.
 * It handles the signing process, including creating canonical requests, string
 * to sign, and computing signatures.
 */
public class Signer {
  private static final Logger LOGGER = LoggerFactory.getLogger(Signer.class);

  public static final String LINE_SEPARATOR = "\n";
  public static final String SDK_SIGNING_ALGORITHM = "SDK-HMAC-SHA256";
  public static final String X_SDK_CONTENT_SHA256 = "x-sdk-content-sha256";
  public static final String X_SDK_DATE = "X-Sdk-Date";
  public static final String AUTHORIZATION = "Authorization";
  private static final Pattern AUTHORIZATION_PATTERN_SHA256 = Pattern
      .compile("SDK-HMAC-SHA256\\s+Access=([^,]+),\\s?SignedHeaders=([^,]+),\\s?Signature=(\\w+)");
  private static final Pattern AUTHORIZATION_PATTERN_SM3 = Pattern
      .compile("SDK-HMAC-SM3\\s+Access=([^,]+),\\s?SignedHeaders=([^,]+),\\s?Signature=(\\w+)");

  public static final String HOST = "Host";
  public String messageDigestAlgorithm = "SDK-HMAC-SHA256";

  /**
   * Constructor for Signer with a specific message digest algorithm.
   *
   * @param messageDigestAlgorithm The message digest algorithm to use
   */
  public Signer(String messageDigestAlgorithm) {
    this.messageDigestAlgorithm = messageDigestAlgorithm;
  }

  /**
   * Default constructor for Signer.
   * Initializes the signer with the default message digest algorithm.
   */
  public Signer() {
  }

  /**
   * Signs a request using the specified algorithm and returns the signed URL and
   * headers.
   *
   * @param request   The request to be signed
   * @throws UnsupportedEncodingException if an error occurs during signing
   */
  public void sign(Request request) throws UnsupportedEncodingException {
    String singerDate = this.getHeader(request, "X-Sdk-Date");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    if (singerDate == null) {
      singerDate = sdf.format(new Date());
      request.addHeader("X-Sdk-Date", singerDate);
    }

    this.addHostHeader(request);
    String messageDigestContent = this.calculateContentHash(request);
    String[] signedHeaders = this.getSignedHeaders(request);
    String canonicalRequest = this.createCanonicalRequest(request, signedHeaders, messageDigestContent);
    byte[] signingKey = this.deriveSigningKey(request.getSecrect());
    String stringToSign = this.createStringToSign(canonicalRequest, singerDate);
    byte[] signature = this.computeSignature(stringToSign, signingKey);
    String signatureResult = this.buildAuthorizationHeader(signedHeaders, signature, request.getKey());
    request.addHeader("Authorization", signatureResult);
  }

  /**
   * Gets the canonicalized resource path from the request.
   *
   * @param resourcePath The resource path to canonicalize
   * @return The canonicalized resource path
   * @throws UnsupportedEncodingException if an error occurs during encoding
   */
  protected String getCanonicalizedResourcePath(String resourcePath) throws UnsupportedEncodingException {
    if (resourcePath != null && !resourcePath.isEmpty()) {
      try {
        resourcePath = (new URI(resourcePath)).getPath();
      } catch (URISyntaxException var3) {
        return resourcePath;
      }

      String value = HttpUtils.urlEncode(resourcePath, true);
      if (!value.startsWith("/")) {
        value = "/".concat(value);
      }

      if (!value.endsWith("/")) {
        value = value.concat("/");
      }

      return value;
    } else {
      return "/";
    }
  }

  /**
   * Gets the canonicalized query string from the request parameters.
   *
   * @param parameters The query string parameters to canonicalize
   * @return The canonicalized query string
   * @throws UnsupportedEncodingException if an error occurs during encoding
   */
  protected String getCanonicalizedQueryString(Map<String, List<String>> parameters)
      throws UnsupportedEncodingException {
    SortedMap<String, List<String>> sorted = new TreeMap<String, List<String>>();
    Iterator var3 = parameters.entrySet().iterator();

    while (var3.hasNext()) {
      Entry<String, List<String>> entry = (Entry) var3.next();
      String encodedParamName = HttpUtils.urlEncode((String) entry.getKey(), false);
      List<String> paramValues = (List) entry.getValue();
      List<String> encodedValues = new ArrayList(paramValues.size());
      Iterator var8 = paramValues.iterator();

      while (var8.hasNext()) {
        String value = (String) var8.next();
        encodedValues.add(HttpUtils.urlEncode(value, false));
      }

      Collections.sort(encodedValues);
      sorted.put(encodedParamName, encodedValues);
    }

    StringBuilder result = new StringBuilder();
    Iterator var11 = sorted.entrySet().iterator();

    while (var11.hasNext()) {
      Entry<String, List<String>> entry = (Entry) var11.next();

      String value;
      for (Iterator var13 = ((List) entry.getValue()).iterator(); var13.hasNext(); result
          .append((String) entry.getKey()).append("=").append(value)) {
        value = (String) var13.next();
        if (result.length() > 0) {
          result.append("&");
        }
      }
    }

    return result.toString();
  }

  /**
   * Creates a canonical request string based on the request method, resource
   * path,
   * query string, headers, and message digest content.
   *
   * @param request              The request to create the canonical request for
   * @param signedHeaders        The signed headers to include in the request
   * @param messageDigestContent The content to hash for the request
   * @return The canonical request string
   * @throws UnsupportedEncodingException if an error occurs during encoding
   */
  protected String createCanonicalRequest(Request request, String[] signedHeaders, String messageDigestContent)
      throws UnsupportedEncodingException {
    return request.getMethod().toString() + "\n" + this.getCanonicalizedResourcePath(request.getPath()) + "\n"
        + this.getCanonicalizedQueryString(request.getQueryStringParams()) + "\n"
        + this.getCanonicalizedHeaderString(request, signedHeaders) + "\n" + this.getSignedHeadersString(signedHeaders)
        + "\n" + messageDigestContent;
  }

  /**
   * Creates a string to sign based on the canonical request and the signing date.
   *
   * @param canonicalRequest The canonical request string
   * @param singerDate       The signing date in UTC format
   * @return The string to sign
   */
  protected String createStringToSign(String canonicalRequest, String singerDate) {
    return StringUtils.equals(this.messageDigestAlgorithm, "SDK-HMAC-SHA256")
        ? this.messageDigestAlgorithm + "\n" + singerDate + "\n" + BinaryUtils.toHex(this.hash(canonicalRequest))
        : this.messageDigestAlgorithm + "\n" + singerDate + "\n" + BinaryUtils.toHex(this.hashSm3(canonicalRequest));
  }

  /**
   * Derives the signing key from the secret key.
   *
   * @param secret The secret key to derive the signing key from
   * @return The derived signing key as a byte array
   */
  private byte[] deriveSigningKey(String secret) {
    return secret.getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Signs the data using the specified key and algorithm.
   *
   * @param data      The data to sign
   * @param key       The signing key
   * @param algorithm The signing algorithm to use (HmacSHA256 or HmacSM3)
   * @return The signed data as a byte array
   */
  protected byte[] sign(byte[] data, byte[] key, SigningAlgorithm algorithm) {
    try {
      if (SigningAlgorithm.HmacSM3.equals(algorithm)) {
        Security.insertProviderAt(new BGMJCEProvider(), 1);
      }

      Mac mac = Mac.getInstance(algorithm.toString());
      mac.init(new SecretKeySpec(key, algorithm.toString()));
      return mac.doFinal(data);
    } catch (InvalidKeyException | NoSuchAlgorithmException var5) {
      return new byte[0];
    }
  }

  /**
   * Computes the signature for the given string using the derived signing key.
   *
   * @param stringToSign The string to sign
   * @param signingKey   The derived signing key
   * @return The computed signature as a byte array
   */
  protected final byte[] computeSignature(String stringToSign, byte[] signingKey) {
    return StringUtils.equals(this.messageDigestAlgorithm, "SDK-HMAC-SHA256")
        ? this.sign(stringToSign.getBytes(StandardCharsets.UTF_8), signingKey, SigningAlgorithm.HmacSHA256)
        : this.sign(stringToSign.getBytes(StandardCharsets.UTF_8), signingKey, SigningAlgorithm.HmacSM3);
  }

  /**
   * Builds the authorization header string using the signed headers, signature,
   * and access key.
   *
   * @param signedHeaders The signed headers
   * @param signature     The computed signature
   * @param accessKey     The access key
   * @return The authorization header string
   */
  private String buildAuthorizationHeader(String[] signedHeaders, byte[] signature, String accessKey) {
    String credential = "Access=" + accessKey;
    String signerHeaders = "SignedHeaders=" + this.getSignedHeadersString(signedHeaders);
    String signatureHeader = "Signature=" + BinaryUtils.toHex(signature);

    String ret = this.messageDigestAlgorithm + " " + credential + ", " + signerHeaders + ", " + signatureHeader;
    LOGGER.info(ret);
    return ret;
  }

  /**
   * Gets the signed headers from the request.
   *
   * @param request The request to get the signed headers from
   * @return An array of signed header names
   */
  protected String[] getSignedHeaders(Request request) {
    String[] signedHeaders = (String[]) request.getHeaders().keySet().toArray(new String[0]);
    Arrays.sort(signedHeaders, String.CASE_INSENSITIVE_ORDER);
    return signedHeaders;
  }

  /**
   * Gets the canonicalized header string from the request headers.
   *
   * @param request       The request to get the headers from
   * @param signedHeaders The signed headers to include in the string
   * @return The canonicalized header string
   */
  protected String getCanonicalizedHeaderString(Request request, String[] signedHeaders) {
    Map<String, String> requestHeaders = request.getHeaders();
    StringBuilder buffer = new StringBuilder();
    String[] var5 = signedHeaders;
    int var6 = signedHeaders.length;

    for (int var7 = 0; var7 < var6; ++var7) {
      String header = var5[var7];
      String key = header.toLowerCase(Locale.getDefault());
      String value = (String) requestHeaders.get(header);
      buffer.append(key).append(":");
      if (value != null) {
        buffer.append(value.trim());
      }

      buffer.append("\n");
    }

    return buffer.toString();
  }

  /**
   * Gets the signed headers as a string.
   *
   * @param signedHeaders The signed headers to convert to a string
   * @return A string representation of the signed headers
   */
  protected String getSignedHeadersString(String[] signedHeaders) {
    StringBuilder buffer = new StringBuilder();
    String[] var3 = signedHeaders;
    int var4 = signedHeaders.length;

    for (int var5 = 0; var5 < var4; ++var5) {
      String header = var3[var5];
      if (buffer.length() > 0) {
        buffer.append(";");
      }

      buffer.append(header.toLowerCase(Locale.getDefault()));
    }

    return buffer.toString();
  }

  protected void addHostHeader(Request request) {
    boolean haveHostHeader = false;
    Iterator var3 = request.getHeaders().keySet().iterator();

    while (var3.hasNext()) {
      String key = (String) var3.next();
      if ("Host".equalsIgnoreCase(key)) {
        haveHostHeader = true;
        break;
      }
    }

    if (!haveHostHeader) {
      request.addHeader("Host", request.getHost());
    }

  }

  /**
   * Gets the value of a specific header from the request.
   *
   * @param request The request to get the header from
   * @param header  The name of the header to retrieve
   * @return The value of the specified header, or null if not found
   */
  protected String getHeader(Request request, String header) {
    if (header == null) {
      return null;
    } else {
      Map<String, String> headers = request.getHeaders();
      Iterator<Map.Entry<String, String>> var4 = headers.entrySet().iterator();

      Entry<String, String> entry;
      do {
        if (!var4.hasNext()) {
          return null;
        }

        entry = (Entry) var4.next();
      } while (!header.equalsIgnoreCase((String) entry.getKey()));

      return (String) entry.getValue();
    }
  }

  /**
   * Verifies the signature of the request.
   *
   * @param request The request to verify
   * @return true if the signature is valid, false otherwise
   * @throws UnsupportedEncodingException if an error occurs during verification
   */
  public boolean verify(Request request) throws UnsupportedEncodingException {
    String singerDate = this.getHeader(request, "X-Sdk-Date");
    String authorization = this.getHeader(request, "Authorization");
    Matcher match = AUTHORIZATION_PATTERN_SM3.matcher(authorization);
    if (StringUtils.equals(this.messageDigestAlgorithm, "SDK-HMAC-SHA256")) {
      match = AUTHORIZATION_PATTERN_SHA256.matcher(authorization);
    }

    if (!match.find()) {
      return false;
    } else {
      String[] signedHeaders = match.group(2).split(";");
      byte[] signingKey = this.deriveSigningKey(request.getSecrect());
      String messageDigestContent = this.calculateContentHash(request);
      String canonicalRequest = this.createCanonicalRequest(request, signedHeaders, messageDigestContent);
      String stringToSign = this.createStringToSign(canonicalRequest, singerDate);
      byte[] signature = this.computeSignature(stringToSign, signingKey);
      String signatureResult = this.buildAuthorizationHeader(signedHeaders, signature, request.getKey());
      return signatureResult.equals(authorization);
    }
  }

  /**
   * Calculates the content hash for the request.
   *
   * @param request The request to calculate the content hash for
   * @return The content hash as a hexadecimal string
   */
  protected String calculateContentHash(Request request) {
    String content_sha256 = this.getHeader(request, "x-sdk-content-sha256");
    if (content_sha256 != null) {
      return content_sha256;
    } else {
      return StringUtils.equals(this.messageDigestAlgorithm, "SDK-HMAC-SHA256")
          ? BinaryUtils.toHex(this.hash(request.getBody()))
          : BinaryUtils.toHex(this.hashSm3(request.getBody()));
    }
  }

  /**
   * Hashes the input text using SHA-256.
   * 
   * @param text The text to hash
   * @return The hashed byte array
   */
  public byte[] hash(String text) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(text.getBytes(StandardCharsets.UTF_8));
      return md.digest();
    } catch (NoSuchAlgorithmException var3) {
      return new byte[0];
    }
  }

  /**
   * Hashes the input text using SM3.
   * 
   * @param text The text to hash
   * @return The hashed byte array
   */
  public byte[] hashSm3(String text) {
    byte[] srcData = text.getBytes(StandardCharsets.UTF_8);
    SM3Digest digest = new SM3Digest();
    digest.update(srcData, 0, srcData.length);
    byte[] hash = new byte[digest.getDigestSize()];
    digest.doFinal(hash, 0);
    return hash;
  }
}
