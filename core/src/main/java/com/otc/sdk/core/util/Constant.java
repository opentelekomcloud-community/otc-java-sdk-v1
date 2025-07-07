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

public final class Constant {
  // verify ssl certificate (true) or do not verify (false)
  public static final boolean DO_VERIFY = false;

  public static final String HTTPS = "HTTPS";
  public static final String TRUST_MANAGER_FACTORY = "SunX509";
  public static final String GM_PROTOCOL = "GMTLS";
  public static final String INTERNATIONAL_PROTOCOL = "TLSv1.2";
  public static final String SIGNATURE_ALGORITHM_SDK_HMAC_SHA256 = "SDK-HMAC-SHA256";
  public static final String SIGNATURE_ALGORITHM_SDK_HMAC_SM3 = "SDK-HMAC-SM3";
  public static final String[] SUPPORTED_CIPHER_SUITES = { "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
      "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
      "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384" };
  public static final String SECURE_RANDOM_ALGORITHM_NATIVE_PRNG_NON_BLOCKING = "NativePRNGNonBlocking";

  private Constant() {
  }
}