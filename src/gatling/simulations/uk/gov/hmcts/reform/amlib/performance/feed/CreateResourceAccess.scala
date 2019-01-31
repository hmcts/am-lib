package gov.hmcts.reform.amlib.performance.feed

import java.util.UUID

import io.gatling.core.feeder.Feeder

object CreateResourceAccess {

  private def randomUUID: String = UUID.randomUUID().toString

  def feed: Feeder[String] = Iterator.continually {
    Map(
      "resourceId" -> randomUUID,
      "accessorId" -> randomUUID
    )
  }
}
