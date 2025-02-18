package com.fsoylemez.sakir.backtest.producer;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.fsoylemez.sakir.strategy.exception.SakirException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.MalformedURLException;
import java.net.URL;

@Singleton
public class CouchDbClientProducer {

    @Inject
    Logger log;

    @ConfigProperty(name = "couchdb.url")
    String couchDbUrl;

    @ConfigProperty(name = "couchdb.user")
    String user;

    @ConfigProperty(name = "couchdb.password")
    String password;

    @Produces
    CloudantClient getCloudantClient() throws SakirException {
        try {
            return ClientBuilder.url(new URL(couchDbUrl))
                    .username(user)
                    .password(password)
                    .build();
        } catch (MalformedURLException e) {
            log.error(e.getMessage());
        }

        throw new SakirException("Could not access to database.");
    }

}
