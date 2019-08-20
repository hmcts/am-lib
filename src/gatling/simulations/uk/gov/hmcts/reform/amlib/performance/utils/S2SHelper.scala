package uk.gov.hmcts.reform.amlib.performance.utils

import com.warrenstrange.googleauth.GoogleAuthenticator
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object  S2SHelper {

  val s2sUrl : String = scala.util.Properties.envOrElse("s2s-url","http://127.0.0.1:8502")

  val s2sname : String = scala.util.Properties.envOrElse("s2s-name","am_accessmgmt_api")

  val s2sSecret : String = scala.util.Properties.envOrElse("s2s-secret","GJNMFGFAAO4FCVD4")

  val getOTP =
    exec(
      session => {
        val otp: String = String.valueOf(new GoogleAuthenticator().getTotpPassword(s2sSecret))
        session.set("OTP", otp)

      })

  val otpp="${OTP}"

  val S2SAuthToken =

    exec {
      session =>
        println("this is a session for otp ....." + session("otpp"))
        session

    }
      .exec(http("AM lib gatling Token")
        .post(s2sUrl+"/lease")
        .header("Content-Type", "application/json")
        .body(StringBody(
          s"""{
       "microservice": "${s2sname}",
        "oneTimePassword": "${ otpp}"
        }"""
        )).asJson
        .check(bodyString.saveAs("s2sToken"))
        .check(bodyString.saveAs("responseBody")))

}
