package com.fsoylemez.sakar.sakir.resource;

import com.fsoylemez.sakar.sakir.model.trade.SpotOrderCancelRequest;
import com.fsoylemez.sakar.sakir.model.trade.SpotOrderOpenRequest;
import com.fsoylemez.sakar.sakir.service.binance.BinanceSpotTradeService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/trade")
public class TradeResource {

    @Inject
    BinanceSpotTradeService tradeService;

    @POST
    @Path("/order/open")
    public Response openOrder(@Valid SpotOrderOpenRequest spotOrderOpenRequest) {
        return Response.ok(tradeService.openOrder(spotOrderOpenRequest)).build();
    }

    @POST
    @Path("/order/cancel")
    public Response cancelOrder(@Valid SpotOrderCancelRequest cancelOrderRequest) {
        return Response.ok(tradeService.cancelOrder(cancelOrderRequest)).build();
    }

    @GET
    @Path("/order/marketBuy/{symbol}/{quantity}")
    public Response marketBuyOrder(@PathParam("symbol") String symbol, String quantity) {
        return Response.ok(tradeService.marketBuyOrder(symbol, quantity)).build();
    }
}
