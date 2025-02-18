package com.fsoylemez.sakir.backtest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsoylemez.sakir.backtest.enums.TiingoInterval;
import com.fsoylemez.sakir.backtest.enums.TiingoMarket;
import com.fsoylemez.sakir.backtest.mapper.OhlcMapper;
import com.fsoylemez.sakir.backtest.model.tiingo.OhlcData;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Properties;

@ApplicationScoped
public class TiingoDataFetchService {

    @ConfigProperty(name = "data.api.tiingo.base.url")
    String baseApiUrl;

    @ConfigProperty(name = "data.api.tiingo.crypto.price.path")
    String cryptoPricePath;

    @ConfigProperty(name = "data.api.tiingo.fx.price.path")
    String fxPricePath;

    @ConfigProperty(name = "data.api.tiingo.stock.price.path")
    String stockPricePath;

    @Inject
    Properties properties;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    OhlcMapper ohlcMapper;


    public List<OhlcData> fetchData(String ticker, String startDate, TiingoInterval interval, TiingoMarket market) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        URI uri;
        if (TiingoMarket.STOCK.equals(market)) {
            uri = UriBuilder.fromUri(baseApiUrl + stockPricePath)
                    .path(ticker + "/prices")
                    .queryParam("token", properties.getProperty("tiingo.token"))
                    .queryParam("startDate", startDate)
                    .queryParam("resampleFreq", interval.getValue())
                    .queryParam("columns", "open,high,low,close,volume")
                    .build();
        } else if (TiingoMarket.FX.equals(market)) {
            uri = UriBuilder.fromUri(baseApiUrl + fxPricePath)
                    .path(ticker + "/prices")
                    .queryParam("token", properties.getProperty("tiingo.token"))
                    .queryParam("startDate", startDate)
                    .queryParam("resampleFreq", interval.getValue())
                    .build();
        } else {
            uri = UriBuilder.fromUri(baseApiUrl + cryptoPricePath)
                    .queryParam("token", properties.getProperty("tiingo.token"))
                    .queryParam("tickers", ticker)
                    .queryParam("startDate", startDate)
                    .queryParam("resampleFreq", interval.getValue())
                    .build();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        List<OhlcData> responses = objectMapper.readValue(response.body(), new TypeReference<>() {});

        //return ohlcMapper.toBar(responses.get(0).getPriceData(), interval);
        return responses;
    }
}
