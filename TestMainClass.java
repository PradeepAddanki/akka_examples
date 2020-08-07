package com.tresata.akka.http.spnego;


import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.server.Route;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.RouteAdapter;
import akka.http.scaladsl.server.Directive;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.Tuple1;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import static akka.http.javadsl.server.Directives.*;
public class TestMainClass {


    //private static Object testing;

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create();
        ActorMaterializer materializer = ActorMaterializer.create(system);
        //Main main= new Main();
        SpnegoAuthenticator.spnegoAuthenticate(ConfigFactory.load());
        ;
       // RouteAdapter.asJava(scalaRoute);

        //testing.spnegoAuthenticate(ConfigFactory.load());

        Http http = Http.get(system);
        TestMain main= new TestMain();
        // Main main= new Main();
        Route route = new RouteAdapter(main.route());
        scala.Function1<
                akka.http.scaladsl.server.RequestContext,
                scala.concurrent.Future<akka.http.scaladsl.server.RouteResult>> scalaRoute = main.route();
        akka.http.javadsl.server.Route helloRoute = RouteAdapter.asJava(scalaRoute);


        http.bindAndHandle(route.flow(system, materializer), ConnectHttp.toHost("localhost",4567), materializer);
    }
//    public static Route createRoute() {
//
//        return
//                get(() -> concat(
//                        pathSingleSlash(() ->
//                                complete(HttpEntities.create(ContentTypes.TEXT_HTML_UTF8, "<html><body>Hello world!</body></html>"))
//                        ),
//                        path("ping", () ->
//                                complete("PONG!")
//                        ),
//                        path("hello", () ->
//                                helloRoute
//                        )
//                ));
//    }

}
