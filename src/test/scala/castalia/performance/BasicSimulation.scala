package castalia.performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scalaj.http.Http

import scala.concurrent.duration._


class BasicSimulation extends Simulation {

  val noEndpoints = 5

  before {
    // initial configuration of castalia endpoints, publish couple of endpoints

    for (endpoint <- 0 until noEndpoints)
      Http("http://localhost:9090/castalia/manager/endpoints")
        .postData(s"""{"endpoint":"my/endpoint${endpoint}", "responses": [{"httpStatusCode": 200, "response": {"value": "somevalue"}}]}""")
        .header("content-type", "application/json").asString
  }

  val httpConf = http.baseURL("http://localhost:9000").acceptHeader("application/json")

  val endpoints = repeat(noEndpoints, "index") {
    exec(http("my/endpoint${index}").get("/my/endpoint${index}")).pause(10 milli)
  }

  val scn = scenario("some basic").exec(endpoints)

  setUp(scn.inject(rampUsers(500) over (10 seconds))).protocols(httpConf).
    assertions(global.successfulRequests.percent.is(100))

}
