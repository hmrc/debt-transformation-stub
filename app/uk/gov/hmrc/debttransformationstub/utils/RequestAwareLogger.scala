/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.debttransformationstub.utils

import org.slf4j.MDC
import play.api.Logger
import uk.gov.hmrc.http.{ HeaderCarrier, RequestId }

class RequestAwareLogger(clazz: Class[_]) {
  private val underlying = Logger(clazz)

  val requestIdKey = "x-request-id"

  def trace(msg: => String)(implicit hc: HeaderCarrier): Unit =
    withRequestIDInMDC(underlying.trace(msg))

  def trace(msg: => String, error: => Throwable)(implicit hc: HeaderCarrier): Unit =
    withRequestIDInMDC(underlying.trace(msg, error))

  def debug(msg: => String)(implicit hc: HeaderCarrier): Unit =
    withRequestIDInMDC(underlying.debug(msg))

  def debug(msg: => String, error: => Throwable)(implicit hc: HeaderCarrier): Unit =
    withRequestIDInMDC(underlying.debug(msg, error))

  def info(msg: => String)(implicit hc: HeaderCarrier): Unit =
    withRequestIDInMDC(underlying.info(msg))

  def info(msg: => String, error: => Throwable)(implicit hc: HeaderCarrier): Unit =
    withRequestIDInMDC(underlying.info(msg, error))

  def warn(msg: => String)(implicit hc: HeaderCarrier): Unit =
    withRequestIDInMDC(underlying.warn(msg))

  def warn(msg: => String, error: => Throwable)(implicit hc: HeaderCarrier): Unit =
    withRequestIDInMDC(underlying.warn(msg, error))

  def error(msg: => String)(implicit hc: HeaderCarrier): Unit =
    withRequestIDInMDC(underlying.error(msg))

  def error(msg: => String, error: => Throwable)(implicit hc: HeaderCarrier): Unit =
    withRequestIDInMDC(underlying.error(msg, error))

  def withRequestIDInMDC(f: => Unit)(implicit hc: HeaderCarrier): Unit = {
    val requestId = hc.requestId.getOrElse(RequestId("Undefined"))
    MDC.put(requestIdKey, requestId.value)
    f
    MDC.remove(requestIdKey)
  }
}
