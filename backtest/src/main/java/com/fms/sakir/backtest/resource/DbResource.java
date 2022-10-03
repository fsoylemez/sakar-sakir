package com.fms.sakir.backtest.resource;

import com.fms.sakir.backtest.db.couchdb.CouchDbService;
import com.fms.sakir.backtest.model.StrategyPerformance;
import com.fms.sakir.backtest.util.BackTestConstants;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;

@Slf4j
@Path("/db")
public class DbResource {

    @Inject
    CouchDbService dbService;

    @GET
    @Path("dropDatabases")
    public Response dropDbs() {
        dbService.dropDatabases();

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
