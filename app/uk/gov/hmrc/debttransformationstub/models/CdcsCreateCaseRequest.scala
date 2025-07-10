/*
 * Copyright 2025 HM Revenue & Customs
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

package uk.gov.hmrc.debttransformationstub.models

/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

import play.api.libs.json.{ Format, Json, OFormat }
import uk.gov.hmrc.debttransformationstub.models.CdcsCreateCaseRequest._
import uk.gov.hmrc.debttransformationstub.models.CdcsCreateCaseRequestWrappedTypes._

case class CdcsCreateCaseRequest(
  plan: CdcsCreateCaseRequestPlan,
  customer: CdcsCreateCaseRequestCustomer,
  taxAccount: CdcsCreateCaseRequestTaxAccount,
  upstreamErrors: Option[List[CdcsCreateCaseRequestUpstreamError]]
)

object CdcsCreateCaseRequest {

  implicit val format: OFormat[CdcsCreateCaseRequest] = Json.format[CdcsCreateCaseRequest]

  case class CdcsCreateCaseRequestPlan(
    frequency: CdcsCreateCaseRequestPlanFrequency,
    payments: List[CdcsCreateCaseRequestPayment],
    instalments: List[CdcsCreateCaseRequestInstalment],
    instalmentStartDate: CdcsCreateCaseRequestInstalmentStartDate,
    instalmentAmount: CdcsCreateCaseRequestInstalmentAmount,
    numberOfInstalments: CdcsCreateCaseRequestNumberOfInstalments,
    planInterest: CdcsCreateCaseRequestPlanInterest,
    totalInterest: CdcsCreateCaseRequestTotalInterest,
    totalDebtIncInt: CdcsCreateCaseRequestTotalDebtIncInt,
    interestAccrued: CdcsCreateCaseRequestInterestAccrued,
    ttpArrangementId: Option[CdcsCreateCaseRequestTtpArrangementId],
    AAId: Option[CdcsCreateCaseRequestAAId],
    duration: Option[CdcsCreateCaseRequestPlanDuration],
    initialPaymentMethod: Option[CdcsCreateCaseRequestPaymentMethod],
    initialPaymentReference: Option[CdcsCreateCaseRequestInitialPaymentReference],
    totalLiabilityAmount: Option[CdcsCreateCaseRequestTotalLiabilityAmount],
    initialPaymentAmount: Option[CdcsCreateCaseRequestInitialPaymentAmount],
    initialPaymentDate: Option[CdcsCreateCaseRequestInitialPaymentDate],
    paymentPlanType: Option[CdcsCreateCaseRequestPaymentPlanType],
    collectionInfo: Option[List[CdcsCreateCaseRequestCollectionInfo]]
  )

  object CdcsCreateCaseRequestPlan {
    implicit val format: OFormat[CdcsCreateCaseRequestPlan] = Json.format[CdcsCreateCaseRequestPlan]
  }

  case class CdcsCreateCaseRequestInstalment(
    debtItemChargeId: CdcsCreateCaseRequestDebtItemChargeId,
    dueDate: CdcsCreateCaseRequestInstalmentDueDate,
    amountDue: CdcsCreateCaseRequestInstalmentAmountDue,
    expectedPayment: Option[
      CdcsCreateCaseRequestInstalmentExpectedPayment
    ], // breaks schema but required in order for us to not to potentially trigger a customer impacting process
    instalmentNumber: CdcsCreateCaseRequestInstalmentNumber,
    instalmentInterestAccrued: CdcsCreateCaseRequestInstalmentInterestAccrued,
    instalmentBalance: CdcsCreateCaseRequestInstalmentBalance
  )

  object CdcsCreateCaseRequestInstalment {
    implicit val format: OFormat[CdcsCreateCaseRequestInstalment] = Json.format[CdcsCreateCaseRequestInstalment]

  }

  case class CdcsCreateCaseRequestPayment(
    paymentMethod: Option[CdcsCreateCaseRequestPaymentMethod],
    paymentReference: Option[CdcsCreateCaseRequestPaymentReference]
  )

  object CdcsCreateCaseRequestPayment {
    implicit val format: OFormat[CdcsCreateCaseRequestPayment] = Json.format[CdcsCreateCaseRequestPayment]
  }

  case class CdcsCreateCaseRequestCollectionInfo(
    initialCollection: Option[CdcsCreateCaseRequestCollection],
    regularCollections: List[CdcsCreateCaseRequestCollection]
  )

  object CdcsCreateCaseRequestCollectionInfo {
    implicit val format: OFormat[CdcsCreateCaseRequestCollectionInfo] = Json.format[CdcsCreateCaseRequestCollectionInfo]

  }

  case class CdcsCreateCaseRequestCollection(
    amountDue: CdcsCreateCaseRequestAmountDue,
    paymentDueDate: CdcsCreateCaseRequestPaymentDueDate
  )

  object CdcsCreateCaseRequestCollection {
    implicit val format: OFormat[CdcsCreateCaseRequestCollection] = Json.format[CdcsCreateCaseRequestCollection]
  }

  case class CdcsCreateCaseRequestUpstreamError(
    sourceSystem: CdcsCreateCaseRequestSourceSystem,
    upstreamErrorCode: Option[CdcsCreateCaseRequestUpstreamErrorCode],
    upstreamErrorDescription: Option[CdcsCreateCaseRequestUpstreamErrorDescription]
  )

  object CdcsCreateCaseRequestUpstreamError {

    implicit val format: OFormat[CdcsCreateCaseRequestUpstreamError] = Json.format[CdcsCreateCaseRequestUpstreamError]
  }

  case class CdcsCreateCaseRequestCustomer(individual: CdcsCreateCaseRequestIndividual)

  object CdcsCreateCaseRequestCustomer {
    implicit val format: OFormat[CdcsCreateCaseRequestCustomer] = Json.format[CdcsCreateCaseRequestCustomer]
  }

  case class CdcsCreateCaseRequestIndividual(
    lastName: CdcsCreateCaseRequestLastName,
    customerAddresses: List[CdcsCreateCaseRequestCustomerAddress],
    identifications: List[CdcsCreateCaseRequestIdentification],
    title: Option[CdcsCreateCaseRequestTitle],
    initials: Option[CdcsCreateCaseRequestInitials],
    firstName: Option[CdcsCreateCaseRequestFirstName],
    middleName: Option[CdcsCreateCaseRequestMiddleName],
    dateOfBirth: Option[CdcsCreateCaseRequestDateOfBirth]
  )

  object CdcsCreateCaseRequestIndividual {

    implicit val format: OFormat[CdcsCreateCaseRequestIndividual] = Json.format[CdcsCreateCaseRequestIndividual]
  }

  case class CdcsCreateCaseRequestCustomerAddress(
    addressType: Option[
      CdcsCreateCaseRequestAddressTypeReference
    ], // breaks schema but required in order for us to not to potentially trigger a customer impacting process
    addressLine1: CdcsCreateCaseRequestAddressLine,
    addressLine2: Option[CdcsCreateCaseRequestAddressLine],
    addressLine3: Option[CdcsCreateCaseRequestAddressLine],
    addressLine4: Option[CdcsCreateCaseRequestAddressLine],
    postCode: Option[CdcsCreateCaseRequestPostCode],
    contactDetails: Option[CdcsCreateCaseRequestContactDetails]
  )

  object CdcsCreateCaseRequestCustomerAddress {

    implicit val format: OFormat[CdcsCreateCaseRequestCustomerAddress] =
      Json.format[CdcsCreateCaseRequestCustomerAddress]
  }

  case class CdcsCreateCaseRequestContactDetails(
    telephoneNumber: CdcsCreateCaseRequestTelephoneNumber,
    email: CdcsCreateCaseRequestEmail
  )

  object CdcsCreateCaseRequestContactDetails {
    implicit val format: OFormat[CdcsCreateCaseRequestContactDetails] = Json.format[CdcsCreateCaseRequestContactDetails]
  }

  case class CdcsCreateCaseRequestIdentification(
    idType: CdcsCreateCaseRequestIdTypeReference,
    idValue: CdcsCreateCaseRequestIdValue
  )

  object CdcsCreateCaseRequestIdentification {
    implicit val format: OFormat[CdcsCreateCaseRequestIdentification] = Json.format[CdcsCreateCaseRequestIdentification]
  }

  case class CdcsCreateCaseRequestTaxAccount(
    regimeType: CdcsCreateCaseRequestRegimeTypeReference,
    debtItems: Option[
      List[CdcsCreateCaseRequestDebtItem]
    ], // breaks schema but required in order for us to not to potentially trigger a customer impacting process .
    taxAccountDetails: Option[CdcsCreateCaseRequestTaxAccountDetails]
  )

  object CdcsCreateCaseRequestTaxAccount {

    implicit val format: OFormat[CdcsCreateCaseRequestTaxAccount] = Json.format[CdcsCreateCaseRequestTaxAccount]
  }

  case class CdcsCreateCaseRequestDebtItem(
    chargeReference: CdcsCreateCaseRequestChargeReference,
    amount: CdcsCreateCaseRequestDebtItemAmount,
    charges: List[CdcsCreateCaseRequestCharge],
    parentChargeReference: Option[CdcsCreateCaseRequestParentChargeReference]
  )

  object CdcsCreateCaseRequestDebtItem {
    implicit val format: OFormat[CdcsCreateCaseRequestDebtItem] = Json.format[CdcsCreateCaseRequestDebtItem]

  }

  case class CdcsCreateCaseRequestCharge(
    mainTrans: CdcsCreateCaseRequestMainTrans,
    subTrans: CdcsCreateCaseRequestSubTrans,
    outstandingAmount: CdcsCreateCaseRequestOutstandingAmount,
    dueDate: CdcsCreateCaseRequestChargeDueDate,
    source: Option[CdcsCreateCaseRequestChargeSource],
    parentMainTrans: Option[CdcsCreateCaseRequestParentMainTrans],
    periodFrom: Option[CdcsCreateCaseRequestTaxPeriodFrom],
    periodTo: Option[CdcsCreateCaseRequestTaxPeriodTo],
    interestStartDate: Option[CdcsCreateCaseRequestInterestStartDate]
  )

  object CdcsCreateCaseRequestCharge {
    implicit val format: OFormat[CdcsCreateCaseRequestCharge] = Json.format[CdcsCreateCaseRequestCharge]
  }

  case class CdcsCreateCaseRequestTaxAccountDetails(districtNumber: Option[CdcsCreateCaseRequestDistrictNumber])

  object CdcsCreateCaseRequestTaxAccountDetails {
    implicit val format: OFormat[CdcsCreateCaseRequestTaxAccountDetails] =
      Json.format[CdcsCreateCaseRequestTaxAccountDetails]

  }
}
