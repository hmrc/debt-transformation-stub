package uk.gov.hmrc.debttransformationstub.testutils

import org.scalactic.source.Position
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers._

object StringAssertionOps {
  implicit final class RichStringAssertion(str: String) {
    /** The same as the normal `shouldBe` but without any special String manipulation, for easy diffing in IntelliJ. */
    def shouldBeThatString(other: String)(implicit pos: Position): Assertion = {
      final case class StringWithBetterErrorDisplay(override val toString: String)
      StringWithBetterErrorDisplay(str) shouldBe StringWithBetterErrorDisplay(other)
    }
  }
}
