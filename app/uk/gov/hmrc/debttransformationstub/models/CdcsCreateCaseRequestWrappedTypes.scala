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

import enumeratum.values.{ IntEnum, IntEnumEntry, IntPlayJsonValueEnum }
import enumeratum.{ Enum, EnumEntry, PlayJsonEnum }
import play.api.libs.json.{ Format, Json }
import java.time.LocalDate
import scala.collection.immutable

object CdcsCreateCaseRequestWrappedTypes {

  // String Wrappers
  final case class CdcsCreateCaseRequestTtpArrangementId(value: String)
  object CdcsCreateCaseRequestTtpArrangementId {
    implicit val format: Format[CdcsCreateCaseRequestTtpArrangementId] =
      Json.valueFormat[CdcsCreateCaseRequestTtpArrangementId]
  }

  final case class CdcsCreateCaseRequestAddressType(value: String)
  object CdcsCreateCaseRequestAddressType {
    implicit val format: Format[CdcsCreateCaseRequestAddressType] = Json.valueFormat[CdcsCreateCaseRequestAddressType]
  }

  final case class CdcsCreateCaseRequestIdType(value: String)
  object CdcsCreateCaseRequestIdType {
    implicit val format: Format[CdcsCreateCaseRequestIdType] = Json.valueFormat[CdcsCreateCaseRequestIdType]
  }

  final case class CdcsCreateCaseRequestRegimeType(value: String)
  object CdcsCreateCaseRequestRegimeType {
    implicit val format: Format[CdcsCreateCaseRequestRegimeType] = Json.valueFormat[CdcsCreateCaseRequestRegimeType]
  }

  final case class CdcsCreateCaseRequestAAId(value: String)
  object CdcsCreateCaseRequestAAId {
    implicit val format: Format[CdcsCreateCaseRequestAAId] = Json.valueFormat[CdcsCreateCaseRequestAAId]
  }

  final case class CdcsCreateCaseRequestPaymentReference(value: String)
  object CdcsCreateCaseRequestPaymentReference {
    implicit val format: Format[CdcsCreateCaseRequestPaymentReference] =
      Json.valueFormat[CdcsCreateCaseRequestPaymentReference]
  }

  final case class CdcsCreateCaseRequestInitialPaymentReference(value: String)
  object CdcsCreateCaseRequestInitialPaymentReference {
    implicit val format: Format[CdcsCreateCaseRequestInitialPaymentReference] =
      Json.valueFormat[CdcsCreateCaseRequestInitialPaymentReference]
  }

  final case class CdcsCreateCaseRequestDebtItemChargeId(value: String)
  object CdcsCreateCaseRequestDebtItemChargeId {
    implicit val format: Format[CdcsCreateCaseRequestDebtItemChargeId] =
      Json.valueFormat[CdcsCreateCaseRequestDebtItemChargeId]
  }

  final case class CdcsCreateCaseRequestUpstreamErrorCode(value: String)
  object CdcsCreateCaseRequestUpstreamErrorCode {
    implicit val format: Format[CdcsCreateCaseRequestUpstreamErrorCode] =
      Json.valueFormat[CdcsCreateCaseRequestUpstreamErrorCode]
  }

  final case class CdcsCreateCaseRequestUpstreamErrorDescription(value: String)
  object CdcsCreateCaseRequestUpstreamErrorDescription {
    implicit val format: Format[CdcsCreateCaseRequestUpstreamErrorDescription] =
      Json.valueFormat[CdcsCreateCaseRequestUpstreamErrorDescription]
  }

  final case class CdcsCreateCaseRequestTitle(value: String)
  object CdcsCreateCaseRequestTitle {
    implicit val format: Format[CdcsCreateCaseRequestTitle] = Json.valueFormat[CdcsCreateCaseRequestTitle]
  }

  final case class CdcsCreateCaseRequestInitials(value: String)
  object CdcsCreateCaseRequestInitials {
    implicit val format: Format[CdcsCreateCaseRequestInitials] = Json.valueFormat[CdcsCreateCaseRequestInitials]
  }

