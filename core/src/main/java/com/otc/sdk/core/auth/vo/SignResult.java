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

public class SignResult {
   private Map<String, String> headers = new HashMap<>();
   private URL url;
   private Map<String, String> parameters = new HashMap<>();
   private InputStream inputStream;

   public Map<String, String> getHeaders() {
      return this.headers;
   }

   public void setHeaders(Map<String, String> headers) {
      this.headers = headers;
   }

   public URL getUrl() {
      return this.url;
   }

   public void setUrl(URL url) {
      this.url = url;
   }

   public Map<String, String> getParameters() {
      return this.parameters;
   }

   public void setParameters(Map<String, String> parameters) {
      this.parameters = parameters;
   }

   public InputStream getInputStream() {
      return this.inputStream;
   }

   public void setInputStream(InputStream inputStream) {
      this.inputStream = inputStream;
   }
}
