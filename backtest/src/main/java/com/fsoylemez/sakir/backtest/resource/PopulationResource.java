package com.fsoylemez.sakir.backtest.resource;

import com.fsoylemez.sakir.backtest.service.CryptoPopulationService;
import com.fsoylemez.sakir.backtest.service.FxStockPopulationService;
import com.fsoylemez.sakir.backtest.util.BackTestConstants;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

@Slf4j
@Path("/populate")
public class PopulationResource {

    @Inject
    CryptoPopulationService cryptoPopulationService;

    @Inject
    FxStockPopulationService fxStockPopulationService;


    @GET
    @Path("/{symbol}/{interval}/{year}/{month}/{day}")
    public Response populateFrom(String symbol, String interval, Integer year, Integer month, Integer day) {
        cryptoPopulationService.populate(symbol, interval, year, month, day);

        return Response.ok().build();
    }

    @GET
    @Path("/{symbol}/{interval}")
    public Response populate(String symbol, String interval) {
        cryptoPopulationService.populate(symbol, interval, null, null, null);

        return Response.ok().build();
    }

    @POST
    @Path("/{interval}")
    public Response populateAll(List<String> symbols, @PathParam("interval") String interval) {
        if(symbols == null || symbols.isEmpty()) {
            return Response.noContent().build();
        }
        for (String symbol : symbols) {
            cryptoPopulationService.populate(symbol, interval, null, null, null);
        }

        return Response.ok().build();
    }

    @GET
    @Path("/fx/{interval}")
    public Response populateFx(@PathParam("interval") String interval) throws IOException, InterruptedException {
        String[] pairs = BackTestConstants.FX_PAIRS;

        for (String pair : pairs) {
            fxStockPopulationService.populate(pair, interval, null, "FX");
        }

        log.info("COMPLETED");

        return Response.ok().build();
    }

    @GET
    @Path("/stock/{interval}")
    public Response populateStock(@PathParam("interval") String interval) throws IOException, InterruptedException {
        String[] pairs = BackTestConstants.STOCK_SYMBOLS;

        for (String pair : pairs) {
            fxStockPopulationService.populate(pair, interval, null, "STOCK");
        }

        log.info("COMPLETED");

        return Response.ok().build();
    }
}
