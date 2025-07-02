package uk.gov.hmrc.debttransformationstub.models

import uk.gov.hmrc.debttransformationstub.models.CDCSChargeInfoResponse.{ Address, ChargeTypeAssessment, IndividualDetails }

final case class ChargeInfoResponse(
  processingDateTime: String,
  identification: Seq[Identification],
  individualDetails: IndividualDetails,
  addresses: Seq[Address],
  chargeTypeAssessment: Seq[ChargeTypeAssessment]
)

object CDCSChargeInfoResponse {
  final case class Identification(
    idType: String,
    idValue: String
  )

  final case class IndividualDetails(
    title: String,
    firstName: String,
    lastName: String,
    dateOfBirth: String,
    districtNumber: String,
    customerType: String,
    transitionToCDCS: Boolean
  )

  final case class Address(
    addressType: String,
    addressLine1: String,
    addressLine2: String,
    addressLine3: String,
    addressLine4: String,
    rls: Boolean,
    contactDetails: ContactDetails,
    postCode: String,
    country: String,
    postcodeHistory: Seq[PostcodeHistory]
  )

  final case class ContactDetails(
    telephoneNumber: String,
    fax: String,
    mobile: String,
    emailAddress: String,
    emailSource: String,
    altFormat: Int
  )

  final case class PostcodeHistory(
    addressPostcode: String,
    postcodeDate: String
  )

  final case class ChargeTypeAssessment(
    debtTotalAmount: BigDecimal,
    chargeReference: String,
    mainTrans: String,
    parentChargeReference: String,
    charges: Seq[Charge]
  )

  final case class Charge(
    taxPeriodFrom: String,
    taxPeriodTo: String,
    chargeType: String,
    mainType: String,
    subTrans: String,
    outstandingAmount: BigDecimal,
    dueDate: String,
    isInterestBearingCharge: Boolean,
    interestStartDate: String,
    accruedInterest: BigDecimal,
    chargeSource: String,
    parentMainTrans: String,
    originalCreationDate: String,
    tieBreaker: String,
    originalTieBreaker: String,
    saTaxYearEnd: String,
    creationDate: String,
    originalChargeType: String
  )
}
