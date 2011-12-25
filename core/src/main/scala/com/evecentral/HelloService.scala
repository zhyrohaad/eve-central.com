package com.evecentral

import cc.spray.http.MediaTypes._
import java.util.concurrent.TimeUnit
import akka.actor.{PoisonPill, Actor, Scheduler}
import cc.spray.Directives

trait HelloService extends Directives {

  def getOrders = {
    val r = Actor.registry.actorsFor[com.evecentral.Markets]
    r(0)
  }

  val helloService = {
    path("orders") {
      get {
        respondWithMediaType(`text/plain`) {
          completeWith {
            (getOrders ? GetOrdersFor(Nil, Nil)).as[Seq[MarketOrder]] match {
              case Some(x) => x(0).orderId.toString
              case _ => "Nothing!"
            }
          }
        }
      }
    } ~
    path("") {
      get {
        respondWithMediaType(`text/html`) {
          completeWith {
            <html>
              <p>Say hello to <i>spray</i> on <b>spray-can</b>!</p>
              <p><a href="/shutdown?method=post">Shutdown</a> this server</p>
            </html>
          }
        }
      }
    } ~
      path("shutdown") {
        (post | parameter('method ! "post")) { ctx =>
          Scheduler.scheduleOnce(() => Actor.registry.foreach(_ ! PoisonPill), 1000, TimeUnit.MILLISECONDS)
          ctx.complete("Will shutdown server in 1 second...")
        }
      }
  }

}