package uk.gov.hmcts.reform.amlib.performance.http

import java.util.UUID

import com.warrenstrange.googleauth.GoogleAuthenticator

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

object AccessManagement {

  val s2sUrl : String = scala.util.Properties.envOrElse("s2s-url","http://127.0.0.1:8502")

  val s2sname : String = scala.util.Properties.envOrElse("s2s-name","am_accessmgmt_api")

  val s2sSecret : String = scala.util.Properties.envOrElse("s2s-secret","GJNMFGFAAO4FCVD4")

  val authenticator = new  GoogleAuthenticator


  private val responseS2S = http("${s2sUrl}")
    .post("/lease")
    .formParamMap(Map("microservice" -> s2sname, "oneTimePassword" -> authenticator.getTotpPassword(s2sSecret)))
    //.check(bodyString.saveAs("auth_Response"))

  private def getRequest(url: String): HttpRequestBuilder =
    http("/returnResourceAccessors")
      .get("/api" + url)
      .header("ServiceAuthorization", responseS2S.toString())
      .check(status.is(200))

  private def postRequest(url: String, body: String, statusExpected: Int): HttpRequestBuilder =
    http(url)
      .post("/api" + url)
      .header("ServiceAuthorization",responseS2S.toString())
      .body(ElFileBody(body)).asJson
      .check(status.is(statusExpected))

  private def deleteRequest(url: String, body: String): HttpRequestBuilder =
    http(url)
      .delete("/api" + url)
      .header("ServiceAuthorization",responseS2S.toString())
      .body(ElFileBody(body)).asJson
      .check(status.is(204))



  def createResourceAssess: HttpRequestBuilder =
    postRequest("/access-resource","createResourceAccess.json", 201)

  def filterResource: HttpRequestBuilder =
    postRequest("/filter-resource", "filterResource.json", 200)

  def revokeResourceAccess: HttpRequestBuilder =
    deleteRequest("/access-resource", "revokeResourceAccess.json")

  def returnResourceAccessors: HttpRequestBuilder =
    getRequest("/resource/resourceType/case-test/resourceName/claim-test/resourceId/${resourceId}")

}
