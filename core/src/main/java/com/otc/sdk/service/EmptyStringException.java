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
 * Exception thrown when an empty string is encountered where it is not allowed.
 * This exception extends RuntimeException and can be used to indicate that a
 * required string parameter is empty.
 */
public class EmptyStringException extends RuntimeException {
  private static final long serialVersionUID = 4312820110480855928L;
  private String retCd;
  private String msgDes;

  /**
   * Default constructor for EmptyStringException.
   * Initializes the exception without a specific message.
   */
  public EmptyStringException() {
  }

  /**
   * Constructor for EmptyStringException with a specific message.
   *
   * @param message The detail message for the exception
   */
  public EmptyStringException(String message) {
    super(message);
    this.msgDes = message;
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
