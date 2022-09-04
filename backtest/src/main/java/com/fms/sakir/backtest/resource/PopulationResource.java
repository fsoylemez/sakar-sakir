package com.fms.sakir.backtest.resource;

import com.fms.sakir.backtest.db.couchdb.CouchDbService;
import com.fms.sakir.backtest.model.StrategyPerformance;
import com.fms.sakir.backtest.service.CryptoPopulationService;
import com.fms.sakir.backtest.service.FxStockPopulationService;
import com.fms.sakir.backtest.util.BackTestConstants;
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
@Path("db")
public class PopulationResource {

    @Inject
    CouchDbService dbService;

    @Inject
    CryptoPopulationService cryptoPopulationService;

    @Inject
    FxStockPopulationService fxStockPopulationService;

    @GET
    @Path("dropDatabases")
    public Response dropDbs() {
        dbService.dropDatabases();

        return Response.ok().build();
    }

    @GET
    @Path("/populate/{symbol}/{interval}/{year}/{month}/{day}")
    public Response populateFrom(String symbol, String interval, Integer year, Integer month, Integer day) {
        cryptoPopulationService.populate(symbol, interval, year, month, day);

        return Response.ok().build();
    }

    @GET
    @Path("/populate/{symbol}/{interval}")
    public Response populate(String symbol, String interval) {
        cryptoPopulationService.populate(symbol, interval, null, null, null);

        return Response.ok().build();
    }

    @POST
    @Path("/populate/{interval}")
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
    @Path("/fx/populate/{interval}")
    public Response populateFx(@PathParam("interval") String interval) throws IOException, InterruptedException {
        String[] pairs = BackTestConstants.FX_PAIRS;

        for (String pair : pairs) {
            fxStockPopulationService.populate(pair, interval, null, "FX");
        }

        log.info("COMPLETED");

        return Response.ok().build();
    }

    @GET
    @Path("/cleanup")
    public Response cleanup() {
        String[] fxPairs = BackTestConstants.FX_PAIRS;
        String[] cryptoPairs = BackTestConstants.TICKERS;

        List<StrategyPerformance> fxToDelete = dbService.getForDelete("aaa_strategy_performance_fx", cryptoPairs);
        fxToDelete.forEach(s->s.set_deleted(true));
        dbService.bulk("aaa_strategy_performance_fx", fxToDelete);

        List<StrategyPerformance> crToDelete = dbService.getForDelete("strategy_performance", fxPairs);
        crToDelete.forEach(s->s.set_deleted(true));
        dbService.bulk("strategy_performance", crToDelete);

        log.info("COMPLETED");

        return Response.ok().build();
    }
}