  final case class CdcsCreateCaseRequestFirstName(value: String)
  object CdcsCreateCaseRequestFirstName {
    implicit val format: Format[CdcsCreateCaseRequestFirstName] = Json.valueFormat[CdcsCreateCaseRequestFirstName]
  }

  final case class CdcsCreateCaseRequestMiddleName(value: String)
  object CdcsCreateCaseRequestMiddleName {
    implicit val format: Format[CdcsCreateCaseRequestMiddleName] = Json.valueFormat[CdcsCreateCaseRequestMiddleName]
  }

  final case class CdcsCreateCaseRequestLastName(value: String)
  object CdcsCreateCaseRequestLastName {
    def empty: CdcsCreateCaseRequestLastName = CdcsCreateCaseRequestLastName("")
    implicit val format: Format[CdcsCreateCaseRequestLastName] = Json.valueFormat[CdcsCreateCaseRequestLastName]
  }

  final case class CdcsCreateCaseRequestChargeReference(value: String)
  object CdcsCreateCaseRequestChargeReference {
    def empty: CdcsCreateCaseRequestChargeReference = CdcsCreateCaseRequestChargeReference("")
    implicit val format: Format[CdcsCreateCaseRequestChargeReference] =
      Json.valueFormat[CdcsCreateCaseRequestChargeReference]
  }

  final case class CdcsCreateCaseRequestParentChargeReference(value: String)
  object CdcsCreateCaseRequestParentChargeReference {
    implicit val format: Format[CdcsCreateCaseRequestParentChargeReference] =
      Json.valueFormat[CdcsCreateCaseRequestParentChargeReference]
  }

  final case class CdcsCreateCaseRequestMainTrans(value: String)
  object CdcsCreateCaseRequestMainTrans {
    def empty: CdcsCreateCaseRequestMainTrans = CdcsCreateCaseRequestMainTrans("")
    implicit val format: Format[CdcsCreateCaseRequestMainTrans] = Json.valueFormat[CdcsCreateCaseRequestMainTrans]
  }

  final case class CdcsCreateCaseRequestSubTrans(value: String)
  object CdcsCreateCaseRequestSubTrans {
    def empty: CdcsCreateCaseRequestSubTrans = CdcsCreateCaseRequestSubTrans("")
    implicit val format: Format[CdcsCreateCaseRequestSubTrans] = Json.valueFormat[CdcsCreateCaseRequestSubTrans]
  }

  final case class CdcsCreateCaseRequestParentMainTrans(value: String)
  object CdcsCreateCaseRequestParentMainTrans {
    implicit val format: Format[CdcsCreateCaseRequestParentMainTrans] =
      Json.valueFormat[CdcsCreateCaseRequestParentMainTrans]
  }

  final case class CdcsCreateCaseRequestDistrictNumber(value: String)
  object CdcsCreateCaseRequestDistrictNumber {
    implicit val format: Format[CdcsCreateCaseRequestDistrictNumber] =
      Json.valueFormat[CdcsCreateCaseRequestDistrictNumber]
  }

  final case class CdcsCreateCaseRequestAddressLine(value: String)
  object CdcsCreateCaseRequestAddressLine {
    def empty: CdcsCreateCaseRequestAddressLine = CdcsCreateCaseRequestAddressLine("")
    implicit val format: Format[CdcsCreateCaseRequestAddressLine] = Json.valueFormat[CdcsCreateCaseRequestAddressLine]
  }

  final case class CdcsCreateCaseRequestPostCode(value: String)
  object CdcsCreateCaseRequestPostCode {
    implicit val format: Format[CdcsCreateCaseRequestPostCode] = Json.valueFormat[CdcsCreateCaseRequestPostCode]
  }

