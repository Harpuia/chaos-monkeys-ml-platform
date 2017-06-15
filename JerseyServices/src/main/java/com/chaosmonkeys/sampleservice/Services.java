package com.chaosmonkeys.sampleservice;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import com.chaosmonkeys.DTO.BasicResponse;
import javax.ws.rs.GET;

/**
 * Class containing all possible service calls (API methods)
 */
@Path("/services")
public class Services {
    //TODO: Put your service methods here (refer to Jersey tutorial on Wiki)
    /**
     * Sample method to illustrate changing the state of the SimpleService.
     *
     * @param command Command received from the caller.
     * @return Response
     */
    /*@GET
    @Path("/changeState/{command}")
    @Produces(MediaType.APPLICATION_JSON)
    public BasicResponse changeState(@PathParam("command") String command) {
        //Response variable
        BasicResponse response;

        //Handle each command differently
        switch (command) {
            case "RUN":
                Launcher.setServiceStatus(Launcher.RUNNING);
                response = new BasicResponse.BasicResponseBuilder().responseOK().build();
                break;
            case "STOP":
                Launcher.setServiceStatus(Launcher.STOPPED);
                response = new BasicResponse.BasicResponseBuilder().responseOK().build();
                break;
            default:
                response = new BasicResponse.BasicResponseBuilder().responseError().build();
                break;
        }
        return response;
    }*/
}
