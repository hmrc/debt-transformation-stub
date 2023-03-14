package uk.gov.hmrc.debttransformationstub.utils

import java.util.Base64

object BuildRulesIFS extends App {

    def encodeList(notEncodedRuleValues: List[String], num: Int): Unit = {
      if (notEncodedRuleValues.isEmpty) {
        println("")
      } else {
        val rule = notEncodedRuleValues.head
        val encoded = new String(Base64.getEncoder.encodeToString(rule.getBytes()))
        println(s"service-config.rules.$num: " + encoded)
        println(s"# $rule")
        encodeList(notEncodedRuleValues.tail, num + 1)
      }
    }

    // e.g.  encodeList(
    //    List("IF mainTrans == '4794' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true",
    //    "IF mainTrans == '4797' AND subTrans == '1175' -> intRate = 0 AND interestOnlyDebt = true"),
    //    174)

}
