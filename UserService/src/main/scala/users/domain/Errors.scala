package me.alstepan.users.domain

object Errors {

  abstract sealed class Errors(val message:String) {
    def code: Int
    def toJson: String =
      s"""{
         |  "code" : $code,
         |  "message": "$message"
         |}
         |""".stripMargin
  }

  object NotFoundError extends Errors("Not Found") {
    val code: Int = 1
  }

  object UniqueConstraintError extends Errors("Duplicate data") {
    val code: Int = 2
  }

  case class CustomError(state: String) extends Errors("SQL error: " + state) {
    val code: Int = 3
  }

}
