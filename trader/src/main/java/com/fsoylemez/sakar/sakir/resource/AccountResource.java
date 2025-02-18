package com.fsoylemez.sakar.sakir.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsoylemez.sakar.sakir.service.binance.BinanceAccountService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Slf4j
@Path("/account")
public class AccountResource {

    @Inject
    BinanceAccountService accountInformationService;

    @GET
    public Response getAccountInfo() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return Response.ok(objectMapper.writeValueAsString(accountInformationService.getAccount())).build();
        } catch (JsonProcessingException e) {
            log.error("Error transforming account info to json.", e);
            return Response.serverError().build();
        }
    }

    @GET
    @Path("/balances")
    public Response getBalances() {
        return Response.ok(accountInformationService.getBalances()).build();
    }

    @GET
    @Path("/trades/{symbol}")
    public Response getTrades(@PathParam("symbol") String symbol) {
        return Response.ok(accountInformationService.getTrades(symbol)).build();
    }

    @GET
    @Path("/openOrders/{symbol}")
    public Response getOpenOrders(@PathParam("symbol") String symbol) {
        return Response.ok(accountInformationService.getOpenOrders(symbol)).build();
    }
}
