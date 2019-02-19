package gov.hmcts.reform.amlib.performance.http

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

object AccessManagement {

  private def postRequest(url: String, body: String): HttpRequestBuilder =
    http(url)
      .post("/lib" + url)
      .body(ElFileBody(body)).asJSON
      .check(status.is(200))

  def helloWorld: HttpRequestBuilder =
    http("GET Hello World")
      .get("/")
      .check(status.is(200))

  def createResourceAssess: HttpRequestBuilder =
    postRequest("/create-resource-access","createResourceAccess.json")

  def filterResource: HttpRequestBuilder =
    postRequest("/filter-resource", "filterResource.json")

  def getAccessorsList: HttpRequestBuilder =
    postRequest("/get-accessors-list", "getAccessorsList.json")

  def revokeResourceAccess: HttpRequestBuilder =
    postRequest("/revoke-resource-access", "revokeResourceAccess.json")
}
