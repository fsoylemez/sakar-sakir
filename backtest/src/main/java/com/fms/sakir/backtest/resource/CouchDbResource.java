package com.fms.sakir.backtest.resource;

import com.fms.sakir.backtest.db.couchdb.CouchDbService;
import com.fms.sakir.backtest.service.PopulationService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("db")
public class CouchDbResource {

    @Inject
    CouchDbService dbService;

    @Inject
    PopulationService populationService;

    @GET
    @Path("dropDatabases")
    public Response dropDbs() {
        dbService.dropDatabases();

        return Response.ok().build();
    }

    @GET
    @Path("/populate/{symbol}/{interval}/{year}/{month}/{day}")
    public Response populateFrom(String symbol, String interval, Integer year, Integer month, Integer day) {
        populationService.populate(symbol, interval, year, month, day);

        return Response.ok().build();
    }

    @GET
    @Path("/populate/{symbol}/{interval}")
    public Response populate(String symbol, String interval) {
        populationService.populate(symbol, interval, null, null, null);

        return Response.ok().build();
    }

    @POST
    @Path("/populate/{interval}")
    public Response populateAll(List<String> symbols, @PathParam("interval") String interval) {
        if(symbols == null || symbols.isEmpty()) {
            return Response.noContent().build();
        }
        for (String symbol : symbols) {
            populationService.populate(symbol, interval, null, null, null);
        }

        return Response.ok().build();
    }
}
