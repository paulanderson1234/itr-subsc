/*
 * Copyright 2016 HM Revenue & Customs
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

package connectors

import com.typesafe.config.ConfigFactory
import config.{MicroserviceAppConfig, WSHttp}
import model.SubscriptionRequest
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.{ExecutionContext, Future}

object SubscriptionETMPConnector extends SubscriptionETMPConnector {

  override val serviceUrl = MicroserviceAppConfig.desURL
  override def http: HttpGet with HttpPost with HttpPut = WSHttp
  override val environment = MicroserviceAppConfig.desEnvironment
  override val token = MicroserviceAppConfig.desToken
}

trait SubscriptionETMPConnector extends ServicesConfig {

  def http: HttpGet with HttpPost with HttpPut

  val serviceUrl: String
  val environment: String
  val token: String
  private lazy val config = ConfigFactory.load()

  def subscribeToEtmp(safeId: String, subscribeRequest: SubscriptionRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val requestUrl = s"$serviceUrl/tax-assured-venture-capital/taxpayers/$safeId/subscription"
    http.POST[JsValue, HttpResponse](requestUrl, Json.toJson(subscribeRequest),Seq("Environment" -> environment, "Authorization" -> s"Bearer $token"))
  }

  def getSubscription(tavcReferenceNumber: String)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val requestUrl = s"$serviceUrl/tax-assured-venture-capital/taxpayers/$tavcReferenceNumber/subscription"
    http.GET[HttpResponse](requestUrl)(HttpReads.readRaw,hc.withExtraHeaders("Environment" -> environment, "Authorization" -> s"Bearer $token"))
  }
}