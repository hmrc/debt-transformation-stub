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

package uk.gov.hmrc.debttransformationstub.services

import play.api.Logging
import uk.gov.hmrc.debttransformationstub.models.HodReferralRequest

import java.security.spec.PKCS8EncodedKeySpec
import java.security.{ KeyFactory, PrivateKey }
import java.util.Base64
import javax.crypto.spec.{ IvParameterSpec, SecretKeySpec }
import javax.crypto.{ Cipher, SecretKey }
import javax.inject.{ Inject, Singleton }
import scala.util.Try

@Singleton
class HodReferralDecryptionService @Inject() () extends Logging {

  // Test private keys from time-to-pay docs/hod-referral-test-keys.md
  // These are FOR TESTING PURPOSES ONLY and must never be used in production
  private val primaryPrivateKeyBase64: String =
    "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCHo+cNsh5N4eHquuiOG7fRDbTK" +
      "/ODgv5XSI9iifWxQSYvc3DC+QPiwuP6IWrxvJTMpsC/+FEckX5D9jkjiCtxlM5jWNw93RgPS5yOJ" +
      "XBWa1/fbwXNJduOVbPS9H37jr0jp9LO6ePtiqplDDfjR8MsVWXq++SbfVUeEgHh9keIMxstGH7dg" +
      "vMfJdCRYmbf0T2rNrpxc5OUPC3VCDkdVcLXKU1FUKRd131HqjKWcm9Hl8LGn2R9Gyqv0wAuJI616" +
      "F737un/taN1DYIfYjxXaJr/wdz+adsz4yCUsMu6f1DeReScyvFvOt3tio/5Fwj351/j72IXPcdG8" +
      "Zg3tuMeblYdtAgMBAAECggEBAIKm/w1zL7uHQ8yNQyp3SoUE9cu874OtJ0wmGSrTMzzWyvDv68em" +
      "36WiXBLNi8png7O5MVFFCQ3hO9DweAW7vnWA0MwudyhFebNxzwold+4R735vBXSTHz60l72AFAKO" +
      "ueYZPfH1TPfKKhyr3aujjIrBCPnhFm9zPpNECEuz9JCml81jal2TDZZDKAT65ybD0AJTtAOc7K+n" +
      "Iksi8zQZ57tjtWbxfXj6D/KyBh5OKTao6WOU7ASBTt3byLhc/aZ5ZmaM5QSN++XrGBN4uSMkzIzC" +
      "sBieP/Lnb05dTHCBPe0yOD0DCc8SYAOSAPk30B3+ZOfGWYA1NnrU64X3sRNsSMECgYEAyd6uiadf" +
      "roKXQErjFP4xHzabEc1OiaqThpGsqomQFwV1MrisNt7gJslpFhi63xdRqZYhPytTajc+Cye9Kt8z" +
      "IHU5Reu0ivYKXlubuAVsF5ScFs8ZmdnL54uivewBNg4ixD+EdNgDurKWPFcl64dlPcHfxqSuxn4x" +
      "77OjyTXcNX0CgYEArALm3GDmN2omMpm3J7Whfmo0li6hBnvjf/g5bEQmSo0Rmkecs6WcJWNar27n" +
      "SfPaQlzF1umC2XUFO7aVYrIofmWhlfFjzBqbz+l1pz2pJNvceETtzD7AzArqYJJ7k1k8C6Rn/ao2" +
      "zdCjXTWSSbA4gnOKN8xdBov3e0XEwEHcfLECgYBh6izmf9SdJKKQgMJccF8LMMVOgTLS+3XLE+WY" +
      "YlkNkYwjKgf+JWS7632ZRi1+ACWQCE3gAffX0Su65W+P1+tIlxNMouNTc7vbwLtrKtYfKen79kAI" +
      "q+eHS+eID20W9wxN/DEXK5/DctQEOJEgCPBGYD9WbpKHnLZD+fI7qnBBUQKBgEd7cTsWHbXbrAIN" +
      "NTsQVQt20WG0AQDzzSgqHJrse0kx9KW7qthRM2GqN05+dSmbaBF6AlF8ev8pjUIyb0Qzq3ZTf+IT" +
      "DZkNWcD+UQFuM3N41tc4NeZSEcb5pkr5tNcYF0bzTK/y45GGac1bbO6oh+NWZpMnn2UQF4moBcqr" +
      "1BgRAoGBAK4u1/yMqn0sjN9GSMQfnezQYFhNTfykb/lJ14CpasZF72GM9BAEehWPws5D9JAzj+Bj" +
      "aTo3i90hL6um/wcZKkCmaMj+3+oqtPePIZuErXTSY5FvD88NP5dX4nQvhtibiUAM14OPHQ31rIhh" +
      "aArNwWqOY8zO3hS/g5+Qstsfeb1w"

