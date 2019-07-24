package uk.gov.hmcts.reform.amlib.performance.http

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder

object AccessManagement {

  private def getRequest(url: String): HttpRequestBuilder =
    http(url)
      .get("/api" + url)
      .check(status.is(200))

  private def postRequest(url: String, body: String, statusExpected: Int): HttpRequestBuilder =
    http(url)
      .post("/api" + url)
      .body(ElFileBody(body)).asJson
      .check(status.is(statusExpected))

  private def deleteRequest(url: String, body: String): HttpRequestBuilder =
    http(url)
      .delete("/api" + url)
      .body(ElFileBody(body)).asJson
      .check(status.is(204))

  def createResourceAssess: HttpRequestBuilder =
    postRequest("/access-resource","createResourceAccess.json", 201)

  def filterResource: HttpRequestBuilder =
    postRequest("/filter-resource", "filterResource.json", 200)

  def revokeResourceAccess: HttpRequestBuilder =
    deleteRequest("/access-resource", "revokeResourceAccess.json")

  def returnResourceAccessors: HttpRequestBuilder =
    getRequest("/resource/resourceType/case/resourceName/claim/resourceId/${resourceId}")

}
