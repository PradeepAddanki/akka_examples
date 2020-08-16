package com.tresata.akka.http.spnego;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;

import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;


import akka.http.javadsl.model.headers.HttpCookie;
import akka.http.javadsl.model.headers.SetCookie;
import akka.http.javadsl.server.*;
import akka.http.javadsl.server.directives.RouteAdapter;
import akka.http.scaladsl.server.RequestContext;
import akka.http.scaladsl.server.RouteResult;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.typesafe.config.ConfigFactory;
import scala.Function1;
import scala.concurrent.Future;

import java.io.IOException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static akka.http.javadsl.server.Directives.complete;
import static akka.http.javadsl.server.Directives.extractMethod;
import static akka.http.javadsl.server.Directives.get;
import static akka.http.javadsl.server.Directives.path;
import static akka.http.javadsl.server.Directives.put;
import static akka.http.javadsl.server.Directives.route;

import static akka.http.javadsl.server.PathMatchers.integerSegment;
import static akka.http.javadsl.server.PathMatchers.segment;
import static akka.http.javadsl.server.Directives.path;


public class HighLevelServerExample extends AllDirectives {
    public static void main(String[] args) throws IOException {
        // boot up server using the route as defined below
        ActorSystem system = ActorSystem.create();

        final HighLevelServerExample app = new HighLevelServerExample();

        final Http http = Http.get(system);
        final ActorMaterializer materializer = ActorMaterializer.create(system);

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.getRequestContext().flow(system, materializer);
        http.bindAndHandle(routeFlow, ConnectHttp.toHost("localhost", 8080), materializer);
    }

    public Route getRequestContext() {
        Route route = path("sample", () -> extractExecutionContext(executor -> onSuccess(CompletableFuture.supplyAsync(() -> "Run on " + executor.hashCode() + "!", executor), str -> complete(str))));
        Route requestContext = path("sample", () -> extractRequestContext(ctx -> {
            Optional<HttpHeader> httpHeaderoption =  ctx.getRequest().getHeader("Set-Cookie");
            if(httpHeaderoption.isPresent()){
                HttpCookie httpCookie=  ((SetCookie) (httpHeaderoption.get())).cookie();
            } else{
                final Function<Iterable<Rejection>, Boolean> existsAuthenticationFailedRejection =
                        rejections ->
                                StreamSupport.stream(rejections.spliterator(), false)
                                        .anyMatch(r -> r instanceof AuthenticationFailedRejection);

                Route route1 =  recoverRejectionsWith(
                        rejections -> CompletableFuture.supplyAsync(() -> {
                            if (existsAuthenticationFailedRejection.apply(rejections)) {
                                return RouteResults.complete(HttpResponse.create().withEntity("Nothing to see here, move along."));
                            } else {
                                return RouteResults.rejected(rejections);
                            }
                        }), ()->route);
            }
            ctx.getLog().debug("Using access to additional context available, like the logger.");
            HttpRequest request = ctx.getRequest();
            //AuthenticationFailedRejection
            return complete("Request method is " + request.method().name() + " and content-type is " + request.entity().getContentType());
        }));
        return requestContext;
    }


    private HttpCookie getCookieValues(HttpRequest response, String cookieName) {

        /*
        cookies we may get:
         Set-Cookie: _sessiondata=1AAFE061C539EFD16A20CBC834608EAE9909A5EF-1485426400816-xmy+session+object; Path=/; HttpOnly
         Set-Cookie: _refreshtoken=t7i7iv0i7b5lbet2:nhnpqrc855anom6sffr70bicl41951dq8d3mms1u6f9pnatc5ouuhl17m74fnda5; Max-Age=2592000; Path=/; HttpOnly
         Set-Cookie: XSRF-TOKEN=1ei80a0p21paueur5smbiokpm9t13cj418fh5pv05537273uesru8utrr3c92s45; Path=/
        */
//        cookie("userName", nameCookie ->
//                complete("The logged in user is '" + nameCookie.value() + "'")
//        )
        List<HttpCookie> cookies = StreamSupport
                .stream(response.getHeaders().spliterator(), false)
                .collect(Collectors.toList())
                .stream()
                .map(header -> ((SetCookie) (header)).cookie())
                                .filter(cookie -> cookie.name().equals(cookieName))
                .collect(Collectors.toList());

        return cookies.size() == 0 ? null : cookies.get(0);
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
        return javaRoute;
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
        // a function for authentication

        // SpnegoDirectives$.MODULE$.spnegoAuthenticate(ConfigFactory.load()).tmap("");
        scala.Function1<RequestContext, Future<RouteResult>> scalaRoute1 = SpnegoAuthenticator.spnegoAuthenticate(ConfigFactory.load()).tapply(null);
        ;
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