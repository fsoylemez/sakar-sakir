package com.fsoylemez.sakar.sakir.configuration;

import com.fsoylemez.sakir.strategy.exception.SakirException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@Singleton
public class PropertiesProducer {

    @Inject
    @ConfigProperty(name = "properties.path")
    String path;

    @Produces
    public Properties getProperties() throws SakirException {
        Properties properties = new Properties();
        try (FileInputStream fs = new FileInputStream(path)){
            properties.load(fs);
        } catch (IOException e) {
            throw  new SakirException("Could not find properties file.");
        }
        return properties;
    }
}
