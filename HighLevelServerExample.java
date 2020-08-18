package com.tresata.akka.http.spnego;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;

import akka.http.javadsl.model.*;


import akka.http.javadsl.model.headers.*;
import akka.http.javadsl.server.*;
import akka.http.javadsl.server.directives.RouteAdapter;
import akka.http.scaladsl.server.RequestContext;
import akka.http.scaladsl.server.RouteResult;
import akka.japi.Option;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.typesafe.config.ConfigFactory;
import scala.Function1;
import scala.None;
import scala.concurrent.Future;
import scala.util.Either;
import scala.util.Left;
import scala.util.Right;

import java.io.IOException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
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

    public Route getRequestContext1() {
        //Route route = path("sample", () -> extractExecutionContext(executor -> onSuccess(CompletableFuture.supplyAsync(() -> "Run on " + executor.hashCode() + "!", executor), str -> complete(str))));
       // RejectionHandler.defaultHandler().mapRejectionResponse(re)
       // HttpChallenge challenge = WWWAuthenticate.create(HttpChallenge.create("MyAuth", new Option.Some<>("MyRealm")));
        Route requestContext = path("sample", () -> extractRequestContext(ctx -> {
            RejectionHandler totallyMissingHandler = RejectionHandler.newBuilder()
                    .handleNotFound(
                            extractUnmatchedPath(path ->
                                    complete(StatusCodes.UNAUTHORIZED, "The path " + path + " was not found!")
                            )
                    )
                    .build();
            Optional<HttpHeader> httpHeaderoption = ctx.getRequest().getHeader("Set-Cookie");
            if (httpHeaderoption.isPresent()) {
                HttpCookie httpCookie = ((SetCookie) (httpHeaderoption.get())).cookie();
                //Validate It
            } else {
                HttpHeader httpHeader = RawHeader.create("WWW-Authenticate", "Test");
                //WWWAuthenticate.create(HttpChallenge.create(httpHeader.name(), "Test"));


                ctx.getLog().debug("Using access to additional context available, like the logger.");
                HttpRequest request = ctx.getRequest();
                //AuthenticationFailedRejection
                //return complete("Request method is " + request.method().name() + " and content-type is " + request.entity().getContentType());
            }
            return handleRejections(totallyMissingHandler, () -> path("hello", () -> complete("Hello there")));

        }));
        return requestContext;
    }

    public Route getRequestContext(){
        final HttpChallenge challenge = HttpChallenge.create("MyAuth", new Option.Some<>("MyRealm"));
//
//        // your custom authentication logic:
//        final Function<HttpCredentials, Boolean> auth = credentials -> true;
//
//        final Function<Optional<HttpCredentials>, CompletionStage<Either<HttpChallenge, String>>> myUserPassAuthenticator =
//                opt -> {
//                    if (opt.isPresent() && auth.apply(opt.get())) {
//                        return CompletableFuture.completedFuture(Right.apply("some-user-name-from-creds"));
//                    } else {
//                        return CompletableFuture.completedFuture(Left.apply(challenge));
//                    }
//                };
//
//        final Route route = path("secured", () ->
//                authenticateOrRejectWithChallenge(myUserPassAuthenticator, userName -> complete("Authenticated!"))
//        ).seal();
//        final Function<Optional<ProvidedCredentials>, Optional<String>> myUserPassAuthenticator =
//                credentials ->
//                        credentials.filter(c -> c.verify("p4ssw0rd")).map(ProvidedCredentials::identifier);
//
//        final Route route = path("secured", () -> authenticateBasic("secure site", myUserPassAuthenticator, userName -> complete("The user is '" + userName + "'"))
//        ).seal();

        final RejectionHandler rejectionHandler = RejectionHandler.defaultHandler()
                .mapRejectionResponse(response -> {
                    if (response.entity() instanceof HttpEntity.Strict) {
                        // since all Akka default rejection responses are Strict this will handle all rejections
                        response.getHeaders().forEach(System.out::println);
                        String message = ((HttpEntity.Strict) response.entity()).getData().utf8String()
                                .replaceAll("\"", "\\\"");
                        // we create a new copy the response in order to keep all headers and status code,
                        // replacing the original entity with a custom message as hand rolled JSON you could the
                        // entity using your favourite marshalling library (e.g. spray json or anything else)
                        return response.withEntity(ContentTypes.APPLICATION_JSON,
                                "{\"rejection\": \"" + message + "\"}");
                    } else {
                        // pass through all other types of responses
                        return response;
                    }
                });

        RejectionHandler rejectionHandlerBuilder = RejectionHandler.newBuilder()
                .handle(MissingCookieRejection.class, rej ->
                        complete(StatusCodes.BAD_REQUEST, "No cookies, no service!!!")
                )
                .handle(AuthorizationFailedRejection.class, rej ->
                        complete(StatusCodes.FORBIDDEN, "You're out of your depth!")
                )
                .handle(ValidationRejection.class, rej ->
                        complete(StatusCodes.INTERNAL_SERVER_ERROR, "That wasn't valid! " + rej.message())
                )
                .handleAll(MethodRejection.class, rejections -> {
                    String supported = rejections.stream()
                            .map(rej -> rej.supported().name())
                            .collect(Collectors.joining(" or "));
                    return complete(StatusCodes.METHOD_NOT_ALLOWED, "Can't do that! Supported: " + supported + "!");
                })
                .handleNotFound(complete(StatusCodes.NOT_FOUND, "Not here!"))
                .build();


        Route route = handleRejections(rejectionHandler, () ->
                path("hello", () ->
                        extractRequestContext(ctx -> {
                            ctx.reject(Rejections.authenticationCredentialsMissing(HttpChallenge.create("WWW-Authentication", "Test")));
                            return complete("======="+ctx.getRequest().getHeaders());})
                ));


        return  route;
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