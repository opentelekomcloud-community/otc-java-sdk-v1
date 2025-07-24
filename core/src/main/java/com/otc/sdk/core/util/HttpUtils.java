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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpUtils class provides utility methods for handling HTTP-related tasks,
 * such as URL encoding.
 * It includes methods to encode URLs and handle specific character
 * replacements.
 */
public class HttpUtils {
  private static final String DEFAULT_ENCODING = "UTF-8";
  private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
  private static final Pattern ENCODED_CHARACTERS_PATTERN;

  /**
   * Encodes a raw value for use in a URL, replacing specific characters as
   * needed.
   *
   * @param rawValue The raw value to encode
   * @param path     If true, special handling for path segments is applied
   * @return The encoded string
   * @throws UnsupportedEncodingException If the encoding is not supported
   */
  public static String urlEncode(String rawValue, boolean path) throws UnsupportedEncodingException {
    if (rawValue == null) {
      return "";
    } else {
      try {
        String encoded = URLEncoder.encode(rawValue, DEFAULT_ENCODING);
        Matcher match = ENCODED_CHARACTERS_PATTERN.matcher(encoded);

        StringBuffer buffer;
        String replacementTemp;
        for (buffer = new StringBuffer(encoded.length()); match.find(); match.appendReplacement(buffer,
            replacementTemp)) {
          replacementTemp = match.group(0);
          if ("+".equals(replacementTemp)) {
            replacementTemp = "%20";
          } else if ("*".equals(replacementTemp)) {
            replacementTemp = "%2A";
          } else if ("%7E".equals(replacementTemp)) {
            replacementTemp = "~";
          } else if (path && "%2F".equals(replacementTemp)) {
            replacementTemp = "/";
          }
        }

        match.appendTail(buffer);
        return buffer.toString();
      } catch (UnsupportedEncodingException var6) {
        LOGGER.info("fail to encode url: ", var6.getMessage());
        throw var6;
      }
    }
  }

  static {
    StringBuilder pattern = new StringBuilder();
    pattern.append(Pattern.quote("+")).append("|").append(Pattern.quote("*")).append("|").append(Pattern.quote("%7E"))
        .append("|").append(Pattern.quote("%2F"));
    ENCODED_CHARACTERS_PATTERN = Pattern.compile(pattern.toString());
  }
}
