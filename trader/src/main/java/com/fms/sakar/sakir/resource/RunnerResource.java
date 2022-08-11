package com.fms.sakar.sakir.resource;

import com.fms.sakar.sakir.model.runner.RunnerRequest;
import com.fms.sakar.sakir.service.RunnerService;
import com.fms.sakir.strategy.exception.SakirException;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;

@Path("/runner")
public class RunnerResource {

    @Inject
    RunnerService runnerService;

    @POST
    public Response newRunner(@Valid RunnerRequest runnerRequest) {
        UUID taskId = runnerService.newRunner(runnerRequest);

        return Response.ok(taskId).build();
    }

    @GET
    @Path("/stop/{taskId}")
    public Response shutdown(@PathParam("taskId") UUID taskId) throws IOException {
        runnerService.stopTask(taskId);

        return Response.ok().build();
    }

    @GET
    @Path("/running")
    public Response getRunning() {
        return Response.ok(runnerService.getRunning()).build();
    }
}
