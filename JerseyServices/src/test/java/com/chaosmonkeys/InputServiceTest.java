package com.chaosmonkeys;


import com.chaosmonkeys.inputservice.InputService;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.*;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class InputServiceTest extends JerseyTest {
    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    /**
     * Control over configuring values of the properties defined and described in the TestProperties class
     *
     * @return ResourceConfig
     */
    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);
        ResourceConfig config = new ResourceConfig(InputService.class);
        config.register(MultiPartFeature.class); // IMPORTANT: if you want to test multipart feature, remember to add this
        return config;
    }

    /**
     * Some advanced client configuration
     * This is for enabling uploading mulitpart feature, avoid
     * MessageBodyWriter not found error
     *
     * @param config
     */
    @Override
    protected void configureClient(ClientConfig config) {
        super.configureClient(config);
        // when you are using some advanced feature, remember to register
        config.register(MultiPartFeature.class);
    }

    /**
     * Test to see that the service does not accept GET request
     */
    @Test
    public void testGetRequest() {
        int errorCode = target("services/upload").request().get().getStatus();
        assertEquals(405, errorCode);
    }

    /**
     * Test to see that the service does not accept wrong parameter
     */
    @Test
    public void testUnsupportedMediaType() {
        int responseMsg = target("services/upload").request().post(Entity.text("asb")).getStatus();
        assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), responseMsg);
    }

    /**
     * Test with wrong parameter type (project id)
     */
    @Test
    public void testLackOfFormBodyPart(){
        MediaType contentType = MediaType.MULTIPART_FORM_DATA_TYPE;
        contentType = Boundary.addBoundary(contentType);
        // declare a file as part of the form data
        FormDataBodyPart filePart;
        try {
            // create test file
            final File testFile = folder.newFile("test.txt");

            filePart = new FileDataBodyPart("file", testFile);   // pom.xml

            // construct the entire form with all required parameters
            MultiPart multipartEntity = new FormDataMultiPart()
                    .field("project_id","test_id")
                    .field("format","csv")
                    .field("description","TEST DATA-SET DESCRIPTION")
                    .field("user_id", "test_u_id")
                    .field("name", "testname");
            // request the response Jersey 2.25 will not recognize multipartEntity.getMediaType()
            Response response = target("services/upload").request().post(Entity.entity(multipartEntity, contentType));

            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test with normal operation
     */
    @Test
    public void testNormalUpload() {
        MediaType contentType = MediaType.MULTIPART_FORM_DATA_TYPE;
        contentType = Boundary.addBoundary(contentType);
        // declare a file as part of the form data
        FormDataBodyPart filePart;
        try {
            // create test file
            final File testFile = folder.newFile("test.txt");
            filePart = new FileDataBodyPart("file", testFile);   // pom.xml

            // construct the entire form with all required parameters
            MultiPart multipartEntity = new FormDataMultiPart()
                    .field("project_id", "123")
                    .field("user_id", "test_u_id")
                    .field("name", "testname")
                    .bodyPart(filePart);
            // request the response Jersey 2.25 will not recognize multipartEntity.getMediaType()
            Response response = target("services/upload").request().post(Entity.entity(multipartEntity, contentType));

            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}