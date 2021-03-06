package uk.gov.hmcts.reform.amlib.performance.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import uk.gov.hmcts.reform.amlib.performance.scenarios.BasicScenarios
import uk.gov.hmcts.reform.amlib.performance.utils.Environment

import scala.concurrent.duration._

class PipelineSimulation extends Simulation {

  private val httpProtocol = http.baseUrl(Environment.baseUrl)

  private val loadProfile = rampUsers(1) during 1.seconds

  /* load profile and assertions to be changed once NFRs are defined
      this is just an exemplary simulation*/
  //this is used in AAT environment
 setUp(
    BasicScenarios.createResourceAccess.inject(loadProfile).protocols(httpProtocol),
    BasicScenarios.filterResource.inject(loadProfile).protocols(httpProtocol),
    BasicScenarios.revokeResourceAccess.inject(loadProfile).protocols(httpProtocol),
    BasicScenarios.returnResourceAccessors.inject(loadProfile).protocols(httpProtocol)
  ).maxDuration(600.seconds)
    .assertions(
      global.failedRequests.count.is(0),
      global.responseTime.max.lt(30000)
    )

 /* setUp(

 //   BasicScenarios.createResourceAccess.inject(atOnceUsers(1)).protocols(httpProtocol)
//    BasicScenarios.createResourceAccess.inject(
//        constantUsersPerSec(100) during (10 minutes)).protocols(httpProtocol),
//    BasicScenarios.filterResource.inject(
//        constantUsersPerSec(100) during (10 minutes)).protocols(httpProtocol),
//    BasicScenarios.revokeResourceAccess.inject(
//        constantUsersPerSec(100) during (10 minutes)).protocols(httpProtocol),
//    BasicScenarios.returnResourceAccessors.inject(
//        constantUsersPerSec(100) during (10 minutes)).protocols(httpProtocol)
  )
  .throttle(
    reachRps(100) in (10 seconds),
    holdFor(2 minute),
    jumpToRps(50),
    holdFor(10 minutes)
  )
  .assertions(
      global.failedRequests.count.is(0),
      global.responseTime.max.lt(30000)
    ) */
}
