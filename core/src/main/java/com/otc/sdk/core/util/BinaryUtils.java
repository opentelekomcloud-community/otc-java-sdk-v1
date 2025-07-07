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

import java.util.Locale;

public class BinaryUtils {
   public static String toHex(byte[] data) {
      StringBuffer sbuff = new StringBuffer(data.length * 2);
      byte[] var2 = data;
      int var3 = data.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         byte bye = var2[var4];
         String hexStr = Integer.toHexString(bye);
         if (hexStr.length() == 1) {
            sbuff.append("0");
         } else if (hexStr.length() == 8) {
            hexStr = hexStr.substring(6);
         }

         sbuff.append(hexStr);
      }

      return sbuff.toString().toLowerCase(Locale.getDefault());
   }
}
