package uk.gov.hmcts.reform.amlib.performance.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import uk.gov.hmcts.reform.amlib.performance.utils.Environment

class PipelineSimulation extends Simulation {

  val httpProtocol = http.baseURL(Environment.baseUrl)

  val scn = scenario("Hello World")
    .forever(
      exec(http("GET Hello World")
        .get("/")
        .check(status.is(200)))
    )

  setUp(
    scn.inject(atOnceUsers(10))
      .protocols(httpProtocol)
  ).maxDuration(20 seconds)
    .assertions(
      global.failedRequests.count.is(0)
    )
}