  final case class CdcsCreateCaseRequestTelephoneNumber(value: String)
  object CdcsCreateCaseRequestTelephoneNumber {
    def empty: CdcsCreateCaseRequestTelephoneNumber = CdcsCreateCaseRequestTelephoneNumber("")
    implicit val format: Format[CdcsCreateCaseRequestTelephoneNumber] =
      Json.valueFormat[CdcsCreateCaseRequestTelephoneNumber]
  }

  final case class CdcsCreateCaseRequestEmail(value: String)
  object CdcsCreateCaseRequestEmail {
    def empty: CdcsCreateCaseRequestEmail = CdcsCreateCaseRequestEmail("")
    implicit val format: Format[CdcsCreateCaseRequestEmail] = Json.valueFormat[CdcsCreateCaseRequestEmail]
  }

  final case class CdcsCreateCaseRequestIdValue(value: String)
  object CdcsCreateCaseRequestIdValue {
    implicit val format: Format[CdcsCreateCaseRequestIdValue] = Json.valueFormat[CdcsCreateCaseRequestIdValue]
  }

  // Int Wrappers
  final case class CdcsCreateCaseRequestPlanDuration(value: Int)
  object CdcsCreateCaseRequestPlanDuration {
    implicit val format: Format[CdcsCreateCaseRequestPlanDuration] = Json.valueFormat[CdcsCreateCaseRequestPlanDuration]
  }

  final case class CdcsCreateCaseRequestNumberOfInstalments(value: Int)
  object CdcsCreateCaseRequestNumberOfInstalments {
    implicit val format: Format[CdcsCreateCaseRequestNumberOfInstalments] =
      Json.valueFormat[CdcsCreateCaseRequestNumberOfInstalments]
  }

  final case class CdcsCreateCaseRequestInstalmentNumber(value: Int)
  object CdcsCreateCaseRequestInstalmentNumber {
    implicit val format: Format[CdcsCreateCaseRequestInstalmentNumber] =
      Json.valueFormat[CdcsCreateCaseRequestInstalmentNumber]
  }

  // BigInt Wrappers
  final case class CdcsCreateCaseRequestInstalmentAmount(value: BigInt)
  object CdcsCreateCaseRequestInstalmentAmount {
    implicit val format: Format[CdcsCreateCaseRequestInstalmentAmount] =
      Json.valueFormat[CdcsCreateCaseRequestInstalmentAmount]
  }

  final case class CdcsCreateCaseRequestTotalDebtIncInt(value: BigInt)
  object CdcsCreateCaseRequestTotalDebtIncInt {
    implicit val format: Format[CdcsCreateCaseRequestTotalDebtIncInt] =
      Json.valueFormat[CdcsCreateCaseRequestTotalDebtIncInt]
  }

  final case class CdcsCreateCaseRequestTotalInterest(value: BigInt)
  object CdcsCreateCaseRequestTotalInterest {
    implicit val format: Format[CdcsCreateCaseRequestTotalInterest] =
      Json.valueFormat[CdcsCreateCaseRequestTotalInterest]
  }

  final case class CdcsCreateCaseRequestInterestAccrued(value: BigInt)
  object CdcsCreateCaseRequestInterestAccrued {
    implicit val format: Format[CdcsCreateCaseRequestInterestAccrued] =
      Json.valueFormat[CdcsCreateCaseRequestInterestAccrued]
  }

  final case class CdcsCreateCaseRequestPlanInterest(value: BigInt)
  object CdcsCreateCaseRequestPlanInterest {
    implicit val format: Format[CdcsCreateCaseRequestPlanInterest] = Json.valueFormat[CdcsCreateCaseRequestPlanInterest]
  }

  final case class CdcsCreateCaseRequestInitialPaymentAmount(value: BigInt)
  object CdcsCreateCaseRequestInitialPaymentAmount {
    implicit val format: Format[CdcsCreateCaseRequestInitialPaymentAmount] =
      Json.valueFormat[CdcsCreateCaseRequestInitialPaymentAmount]
  }

