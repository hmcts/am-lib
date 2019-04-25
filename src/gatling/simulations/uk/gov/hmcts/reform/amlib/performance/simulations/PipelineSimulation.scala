package uk.gov.hmcts.reform.amlib.performance.simulations

import uk.gov.hmcts.reform.amlib.performance.scenarios.BasicScenarios
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.reform.amlib.performance.utils.Environment

import scala.concurrent.duration._

class PipelineSimulation extends Simulation {

  private val httpProtocol = http.baseUrl(Environment.baseUrl)
  private val loadProfile = rampUsers(10) during 2.seconds

  /* load profile and assertions to be changed once NFRs are defined
      this is just an exemplary simulation */

  setUp(
    BasicScenarios.createResourceAccess.inject(loadProfile).protocols(httpProtocol),
    BasicScenarios.filterResource.inject(loadProfile).protocols(httpProtocol),
    BasicScenarios.revokeResourceAccess.inject(loadProfile).protocols(httpProtocol)
  ).maxDuration(30.seconds)
    .assertions(
      global.failedRequests.count.is(0),
      global.responseTime.max.lt(30000)
    )
}
