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

package services

import common.GovernmentGatewayConstants._
import connectors.{GovernmentGatewayAdminConnector, SubscriptionETMPConnector}
import model.SubscriptionRequest
import models.{KnownFact, KnownFactsForService}
import play.api.http.Status._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

object SubscriptionService extends SubscriptionService{
  val subscriptionETMPConnector: SubscriptionETMPConnector = SubscriptionETMPConnector
  val ggAdminConnector: GovernmentGatewayAdminConnector = GovernmentGatewayAdminConnector
}

trait SubscriptionService {

  val subscriptionETMPConnector: SubscriptionETMPConnector
  val ggAdminConnector: GovernmentGatewayAdminConnector

  def subscribe(safeId: String,
                subscriptionRequest: SubscriptionRequest,
                postcode: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] =
    for {
      etmpResponse <- subscriptionETMPConnector.subscribeToEtmp(safeId,subscriptionRequest)
      ggAdminResponse <- addKnownFacts(etmpResponse, postcode)
    } yield ggAdminResponse

  def addKnownFacts(etmpResponse: HttpResponse, postCode: String)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    etmpResponse.status match {
      case CREATED => ggAdminConnector.addKnownFacts(knownFactsBuilder(etmpResponse, postCode))
      case _ => Future.successful(etmpResponse)
    }

  def knownFactsBuilder(etmpResponse: HttpResponse, postCode: String): KnownFactsForService = {
    val knownFact1 = KnownFact(tavcReferenceKey, (etmpResponse.json \ tavcReferenceKey).as[String])
    val knownFact2 = KnownFact(postCodeKey, postCode)
    KnownFactsForService(List(knownFact1, knownFact2))
  }
}