  final case class CdcsCreateCaseRequestInstalmentAmountDue(value: BigInt)
  object CdcsCreateCaseRequestInstalmentAmountDue {
    implicit val format: Format[CdcsCreateCaseRequestInstalmentAmountDue] =
      Json.valueFormat[CdcsCreateCaseRequestInstalmentAmountDue]
  }

  final case class CdcsCreateCaseRequestInstalmentExpectedPayment(value: BigInt)
  object CdcsCreateCaseRequestInstalmentExpectedPayment {
    implicit val format: Format[CdcsCreateCaseRequestInstalmentExpectedPayment] =
      Json.valueFormat[CdcsCreateCaseRequestInstalmentExpectedPayment]
  }

  final case class CdcsCreateCaseRequestInstalmentInterestAccrued(value: BigInt)
  object CdcsCreateCaseRequestInstalmentInterestAccrued {
    implicit val format: Format[CdcsCreateCaseRequestInstalmentInterestAccrued] =
      Json.valueFormat[CdcsCreateCaseRequestInstalmentInterestAccrued]
  }

  final case class CdcsCreateCaseRequestInstalmentBalance(value: BigInt)
  object CdcsCreateCaseRequestInstalmentBalance {
    implicit val format: Format[CdcsCreateCaseRequestInstalmentBalance] =
      Json.valueFormat[CdcsCreateCaseRequestInstalmentBalance]
  }

  final case class CdcsCreateCaseRequestAmountDue(value: BigInt)
  object CdcsCreateCaseRequestAmountDue {
    implicit val format: Format[CdcsCreateCaseRequestAmountDue] = Json.valueFormat[CdcsCreateCaseRequestAmountDue]
  }

  // BigDecimal Wrappers
  final case class CdcsCreateCaseRequestOutstandingAmount(value: BigDecimal)
  object CdcsCreateCaseRequestOutstandingAmount {
    implicit val format: Format[CdcsCreateCaseRequestOutstandingAmount] =
      Json.valueFormat[CdcsCreateCaseRequestOutstandingAmount]
  }

  final case class CdcsCreateCaseRequestDebtItemAmount(value: BigDecimal)
  object CdcsCreateCaseRequestDebtItemAmount {
    implicit val format: Format[CdcsCreateCaseRequestDebtItemAmount] =
      Json.valueFormat[CdcsCreateCaseRequestDebtItemAmount]
  }

  // LocalDate Wrappers
  final case class CdcsCreateCaseRequestInstalmentStartDate(value: LocalDate)
  object CdcsCreateCaseRequestInstalmentStartDate {
    implicit val format: Format[CdcsCreateCaseRequestInstalmentStartDate] =
      Json.valueFormat[CdcsCreateCaseRequestInstalmentStartDate]
  }

  final case class CdcsCreateCaseRequestChargeDueDate(value: LocalDate)
  object CdcsCreateCaseRequestChargeDueDate {
    implicit val format: Format[CdcsCreateCaseRequestChargeDueDate] =
      Json.valueFormat[CdcsCreateCaseRequestChargeDueDate]
  }

  final case class CdcsCreateCaseRequestInstalmentDueDate(value: LocalDate)
  object CdcsCreateCaseRequestInstalmentDueDate {
    implicit val format: Format[CdcsCreateCaseRequestInstalmentDueDate] =
      Json.valueFormat[CdcsCreateCaseRequestInstalmentDueDate]
  }

  final case class CdcsCreateCaseRequestPaymentDueDate(value: LocalDate)
  object CdcsCreateCaseRequestPaymentDueDate {
    implicit val format: Format[CdcsCreateCaseRequestPaymentDueDate] =
      Json.valueFormat[CdcsCreateCaseRequestPaymentDueDate]
  }

  final case class CdcsCreateCaseRequestDateOfBirth(value: LocalDate)
  object CdcsCreateCaseRequestDateOfBirth {
    implicit val format: Format[CdcsCreateCaseRequestDateOfBirth] = Json.valueFormat[CdcsCreateCaseRequestDateOfBirth]
  }