  private val fallbackPrivateKeyBase64: String =
    "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDAZmQvHyrtdA+nmk2BPcQ6yGJP" +
      "r+7jgVDvVFQpSUyjB94yTOjTSp3k8C41IvyiSlKrBZTZflBquc/hXq9Uurlg+BT3V2ODsM6ccyGN" +
      "Us+qkerbf90bnFG9tgcq1nBCqOt2cS/adZxUR0eglDh0JnTRBDCe2D8qGQqiLVuBKizZTN8Wnf93" +
      "whGTQP7wXZQwEQkaM+F5BQdxKx6OhTH18cO+XNDr3QBUGf2b12prb46hwNhnWHPvOOrbBuK0unMD" +
      "om7xL0dWu1wM9fnjXDS71xJSWemfPygT32jIvM9fJH4MZv021Z2Nf0CcItLkXq9tqdYLrhKpIxIh" +
      "W/MPNfkRla1lAgMBAAECggEBAKYqiFHuMCv+NdsBt/hr+QLvt4CtKLuSHu1kPn+dz7NqAywcImUB" +
      "p5ZnHPFFcz1SxUzxOBoTLq7OJcy18uVRkvM9rxXjgo3k8LxL7ulYYQQmm9jQ+Ix1GF+pS01jAHeB" +
      "3kJgBP0+4DIlkaYM/SjLYc53OOqnRv47vGROmh3W5/8PFp4bMquGAlrS04x7QQIwbT0qWWWqrXae" +
      "hrEM4VueXMJ6QpPs664j64e7Oc1WBENsDPiPyYIm9h3wUA8ui90p7xhGH6UMHgy1D/9CjHSTAvFx" +
      "9+R81Cwd1HiijOB80h6a90udwgWJpT7oSRmcGNw/XhjH1OrlQkrXeINmR4OlqUECgYEA4iI0tP4G" +
      "4HEhds2QBwpbhk7RCuEMBVwYpYgMdtp31SBATmkv3yphD4CBT3Oz+ItXxQj1x0lz/xLS9fFjMY6f" +
      "lRXJufCbCxarP4pKjDtap0Gccwlu6YX6bMON5wFcjo03mXXMxXQMeoPpWnRTBTsw1rG2YlBw/CuF" +
      "x7u7ZtCY05UCgYEA2c+eUXprjtgBeuMTinRFBlXiILy+UYxRcrMd4LZ/4xSY7gAl407EQu0TzUoB" +
      "Qje3ME2pMMCYzI1Mn9XlQGZoW2VoOKFT0Dyokx1sVZjH5ftbNNtF0Fhl0h9krZBoNLMZM2kou21v" +
      "c1GZda3fX+TCZLHznE6JVEWMjsDaKt64/pECgYAtW0EJIH6OnAJjSt3dm6U57isdrH/1v0AuS3h8" +
      "iltIwzxCvfRdJansKxFEt2dcrc4/9gkeHMyIV+U6cV08/mdhxsn8YpcmhTh5+Sn0sJ8BNzbDGBxH" +
      "+Jh/IkYfFHXVHdwOCsO0ww8Rr82pV/Wi8flcxq5XT90ppy0hH7V57NMKdQKBgQCZuJLmtDym4sIg" +
      "pZSL+cUhVIm0SyES0rJE/i8PAh49+LJ5/na+9z9CcKmyEBHVVxcdqyHGe5cbfnnesIoMqnnqTyaA" +
      "GTPa2oSq13A29Q3XvU9AfaTHByxNYMSgTjONRf16MDSEGxc2Txe8Wws0VXHwuTrSTQdVB4TM10Ti" +
      "TrBHUQKBgBHFUBvzsBOhgKquUhykBc91/kh8CvISDeYX+qFxqD3dCRvDachPWajp5E531DNKeqWO" +
      "b9w7dhe6QL6dHpoD8WdQ/s7AQ6vKDUr/0VsnAmUb+8e2SNJjPUMxyyD431DMcWesr/9RRAEMuUdq" +
      "n+7AWaXOyx0oF42mteIIV+jjvHK2"

  private val privateKeys: Seq[Array[Byte]] = Seq(
    Base64.getDecoder.decode(primaryPrivateKeyBase64),
    Base64.getDecoder.decode(fallbackPrivateKeyBase64)
  )

  /** Attempts to decrypt the HoD Referral message content using either the primary or fallback test key.
    * Returns None if decryption fails with both keys.
    */
  def decrypt(request: HodReferralRequest): Option[String] =
    privateKeys.view
      .flatMap(privateKeyBytes => tryDecrypt(request, privateKeyBytes).toOption)
      .headOption

  private def tryDecrypt(request: HodReferralRequest, privateKeyBytes: Array[Byte]): Try[String] = Try {
    val privateKey: PrivateKey = {
      val keySpec    = new PKCS8EncodedKeySpec(privateKeyBytes, "RSA")
      val keyFactory = KeyFactory.getInstance("RSA")
      keyFactory.generatePrivate(keySpec)
    }

    val rsaUnWrappingCipher: Cipher = {
      val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
      cipher.init(Cipher.DECRYPT_MODE, privateKey)
      cipher
    }

    val encryptedAesKeyBytes = Base64.getDecoder.decode(request.aesKey)
    val encryptedIvBytes     = Base64.getDecoder.decode(request.iv)
    val ciphertextBytes      = Base64.getDecoder.decode(request.messageContent)

    val aesKey: SecretKey = {
      val aesKeyBytes = rsaUnWrappingCipher.doFinal(encryptedAesKeyBytes)
      new SecretKeySpec(aesKeyBytes, 0, aesKeyBytes.length, "AES")
    }

    val initialisationVector: Array[Byte] = rsaUnWrappingCipher.doFinal(encryptedIvBytes)

    val aesDecryptionCipher: Cipher = {
      val cipher   = Cipher.getInstance("AES/CBC/PKCS5Padding")
      val ivParams = new IvParameterSpec(initialisationVector)
      cipher.init(Cipher.DECRYPT_MODE, aesKey, ivParams)
      cipher
    }

    val decryptedBytes = aesDecryptionCipher.doFinal(ciphertextBytes)
    new String(decryptedBytes, "UTF-8")
  }
}
