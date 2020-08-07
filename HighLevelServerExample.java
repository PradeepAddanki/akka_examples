package com.tresata.akka.http.spnego;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;

import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;


import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Directives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.RouteAdapter;
import akka.http.scaladsl.server.RequestContext;
import akka.http.scaladsl.server.RouteResult;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.typesafe.config.ConfigFactory;
import scala.Function1;
import scala.concurrent.Future;

import java.io.IOException;

import java.util.function.Supplier;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.extractMethod;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.put;
import static akka.http.javadsl.server.Directives.route;

import static akka.http.javadsl.server.PathMatchers.integerSegment;
import static akka.http.javadsl.server.PathMatchers.segment;
import static akka.http.javadsl.server.Directives.path;



public class HighLevelServerExample extends AllDirectives
{
    public static void main(String[] args) throws IOException {
        // boot up server using the route as defined below
        ActorSystem system = ActorSystem.create();

        final HighLevelServerExample app = new HighLevelServerExample();

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.getOrPut().flow(system, materializer);
        http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", 8080), materializer);
    }

//    public Route createRoute() {
//        // This handler generates responses to `/hello?name=XXX` requests
//        Route helloRoute =
//                parameterOptional("name", optName -> {
//                    String name = optName.orElse("Mister X");
//                    return complete("Hello " + name + "!");
//                });
//        Route helloRouteone = spnegoAuthenticate();
//
//
//        return
//                // here the complete behavior for this server is defined
//
//                // only handle GET requests
//                get(() -> concat(
//                        // matches the empty path
//                        pathSingleSlash(() ->
//                                // return a constant string with a certain content type
//                                complete(HttpEntities.create(ContentTypes.TEXT_HTML_UTF8, "<html><body>Hello world!</body></html>"))
//                        ),
//                        path("ping", () ->parameter("test",option->
//                                // return a simple `text/plain` response
//                                complete("PONG!"+option.toString()))
//                        ),
//                        path("hello", () -> helloRoute)
//                ));
//    }
//
//    private Route spnegoAuthenticate() {
//        return path(segment("order").slash(integerSegment()), id ->
//                get(() -> SpnegoAuthenticator.spnegoAuthenticate(ConfigFactory.load()))
//                        .orElse(
//                                put(() -> complete("Received PUT request for order " + id)))
//        );
//        }
//        //SpnegoAuthenticator.spnegoAuthenticate(ConfigFactory.load()) -> {

    public Route createRoute() {
        TestMain main = new TestMain();
        scala.Function1<akka.http.scaladsl.server.RequestContext, scala.concurrent.Future<akka.http.scaladsl.server.RouteResult>> scalaRoute = main.route();
        akka.http.javadsl.server.Route javaRoute = RouteAdapter.asJava(scalaRoute);
        return  javaRoute;
    }

//    Route getOrPut(Supplier<Route> inner) {
//        Route javaRoute = SpnegoDirectives;
//        scala.Function1<
//                akka.http.scaladsl.server.RequestContext,
//                scala.concurrent.Future<akka.http.scaladsl.server.RouteResult>> scalaRoute = someRoute();
//
//        scala.Function1<RequestContext, Future<RouteResult>> scalaRoute = javaRoute.asScala();
//      //  Route route = new RouteAdapter();
//        return get(inner)
//                .orElse(route);
//    }


    Route getOrPut() {


        scala.Function1<RequestContext, Future<RouteResult>> scalaRoute1 = SpnegoAuthenticator.spnegoAuthenticate(ConfigFactory.load()).tapply(null);;
        Route route = new RouteAdapter(scalaRoute1);
        return route;
    }

//    Route customDirective() {
//        return path(segment("order").slash(integerSegment()), id ->
//                getOrPut(() ->
//                        extractMethod(method -> complete("Received " + method + " for order " + id)))
//        );
//    }


}