  final case class CdcsCreateCaseRequestTaxPeriodFrom(value: LocalDate)
  object CdcsCreateCaseRequestTaxPeriodFrom {
    implicit val format: Format[CdcsCreateCaseRequestTaxPeriodFrom] =
      Json.valueFormat[CdcsCreateCaseRequestTaxPeriodFrom]
  }

  final case class CdcsCreateCaseRequestTaxPeriodTo(value: LocalDate)
  object CdcsCreateCaseRequestTaxPeriodTo {
    implicit val format: Format[CdcsCreateCaseRequestTaxPeriodTo] = Json.valueFormat[CdcsCreateCaseRequestTaxPeriodTo]
  }

  final case class CdcsCreateCaseRequestInterestStartDate(value: LocalDate)
  object CdcsCreateCaseRequestInterestStartDate {
    implicit val format: Format[CdcsCreateCaseRequestInterestStartDate] =
      Json.valueFormat[CdcsCreateCaseRequestInterestStartDate]
  }

  final case class CdcsCreateCaseRequestInitialPaymentDate(value: LocalDate)
  object CdcsCreateCaseRequestInitialPaymentDate {
    implicit val format: Format[CdcsCreateCaseRequestInitialPaymentDate] =
      Json.valueFormat[CdcsCreateCaseRequestInitialPaymentDate]
  }

  sealed abstract class CdcsCreateCaseRequestPlanFrequency(override val entryName: String) extends EnumEntry
  object CdcsCreateCaseRequestPlanFrequency
      extends Enum[CdcsCreateCaseRequestPlanFrequency] with PlayJsonEnum[CdcsCreateCaseRequestPlanFrequency] {

    val values: immutable.IndexedSeq[CdcsCreateCaseRequestPlanFrequency] = findValues

    case object Single extends CdcsCreateCaseRequestPlanFrequency("single")
    case object Weekly extends CdcsCreateCaseRequestPlanFrequency("weekly")
    case object TwoWeekly extends CdcsCreateCaseRequestPlanFrequency("2Weekly")
    case object FourWeekly extends CdcsCreateCaseRequestPlanFrequency("4Weekly")
    case object Quarterly extends CdcsCreateCaseRequestPlanFrequency("quarterly")
    case object Monthly extends CdcsCreateCaseRequestPlanFrequency("monthly")
    case object Annually extends CdcsCreateCaseRequestPlanFrequency("annually")

  }

  sealed abstract class CdcsCreateCaseRequestSourceSystem(override val entryName: String) extends EnumEntry
  object CdcsCreateCaseRequestSourceSystem
      extends Enum[CdcsCreateCaseRequestSourceSystem] with PlayJsonEnum[CdcsCreateCaseRequestSourceSystem] {

    val values: immutable.IndexedSeq[CdcsCreateCaseRequestSourceSystem] = findValues

    case object Cesa extends CdcsCreateCaseRequestSourceSystem("CESA")
    case object Etmp extends CdcsCreateCaseRequestSourceSystem("ETMP")
    case object Ndds extends CdcsCreateCaseRequestSourceSystem("NDDS")
  }

  sealed abstract class CdcsCreateCaseRequestChargeSource(override val entryName: String) extends EnumEntry
  object CdcsCreateCaseRequestChargeSource
      extends Enum[CdcsCreateCaseRequestChargeSource] with PlayJsonEnum[CdcsCreateCaseRequestChargeSource] {

    val values: immutable.IndexedSeq[CdcsCreateCaseRequestChargeSource] = findValues

    case object Cesa extends CdcsCreateCaseRequestChargeSource("CESA")
    case object Etmp extends CdcsCreateCaseRequestChargeSource("ETMP")
  }

