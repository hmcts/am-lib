package gov.hmcts.reform.amlib.performance.scenarios

import gov.hmcts.reform.amlib.performance.feed.CreateResourceAccess
import gov.hmcts.reform.amlib.performance.http.AccessManagement
import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder

import scala.concurrent.duration._

object BasicScenarios {

  val createResourceAccess: ScenarioBuilder = scenario("Create Resource Access")
    .forever(
      feed(CreateResourceAccess.feed)
        .exec(AccessManagement.createResourceAssess)
        .pause(1.second)
    )

  val filterResource: ScenarioBuilder = scenario("Filter Resource")
    .forever(
      feed(CreateResourceAccess.feed)
        .exec(AccessManagement.createResourceAssess)
        .exec(AccessManagement.filterResource)
        .pause(1.second)
    )

  val getAccessorsList: ScenarioBuilder = scenario("Get Accessors List")
    .forever(
      feed(CreateResourceAccess.feed)
        .exec(AccessManagement.createResourceAssess)
        .exec(AccessManagement.getAccessorsList)
        .pause(1.second)
    )

  val revokeResourceAccess: ScenarioBuilder = scenario("Revoke Resource Access")
    .forever(
      feed(CreateResourceAccess.feed)
        .exec(AccessManagement.createResourceAssess)
        .exec(AccessManagement.revokeResourceAccess)
        .pause(1.second)
    )
}
