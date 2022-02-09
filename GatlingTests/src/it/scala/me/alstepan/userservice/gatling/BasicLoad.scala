package me.alstepan.userservice.gatling

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.{DurationDouble, DurationInt}
import scala.util.Random

class BasicLoad extends Simulation {

  val testDuration = 20.minutes

  def randomString = Random.alphanumeric.take(Random.nextInt(15)).mkString

  val httpProtocol = http
    .baseUrl("http://arch.homework/otusapp/") // Here is the root for all relative URLs
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  // Define an infinite feeder which calculates random numbers
  val userRefs = Iterator.continually(
    // Random number will be accessible in session under variable "OrderRef"
    Map(
      "userName" -> randomString,
      "firstName" -> randomString,
      "lastName" -> randomString,
      "email" -> (randomString + "@" + randomString + ".com"),
      "phone" -> Random.nextInt(Integer.MAX_VALUE).toString,
    )
  )

  val defaultUserBody = """{
        "userName":   "#{userName}",
        "firstName": "#{firstName}",
        "lastName":  "#{lastName}",
        "email":     "#{email}",
        "phone":     "#{phone}"
      }""".stripMargin

  val errorBody = """{
        "userName:   "#{userName}",
        "firstName": "#{firstName}",
        "lastName":  "#{lastName}",
        "email":     "#{email}",
        "phone":     "#{phone}"
      }""".stripMargin
  
  val createUserWithError = scenario("Create user with error")
    .feed(userRefs)
    .exec(http("Create user with Error")
       .post("/user")
       .body(StringBody(errorBody))
    )

  val createUser = scenario("Create user")
    .feed(userRefs)
    .exec(http("Create user")
      .post("/user")
      .body(StringBody(defaultUserBody))
    )
    .pause(0.01.seconds)

  val updateUser = scenario("Update user")
    .feed(userRefs)
    .exec(
        http("Update user")
          .put(session => s"/user/${Random.nextLong(session.userId)}")
          .body(StringBody(defaultUserBody))
    )
    .pause(0.05.seconds)

  val readUser = scenario("Get user")
    .exec(
      http("Read user")
        .get(session => s"/user/${Random.nextLong(session.userId)}")
    )

  val deleteUser = scenario("Delete user")
    .exec(
      http("Delete user")
        .delete(session => s"/user/${Random.nextLong(session.userId)}")
    )

  setUp(
    createUser.inject(
      constantUsersPerSec(20).during(testDuration).randomized
    )
      .uniformPauses(0.5),

    createUserWithError.inject(
      constantUsersPerSec(5).during(testDuration).randomized
    ).uniformPauses(0.5),

    readUser.inject(constantUsersPerSec(20).during(testDuration).randomized)
      .uniformPauses(0.5),

    updateUser.pause(3.seconds).inject(constantUsersPerSec(20).during(testDuration).randomized),

    deleteUser.pause(4.seconds).inject(constantUsersPerSec(20).during(testDuration).randomized)

  ).protocols(httpProtocol)
}
