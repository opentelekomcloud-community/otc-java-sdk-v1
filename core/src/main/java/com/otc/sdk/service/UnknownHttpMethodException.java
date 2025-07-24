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

/**
 * Exception thrown when an unknown HTTP method is encountered.
 * This exception extends RuntimeException and can be used to indicate that
 * the specified HTTP method is not recognized or supported.
 */
public class UnknownHttpMethodException extends RuntimeException {
  private static final long serialVersionUID = 4L;
  private String retCd;
  private String msgDes;

  /**
   * Default constructor for UnknownHttpMethodException.
   * Initializes the exception without a specific message.
   */
  public UnknownHttpMethodException() {
  }

  /**
   * Constructor for UnknownHttpMethodException with a specific message.
   *
   * @param message The detail message for the exception
   */
  public UnknownHttpMethodException(String message) {
    super(message);
    this.msgDes = message;
  }

  /**
   * Constructor for UnknownHttpMethodException with return code and message.
   *
   * @param retCd  Return code indicating the error type
   * @param msgDes Detailed message describing the error
   */
  public UnknownHttpMethodException(String retCd, String msgDes) {
    this.retCd = retCd;
    this.msgDes = msgDes;
  }

  /**
   * Gets the return code associated with the exception.
   *
   * @return The return code
   */
  public String getRetCd() {
    return this.retCd;
  }

  /**
   * Gets the detailed message associated with the exception.
   *
   * @return The detailed message
   */
  public String getMsgDes() {
    return this.msgDes;
  }
}
