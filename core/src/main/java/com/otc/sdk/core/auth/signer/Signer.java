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

public class Signer {
  private static final Logger LOGGER = LoggerFactory.getLogger(Signer.class);

   public static final String LINE_SEPARATOR = "\n";
   public static final String SDK_SIGNING_ALGORITHM = "SDK-HMAC-SHA256";
   public static final String X_SDK_CONTENT_SHA256 = "x-sdk-content-sha256";
   public static final String X_SDK_DATE = "X-Sdk-Date";
   public static final String AUTHORIZATION = "Authorization";
   private static final Pattern AUTHORIZATION_PATTERN_SHA256 = Pattern.compile("SDK-HMAC-SHA256\\s+Access=([^,]+),\\s?SignedHeaders=([^,]+),\\s?Signature=(\\w+)");
   private static final Pattern AUTHORIZATION_PATTERN_SM3 = Pattern.compile("SDK-HMAC-SM3\\s+Access=([^,]+),\\s?SignedHeaders=([^,]+),\\s?Signature=(\\w+)");
   
   public static final String HOST = "Host";
   public String messageDigestAlgorithm = "SDK-HMAC-SHA256";

   public Signer(String messageDigestAlgorithm) {
      this.messageDigestAlgorithm = messageDigestAlgorithm;
   }

   public Signer() {
   }

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

   protected String getCanonicalizedQueryString(Map<String, List<String>> parameters) throws UnsupportedEncodingException {
      SortedMap<String, List<String>> sorted = new TreeMap<String, List<String>>();
      Iterator var3 = parameters.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<String, List<String>> entry = (Entry)var3.next();
         String encodedParamName = HttpUtils.urlEncode((String)entry.getKey(), false);
         List<String> paramValues = (List)entry.getValue();
         List<String> encodedValues = new ArrayList(paramValues.size());
         Iterator var8 = paramValues.iterator();

         while(var8.hasNext()) {
            String value = (String)var8.next();
            encodedValues.add(HttpUtils.urlEncode(value, false));
         }

         Collections.sort(encodedValues);
         sorted.put(encodedParamName, encodedValues);
      }

      StringBuilder result = new StringBuilder();
      Iterator var11 = sorted.entrySet().iterator();

      while(var11.hasNext()) {
         Entry<String, List<String>> entry = (Entry)var11.next();

         String value;
         for(Iterator var13 = ((List)entry.getValue()).iterator(); var13.hasNext(); result.append((String)entry.getKey()).append("=").append(value)) {
            value = (String)var13.next();
            if (result.length() > 0) {
               result.append("&");
            }
         }
      }

      return result.toString();
   }

   protected String createCanonicalRequest(Request request, String[] signedHeaders, String messageDigestContent) throws UnsupportedEncodingException {
      return request.getMethod().toString() + "\n" + this.getCanonicalizedResourcePath(request.getPath()) + "\n" + this.getCanonicalizedQueryString(request.getQueryStringParams()) + "\n" + this.getCanonicalizedHeaderString(request, signedHeaders) + "\n" + this.getSignedHeadersString(signedHeaders) + "\n" + messageDigestContent;
   }

   protected String createStringToSign(String canonicalRequest, String singerDate) {
      return StringUtils.equals(this.messageDigestAlgorithm, "SDK-HMAC-SHA256") ? this.messageDigestAlgorithm + "\n" + singerDate + "\n" + BinaryUtils.toHex(this.hash(canonicalRequest)) : this.messageDigestAlgorithm + "\n" + singerDate + "\n" + BinaryUtils.toHex(this.hashSm3(canonicalRequest));
   }

   private byte[] deriveSigningKey(String secret) {
      return secret.getBytes(StandardCharsets.UTF_8);
   }

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

   protected final byte[] computeSignature(String stringToSign, byte[] signingKey) {
      return StringUtils.equals(this.messageDigestAlgorithm, "SDK-HMAC-SHA256") ? this.sign(stringToSign.getBytes(StandardCharsets.UTF_8), signingKey, SigningAlgorithm.HmacSHA256) : this.sign(stringToSign.getBytes(StandardCharsets.UTF_8), signingKey, SigningAlgorithm.HmacSM3);
   }

   private String buildAuthorizationHeader(String[] signedHeaders, byte[] signature, String accessKey) {
      String credential = "Access=" + accessKey;
      String signerHeaders = "SignedHeaders=" + this.getSignedHeadersString(signedHeaders);
      String signatureHeader = "Signature=" + BinaryUtils.toHex(signature);

      String ret = this.messageDigestAlgorithm + " " + credential + ", " + signerHeaders + ", " + signatureHeader;
      LOGGER.info(ret);
      return ret;
   }

   protected String[] getSignedHeaders(Request request) {
      String[] signedHeaders = (String[])request.getHeaders().keySet().toArray(new String[0]);
      Arrays.sort(signedHeaders, String.CASE_INSENSITIVE_ORDER);
      return signedHeaders;
   }

   protected String getCanonicalizedHeaderString(Request request, String[] signedHeaders) {
      Map<String, String> requestHeaders = request.getHeaders();
      StringBuilder buffer = new StringBuilder();
      String[] var5 = signedHeaders;
      int var6 = signedHeaders.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         String header = var5[var7];
         String key = header.toLowerCase(Locale.getDefault());
         String value = (String)requestHeaders.get(header);
         buffer.append(key).append(":");
         if (value != null) {
            buffer.append(value.trim());
         }

         buffer.append("\n");
      }

      return buffer.toString();
   }

   protected String getSignedHeadersString(String[] signedHeaders) {
      StringBuilder buffer = new StringBuilder();
      String[] var3 = signedHeaders;
      int var4 = signedHeaders.length;

      for(int var5 = 0; var5 < var4; ++var5) {
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

      while(var3.hasNext()) {
         String key = (String)var3.next();
         if ("Host".equalsIgnoreCase(key)) {
            haveHostHeader = true;
            break;
         }
      }

      if (!haveHostHeader) {
         request.addHeader("Host", request.getHost());
      }

   }

   protected String getHeader(Request request, String header) {
      if (header == null) {
         return null;
      } else {
         Map<String, String> headers = request.getHeaders();
         Iterator<Map.Entry<String,String>> var4 = headers.entrySet().iterator();

         Entry<String,String> entry;
         do {
            if (!var4.hasNext()) {
               return null;
            }

            entry = (Entry)var4.next();
         } while(!header.equalsIgnoreCase((String)entry.getKey()));

         return (String)entry.getValue();
      }
   }

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

   protected String calculateContentHash(Request request) {
      String content_sha256 = this.getHeader(request, "x-sdk-content-sha256");
      if (content_sha256 != null) {
         return content_sha256;
      } else {
         return StringUtils.equals(this.messageDigestAlgorithm, "SDK-HMAC-SHA256") ? BinaryUtils.toHex(this.hash(request.getBody())) : BinaryUtils.toHex(this.hashSm3(request.getBody()));
      }
   }

   public byte[] hash(String text) {
      try {
         MessageDigest md = MessageDigest.getInstance("SHA-256");
         md.update(text.getBytes(StandardCharsets.UTF_8));
         return md.digest();
      } catch (NoSuchAlgorithmException var3) {
         return new byte[0];
      }
   }

   public byte[] hashSm3(String text) {
      byte[] srcData = text.getBytes(StandardCharsets.UTF_8);
      SM3Digest digest = new SM3Digest();
      digest.update(srcData, 0, srcData.length);
      byte[] hash = new byte[digest.getDigestSize()];
      digest.doFinal(hash, 0);
      return hash;
   }
}
