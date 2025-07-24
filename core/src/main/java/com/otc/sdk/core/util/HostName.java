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

/**
 * HostName utility class to manage and validate host names.
 * It provides methods to set a URL host name and check if a given SSL host name
 * matches the set URL host name.
 */
public class HostName {
  private static String urlHostName;

  /**
   * Sets the URL host name.
   */
  public static void setUrlHostName(String hostName) {
    urlHostName = hostName;
  }

  /**
   * Checks if the provided SSL host name matches the set URL host name.
   *
   * @param SSLHostName The SSL host name to check
   * @return true if the SSL host name matches the URL host name, false otherwise
   */
  public static boolean checkHostName(String SSLHostName) {
    return urlHostName.equals(SSLHostName);
  }
}
