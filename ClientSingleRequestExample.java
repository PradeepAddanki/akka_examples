import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.FramingTruncation;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;

import java.io.File;
import java.net.URL;
import java.util.concurrent.CompletionStage;

public class ClientSingleRequestExample {
    static final ActorSystem system = ActorSystem.create("SingleRequest");

    public static void main(String[] args) throws Exception {
        ClientSingleRequestExample test = new ClientSingleRequestExample();
        test.download("https://repo.maven.apache.org/maven2/capital/scalable/spring-auto-restdocs-core/", "/home/pradeep/testng/");


        System.out.println("DONE=====================================================");

    }


    public synchronized void download(String url, String destinationPath) throws Exception {
       // System.out.println("====>" + url);
        String fileExtension = url.replaceFirst(".*/([^/?]+).*", "$1");
        File file = new File(destinationPath);
//        System.out.println("====>"+file.getAbsolutePath());
//        System.out.println("====>"+fileextention);
//        if (!file.exists()) {
//            file.mkdir();
//        }
        final CompletionStage<HttpResponse> responseFuture =
                Http.get(system)
                        .singleRequest(HttpRequest.GET(url));
        // final Function<ByteString, String> transformEachLine = line -> ByteString.f;
        HttpResponse response = responseFuture.toCompletableFuture().get();
        //System.out.println(response.entity().getContentType().mediaType().subType());
        if (response.entity().getContentType().mediaType().subType().equals("html")) {
            //response.
            response.entity().getDataBytes()
                    .via(Framing.delimiter(ByteString.fromString("\n"), Integer.MAX_VALUE, FramingTruncation.ALLOW))
                    .map(ByteString::utf8String)
                    .filter(e -> e.contains("<a href="))
//                   .filter(e -> e.length > 1)
                    .filter(e -> !(e.contains("../")))
                    .runWith(Sink.foreach(e -> {
                                System.out.println(e+"=====>"+url);
                                String path = e.split("\"")[1];
                                //     System.out.println(path);
                                //Thread.sleep(9000l);
                                download(url + path, destinationPath + fileExtension + "/");
                            }
                    ), system);
        } else {
 //           System.out.println("resource URL " + url);
//            response.entity().getDataBytes()
//                   .runWith(FileIO.toPath(new File(destinationPath + fileExtension).toPath()), system);
        }
//        } else {
//            response.entity().getDataBytes()
//                    .runWith(FileIO.toPath(new File(destinationPath + fileExtension).toPath()), system);
//        }
     //   System.out.println("====>" + url);
    }
}
//#single-request-example

class OtherRequestResponseExamples {
    public void request() {
        //#create-simple-request
        HttpRequest.create("https://akka.io");

        // with query params
        HttpRequest.create("https://akka.io?foo=bar");
        //#create-simple-request
        //#create-post-request
        HttpRequest.POST("https://userservice.example/users")
                .withEntity(HttpEntities.create(ContentTypes.TEXT_PLAIN_UTF8, "data"));
        //#create-post-request

        // TODO should we have an API to create an Entity via a Marshaller?
    }

    public void response() {

    }
}
