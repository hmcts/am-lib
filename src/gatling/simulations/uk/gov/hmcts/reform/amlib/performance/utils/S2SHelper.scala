package uk.gov.hmcts.reform.amlib.performance.utils

import com.warrenstrange.googleauth.GoogleAuthenticator
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object  S2SHelper {


  val getOTP =
    exec(
      session => {
        val otp: String = String.valueOf(new GoogleAuthenticator().getTotpPassword(Environment.s2sSecret))
        print("otp:::" + otp + "s2s secret::" + Environment.s2sSecret)
        session.set("OTP", otp)

      })

  val otpp = "${OTP}"

  val S2SAuthToken =

    doIf(session => session("s2sToken").asOption[String].isEmpty) {
      exec(http("AM lib gatling Token")
        .post(Environment.s2sUrl + "/lease")
        .header("Content-Type", "application/json")
        .body(StringBody(
          s"""{
       "microservice": "${Environment.s2sname}",
        "oneTimePassword": "${otpp}"
        }"""
        )).asJson
        .check(bodyString.saveAs("s2sToken"))
        .check(bodyString.saveAs("responseBody")))
  }

}
