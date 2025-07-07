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

public class UnknownHttpMethodException extends RuntimeException {
   private static final long serialVersionUID = 4L;
   private String retCd;
   private String msgDes;

   public UnknownHttpMethodException() {
   }

   public UnknownHttpMethodException(String message) {
      super(message);
      this.msgDes = message;
   }

   public UnknownHttpMethodException(String retCd, String msgDes) {
      this.retCd = retCd;
      this.msgDes = msgDes;
   }

   public String getRetCd() {
      return this.retCd;
   }

   public String getMsgDes() {
      return this.msgDes;
   }
}
