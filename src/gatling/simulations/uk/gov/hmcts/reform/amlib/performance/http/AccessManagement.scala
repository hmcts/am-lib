package uk.gov.hmcts.reform.amlib.performance.http

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

object AccessManagement {

  def version = "v1"

  private def getRequest(url: String): HttpRequestBuilder =
    http("/returnResourceAccessors")
      .get("/api/" + version + "/" + url)
      .header("ServiceAuthorization", "Bearer ${s2sToken}")
      .check(status.is(200))

  private def postRequest(url: String, body: String, statusExpected: Int): HttpRequestBuilder =
    http(url)
      .post("/api/" + version + "/" + url)
      .header("ServiceAuthorization", "Bearer ${s2sToken}")
      .body(ElFileBody(body)).asJson
      .header("Content-Type", "application/json")
      .check(status.is(statusExpected))

  private def deleteRequest(url: String, body: String): HttpRequestBuilder =
    http(url)
      .delete("/api/" + version + "/" + url)
      .header("ServiceAuthorization", "Bearer ${s2sToken}")
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
