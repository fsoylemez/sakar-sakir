package com.fms.sakir.backtest.resource;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.fms.sakir.backtest.model.ExecutionRequest;
import com.fms.sakir.backtest.model.ExecutionResponse;
import com.fms.sakir.backtest.service.StrategyExecutorService;
import com.fms.sakir.strategy.model.StrategyExecutionResponse;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.List;

@Path("/test")
public class BackTestResource {

    @Inject
    StrategyExecutorService strategyExecutorService;

    @GET
    @Path("/{strategy}/{ticker}/{startDate}/{endDate}/{interval}")
    public List<StrategyExecutionResponse> execute(@PathParam("strategy") String strategy, @PathParam("ticker") String ticker, @PathParam("startDate") String startDate, @PathParam("endDate") String endDate, @PathParam("interval") String interval) {
        return strategyExecutorService.execute(strategy, CandlestickInterval.valueOf(interval), ticker, startDate, endDate);
    }

    @POST
    public List<ExecutionResponse> execute(ExecutionRequest request, @QueryParam("showPositions") Boolean showPositions) {
        return strategyExecutorService.execute(request, showPositions);
    }

}
