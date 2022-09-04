package com.fms.sakir.backtest.resource;

import com.fms.sakir.backtest.model.PerformanceStats;
import com.fms.sakir.backtest.service.StrategyPerformanceService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/perf")
public class PerformanceResource {


    @Inject
    StrategyPerformanceService performanceService;

    @GET
    @Path("/stats/{startDate}/{endDate}")
    public PerformanceStats stats(@PathParam("startDate") String startDate, @PathParam("endDate") String endDate) {
        return performanceService.getPerformanceStatistics(startDate, endDate);
    }

    @GET
    @Path("/sync")
    public Response sync() {
        performanceService.sync(true);
        return Response.ok().build();
    }

    @GET
    @Path("/syncFx")
    public Response syncFx() {
        performanceService.sync(false);
        return Response.ok().build();
    }

    @GET
    @Path("/summary")
    public Response summary(@QueryParam("update") boolean update) {
        performanceService.prepareSummary(update);
        return Response.ok().build();
    }

    @GET
    @Path("/summaryFx")
    public Response summaryFx(@QueryParam("update") boolean update) {
        performanceService.prepareSummaryFx(update);
        return Response.ok().build();
    }

    @GET
    @Path("/summaryFx/byPair")
    public Response summaryFxByPair(@QueryParam("update") boolean update) {
        performanceService.prepareSummaryFxByPair(update);
        return Response.ok().build();
    }

    @GET
    @Path("/summary/byPair")
    public Response summaryByPair(@QueryParam("update") boolean update) {
        performanceService.prepareSummaryByPair(update);
        return Response.ok().build();
    }
}
