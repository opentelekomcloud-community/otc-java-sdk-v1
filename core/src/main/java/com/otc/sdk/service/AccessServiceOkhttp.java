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

public abstract class AccessServiceOkhttp {
   protected String ak;
   protected String sk;
   protected String messageDigestAlgorithm = "SDK-HMAC-SHA256";

   public AccessServiceOkhttp(String ak, String sk) {
      this.ak = ak;
      this.sk = sk;
   }

   public AccessServiceOkhttp(String ak, String sk, String messageDigestAlgorithm) {
      this.ak = ak;
      this.sk = sk;
      this.messageDigestAlgorithm = messageDigestAlgorithm;
   }

   public abstract okhttp3.Request access(String var1, Map<String, String> var2, String var3, HttpMethodName var4) throws Exception;

   public okhttp3.Request access(String url, Map<String, String> header, HttpMethodName httpMethod) throws Exception {
      return this.access(url, header, (String)null, httpMethod);
   }

   public okhttp3.Request access(String url, String entity, HttpMethodName httpMethod) throws Exception {
      return this.access(url, (Map<String,String>)null, entity, httpMethod);
   }

   public okhttp3.Request access(String url, HttpMethodName httpMethod) throws Exception {
      return this.access(url, (Map<String,String>)null, (String)null, httpMethod);
   }

   public String getAk() {
      return this.ak;
   }

   public void setAk(String ak) {
      this.ak = ak;
   }

   public String getSk() {
      return this.sk;
   }

   public void setSk(String sk) {
      this.sk = sk;
   }
}
