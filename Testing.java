package com.tresata.akka.http.spnego;
import com.typesafe.config.ConfigFactory;

import static akka.http.javadsl.server.Directives.*;
public class Testing implements SpnegoDirectives {
    public static void main(String[] args) {
        Testing t = new Testing();
        t.spnegoAuthenticate(ConfigFactory.load());
    }


}