  sealed abstract class CdcsCreateCaseRequestPaymentMethod(override val entryName: String) extends EnumEntry
  object CdcsCreateCaseRequestPaymentMethod
      extends Enum[CdcsCreateCaseRequestPaymentMethod] with PlayJsonEnum[CdcsCreateCaseRequestPaymentMethod] {

    val values: immutable.IndexedSeq[CdcsCreateCaseRequestPaymentMethod] = findValues

    case object DirectDebit extends CdcsCreateCaseRequestPaymentMethod("directDebit")
    case object Bacs extends CdcsCreateCaseRequestPaymentMethod("BACS")
    case object Cheque extends CdcsCreateCaseRequestPaymentMethod("cheque")
    case object CardPayment extends CdcsCreateCaseRequestPaymentMethod("cardPayment")
    case object OngoingAward extends CdcsCreateCaseRequestPaymentMethod("Ongoing award")
  }

  sealed abstract class CdcsCreateCaseRequestPaymentPlanType(override val entryName: String) extends EnumEntry
  object CdcsCreateCaseRequestPaymentPlanType
      extends Enum[CdcsCreateCaseRequestPaymentPlanType] with PlayJsonEnum[CdcsCreateCaseRequestPaymentPlanType] {

    val values: immutable.IndexedSeq[CdcsCreateCaseRequestPaymentPlanType] = findValues

    case object TimeToPay extends CdcsCreateCaseRequestPaymentPlanType("timeToPay")
    case object InstalmentOrder extends CdcsCreateCaseRequestPaymentPlanType("instalmentOrder")
    case object ChildBenefits extends CdcsCreateCaseRequestPaymentPlanType("childBenefits")
    case object FieldCollections extends CdcsCreateCaseRequestPaymentPlanType("fieldCollections")
    case object Lfc extends CdcsCreateCaseRequestPaymentPlanType("LFC")
  }

  sealed abstract class CdcsCreateCaseRequestAddressTypeReference(
    val value: Int,
    val idType: CdcsCreateCaseRequestAddressType
  ) extends IntEnumEntry
  object CdcsCreateCaseRequestAddressTypeReference
      extends IntEnum[CdcsCreateCaseRequestAddressTypeReference]
      with IntPlayJsonValueEnum[CdcsCreateCaseRequestAddressTypeReference] {
    val values: immutable.IndexedSeq[CdcsCreateCaseRequestAddressTypeReference] = findValues

    case object TAXPAYER
        extends CdcsCreateCaseRequestAddressTypeReference(1, CdcsCreateCaseRequestAddressType("TAXPAYER"))

    case object XXDEFAULT
        extends CdcsCreateCaseRequestAddressTypeReference(2, CdcsCreateCaseRequestAddressType("XXDEFAULT"))

  }

  sealed abstract class CdcsCreateCaseRequestIdTypeReference(val value: Int, val idType: CdcsCreateCaseRequestIdType)
      extends IntEnumEntry
  object CdcsCreateCaseRequestIdTypeReference
      extends IntEnum[CdcsCreateCaseRequestIdTypeReference]
      with IntPlayJsonValueEnum[CdcsCreateCaseRequestIdTypeReference] {
    val values: immutable.IndexedSeq[CdcsCreateCaseRequestIdTypeReference] = findValues
    case object UTR extends CdcsCreateCaseRequestIdTypeReference(73, CdcsCreateCaseRequestIdType("UTR"))
  }

