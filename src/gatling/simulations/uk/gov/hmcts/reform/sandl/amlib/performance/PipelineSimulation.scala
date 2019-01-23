package uk.gov.hmcts.reform.sandl.snlrules.performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class PipelineSimulation extends Simulation {
  val httpConf = http
    .baseURL(Environments.baseUrl)

  val scn = scenario("Pipeline Quick Checks")
    .forever(
        exec(http("/"))
          .check(status.is(200))
    )


    setUp(
      scn.inject(
        rampUsers(10) over(2 seconds),
      )
    )
    .maxDuration(60 seconds)
    .protocols(httpConf)
    .assertions(
      global.failedRequests.count.is(0),
      global.responseTime.max.lt(5000),
      global.responseTime.percentile3.lt(2000)
    )

}
