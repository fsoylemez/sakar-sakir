package com.fsoylemez.sakir.backtest.resource;

import com.binance.api.client.domain.market.CandlestickInterval;
import com.fsoylemez.sakir.backtest.model.ExecutionRequest;
import com.fsoylemez.sakir.backtest.model.ExecutionResult;
import com.fsoylemez.sakir.backtest.service.StrategyExecutorService;
import com.fsoylemez.sakir.strategy.model.StrategyExecutionResponse;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.List;

@Path("/test")
public class BackTestResource {

    @Inject
    StrategyExecutorService strategyExecutorService;

    @GET
    @Path("/{strategy}/{ticker}/{startDate}/{endDate}/{interval}")
        public List<StrategyExecutionResponse> executeLive(@PathParam("strategy") String strategy, @PathParam("ticker") String ticker, @PathParam("startDate") String startDate, @PathParam("endDate") String endDate, @PathParam("interval") String interval) {
        return strategyExecutorService.executeLive(strategy, CandlestickInterval.valueOf(interval), ticker, startDate, endDate);
    }

    @POST
    public ExecutionResult executeLive(ExecutionRequest request, @QueryParam("hidePositions") Boolean hidePositions) {
        return strategyExecutorService.executeLive(request, hidePositions);
    }

    @GET
    @Path("/db/{strategy}/{ticker}/{startDate}/{endDate}/{interval}")
    public List<StrategyExecutionResponse> executeDb(@PathParam("strategy") String strategy, @PathParam("ticker") String ticker, @PathParam("startDate") String startDate, @PathParam("endDate") String endDate, @PathParam("interval") String interval, @QueryParam("showPositions") Boolean hidePositions) {
        return strategyExecutorService.executeDb(strategy, CandlestickInterval.valueOf(interval), ticker, startDate, endDate, hidePositions);
    }

    @POST
    @Path("/db")
    public ExecutionResult executeDb(ExecutionRequest request, @QueryParam("showPositions") Boolean showPositions) {
        return strategyExecutorService.executeDb(request, showPositions);
    }
}
