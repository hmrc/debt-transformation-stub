/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.debttransformationstub.utils

import org.slf4j.MDC
import play.api.Logger
import uk.gov.hmrc.http.{HeaderCarrier, RequestId}

class RequestAwareLogger(clazz: Class[_]) {
  private val underlying = Logger(clazz)

  val requestIdKey = "x-request-id"

  def trace(msg: => String)(implicit hc: HeaderCarrier): Unit = withRequestIDInMDC(underlying.trace(msg))
  def debug(msg: => String)(implicit hc: HeaderCarrier): Unit = withRequestIDInMDC(underlying.debug(msg))
  def info(msg: => String)(implicit hc: HeaderCarrier): Unit = withRequestIDInMDC(underlying.info(msg))
  def warn(msg: => String)(implicit hc: HeaderCarrier): Unit = withRequestIDInMDC(underlying.warn(msg))
  def error(msg: => String)(implicit hc: HeaderCarrier): Unit = withRequestIDInMDC(underlying.error(msg))

  def withRequestIDInMDC(f: => Unit)(implicit hc: HeaderCarrier): Unit = {
    val requestId = hc.requestId.getOrElse(RequestId("Undefined"))
    MDC.put(requestIdKey, requestId.value)
    f
    MDC.remove(requestIdKey)
  }
}
