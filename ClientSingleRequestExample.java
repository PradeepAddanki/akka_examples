import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.japi.Pair;
import akka.stream.IOResult;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Framing;
import akka.stream.javadsl.FramingTruncation;
import akka.stream.javadsl.Sink;
import akka.util.ByteString;

import java.io.File;
import java.util.Stack;
import java.util.concurrent.CompletionStage;

public class ClientSingleRequestExample {
    static final ActorSystem system = ActorSystem.create("SingleRequest");

    public static void main(String[] args) throws Exception {
        ClientSingleRequestExample test = new ClientSingleRequestExample();
        test.download("https://repo.maven.apache.org/maven2/capital/scalable/spring-auto-restdocs-core/", "/home/pradeep/testng/", true);
        System.out.println("DONE=====================================================");

    }

    public void download(String url, String destinationPath, boolean isRecursive) throws Exception {
        String fileExtension = url.replaceFirst(".*/([^/?]+).*", "$1");
        File file = new File(destinationPath);
        Stack<Pair<String, String>> stack = new Stack<>();
        if (!file.exists()) {
            file.mkdir();
        }
        final CompletionStage<HttpResponse> responseFuture =
                Http.get(system)
                        .singleRequest(HttpRequest.GET(url));
        HttpResponse response = responseFuture.toCompletableFuture().get();
        //System.out.println(response.entity().getContentType().mediaType().subType());
        if (response.entity().getContentType().mediaType().subType().equals("html") && isRecursive) {

            //response.
            CompletionStage recursiveHtmlStage = response.entity().getDataBytes()
                    .via(Framing.delimiter(ByteString.fromString("\n"), Integer.MAX_VALUE, FramingTruncation.ALLOW))
                    .map(ByteString::utf8String)
                    .filter(e -> e.contains("<a href="))
                    .filter(e -> !(e.contains("../")))
                    .runWith(Sink.foreach(e -> {
                                String path = e.split("\"")[1];
                                stack.push(new Pair<>(url + path, destinationPath + fileExtension + "/"));
                            }
                    ), system);
            recursiveHtmlStage.toCompletableFuture().get();
        } else {
            CompletionStage<IOResult> completionStage = response.entity().getDataBytes()
                    .runWith(FileIO.toPath(new File(destinationPath + fileExtension).toPath()), system);
            completionStage.toCompletableFuture().get().productPrefix();
        }

        if (stack.size() > 0 && isRecursive) {
            downloadRecursively(stack);
        }
    }

    private void downloadRecursively(Stack<Pair<String, String>> stack) throws Exception {
        while (stack.size() != 0) {
            Pair<String, String> pairValues = stack.pop();
            download(pairValues.first(), pairValues.second(), true);
        }

    }
}
