package uk.gov.hmcts.reform.amlib.performance.utils

object Environment {

  val baseUrl : String = scala.util.Properties.envOrElse("TEST_URL","http://localhost:3704")

  val s2sUrl : String = scala.util.Properties.envOrElse("S2S_URL_FOR_TESTS","http://127.0.0.1:8502")

  val s2sname : String = scala.util.Properties.envOrElse("S2S_MICROSERVICE","am_accessmgmt_api")

  val s2sSecret : String = scala.util.Properties.envOrElse("S2S_SECRET","AAAAAAAAAAAAAAAB")
}
