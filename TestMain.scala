package com.tresata.akka.http.spnego

import akka.http.scaladsl.server.Directives._
import com.tresata.akka.http.spnego.SpnegoDirectives._
class TestMain {
  val route =
    spnegoAuthenticate(){ token =>
      get{
        path("ping") {
          complete(s"pong for user ${token.principal}")
        }
      }
    }


  val scalaRoute: akka.http.scaladsl.server.Route = route

}
