package uk.gov.hmcts.reform.amlib.performance.utils

object Environment {
  val baseUrl : String = scala.util.Properties.envOrElse("TEST_URL","http://localhost:3704")
}