  sealed abstract class CdcsCreateCaseRequestRegimeTypeReference(
    val value: Int,
    val regimeType: CdcsCreateCaseRequestRegimeType
  ) extends IntEnumEntry
  object CdcsCreateCaseRequestRegimeTypeReference
      extends IntEnum[CdcsCreateCaseRequestRegimeTypeReference]
      with IntPlayJsonValueEnum[CdcsCreateCaseRequestRegimeTypeReference] {

    val values: immutable.IndexedSeq[CdcsCreateCaseRequestRegimeTypeReference] = findValues

    case object AGL extends CdcsCreateCaseRequestRegimeTypeReference(1, CdcsCreateCaseRequestRegimeType("AGL"))
    case object AGSV extends CdcsCreateCaseRequestRegimeTypeReference(2, CdcsCreateCaseRequestRegimeType("AGSV"))
    case object ALL extends CdcsCreateCaseRequestRegimeTypeReference(3, CdcsCreateCaseRequestRegimeType("ALL"))
    case object AMC extends CdcsCreateCaseRequestRegimeTypeReference(4, CdcsCreateCaseRequestRegimeType("AMC"))
    case object AMLS extends CdcsCreateCaseRequestRegimeTypeReference(5, CdcsCreateCaseRequestRegimeType("AMLS"))
    case object APD extends CdcsCreateCaseRequestRegimeTypeReference(6, CdcsCreateCaseRequestRegimeType("APD"))
    case object ATED extends CdcsCreateCaseRequestRegimeTypeReference(7, CdcsCreateCaseRequestRegimeType("ATED"))
    case object AWRS extends CdcsCreateCaseRequestRegimeTypeReference(8, CdcsCreateCaseRequestRegimeType("AWRS"))
    case object BD extends CdcsCreateCaseRequestRegimeTypeReference(9, CdcsCreateCaseRequestRegimeType("BD"))
    case object CBC extends CdcsCreateCaseRequestRegimeTypeReference(10, CdcsCreateCaseRequestRegimeType("CBC"))
    case object CDS extends CdcsCreateCaseRequestRegimeTypeReference(11, CdcsCreateCaseRequestRegimeType("CDS"))
    case object CGT extends CdcsCreateCaseRequestRegimeTypeReference(12, CdcsCreateCaseRequestRegimeType("CGT"))
    case object CHB extends CdcsCreateCaseRequestRegimeTypeReference(13, CdcsCreateCaseRequestRegimeType("CHB"))
    case object CHR extends CdcsCreateCaseRequestRegimeTypeReference(14, CdcsCreateCaseRequestRegimeType("CHR"))
    case object CSSP extends CdcsCreateCaseRequestRegimeTypeReference(15, CdcsCreateCaseRequestRegimeType("CSSP"))
    case object CT extends CdcsCreateCaseRequestRegimeTypeReference(16, CdcsCreateCaseRequestRegimeType("CT"))
    case object DAC extends CdcsCreateCaseRequestRegimeTypeReference(17, CdcsCreateCaseRequestRegimeType("DAC"))
    case object DDS extends CdcsCreateCaseRequestRegimeTypeReference(18, CdcsCreateCaseRequestRegimeType("DDS"))
    case object DEA extends CdcsCreateCaseRequestRegimeTypeReference(19, CdcsCreateCaseRequestRegimeType("DEA"))
    case object DST extends CdcsCreateCaseRequestRegimeTypeReference(20, CdcsCreateCaseRequestRegimeType("DST"))
    case object EI extends CdcsCreateCaseRequestRegimeTypeReference(21, CdcsCreateCaseRequestRegimeType("EI"))
    case object ERS extends CdcsCreateCaseRequestRegimeTypeReference(22, CdcsCreateCaseRequestRegimeType("ERS"))
    case object FHDD extends CdcsCreateCaseRequestRegimeTypeReference(23, CdcsCreateCaseRequestRegimeType("FHDD"))
    case object GD extends CdcsCreateCaseRequestRegimeTypeReference(24, CdcsCreateCaseRequestRegimeType("GD"))
    case object GTR extends CdcsCreateCaseRequestRegimeTypeReference(25, CdcsCreateCaseRequestRegimeType("GTR"))
    case object IHT extends CdcsCreateCaseRequestRegimeTypeReference(26, CdcsCreateCaseRequestRegimeType("IHT"))
    case object IPT extends CdcsCreateCaseRequestRegimeTypeReference(27, CdcsCreateCaseRequestRegimeType("IPT"))
    case object ITSA extends CdcsCreateCaseRequestRegimeTypeReference(28, CdcsCreateCaseRequestRegimeType("ITSA"))
    case object LD extends CdcsCreateCaseRequestRegimeTypeReference(29, CdcsCreateCaseRequestRegimeType("LD"))
    case object LFT extends CdcsCreateCaseRequestRegimeTypeReference(30, CdcsCreateCaseRequestRegimeType("LFT"))
    case object LISA extends CdcsCreateCaseRequestRegimeTypeReference(31, CdcsCreateCaseRequestRegimeType("LISA"))
    case object MGD extends CdcsCreateCaseRequestRegimeTypeReference(32, CdcsCreateCaseRequestRegimeType("MGD"))
    case object NI extends CdcsCreateCaseRequestRegimeTypeReference(33, CdcsCreateCaseRequestRegimeType("NI"))
    case object NLIJ extends CdcsCreateCaseRequestRegimeTypeReference(34, CdcsCreateCaseRequestRegimeType("NLIJ"))
    case object PARC extends CdcsCreateCaseRequestRegimeTypeReference(35, CdcsCreateCaseRequestRegimeType("PARC"))
    case object PAYE extends CdcsCreateCaseRequestRegimeTypeReference(36, CdcsCreateCaseRequestRegimeType("PAYE"))
    case object PNGR extends CdcsCreateCaseRequestRegimeTypeReference(37, CdcsCreateCaseRequestRegimeType("PNGR"))
    case object PODA extends CdcsCreateCaseRequestRegimeTypeReference(38, CdcsCreateCaseRequestRegimeType("PODA"))
    case object PODP extends CdcsCreateCaseRequestRegimeTypeReference(39, CdcsCreateCaseRequestRegimeType("PODP"))
    case object PODS extends CdcsCreateCaseRequestRegimeTypeReference(40, CdcsCreateCaseRequestRegimeType("PODS"))
    case object PPT extends CdcsCreateCaseRequestRegimeTypeReference(41, CdcsCreateCaseRequestRegimeType("PPT"))
    case object SA extends CdcsCreateCaseRequestRegimeTypeReference(42, CdcsCreateCaseRequestRegimeType("SA"))
    case object SIMP extends CdcsCreateCaseRequestRegimeTypeReference(43, CdcsCreateCaseRequestRegimeType("SIMP"))
    case object TAVC extends CdcsCreateCaseRequestRegimeTypeReference(44, CdcsCreateCaseRequestRegimeType("TAVC"))
    case object TRS extends CdcsCreateCaseRequestRegimeTypeReference(45, CdcsCreateCaseRequestRegimeType("TRS"))
    case object UTC extends CdcsCreateCaseRequestRegimeTypeReference(46, CdcsCreateCaseRequestRegimeType("UTC"))
    case object VAT extends CdcsCreateCaseRequestRegimeTypeReference(47, CdcsCreateCaseRequestRegimeType("VAT"))
    case object VATC extends CdcsCreateCaseRequestRegimeTypeReference(48, CdcsCreateCaseRequestRegimeType("VATC"))
    case object ZAIR extends CdcsCreateCaseRequestRegimeTypeReference(49, CdcsCreateCaseRequestRegimeType("ZAIR"))
    case object ZBFP extends CdcsCreateCaseRequestRegimeTypeReference(50, CdcsCreateCaseRequestRegimeType("ZBFP"))
    case object ZFD extends CdcsCreateCaseRequestRegimeTypeReference(51, CdcsCreateCaseRequestRegimeType("ZFD"))
    case object ZGRF extends CdcsCreateCaseRequestRegimeTypeReference(52, CdcsCreateCaseRequestRegimeType("ZGRF"))
    case object ZSDL extends CdcsCreateCaseRequestRegimeTypeReference(53, CdcsCreateCaseRequestRegimeType("ZSDL"))
    case object ZVTR extends CdcsCreateCaseRequestRegimeTypeReference(54, CdcsCreateCaseRequestRegimeType("ZVTR"))

  }
}
