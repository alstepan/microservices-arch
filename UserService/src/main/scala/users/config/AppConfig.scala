package me.alstepan.users.config

case class AppConfig(host: String = "0.0.0.0", port: Int = 8000, dbConfig: DatabaseConfig)
