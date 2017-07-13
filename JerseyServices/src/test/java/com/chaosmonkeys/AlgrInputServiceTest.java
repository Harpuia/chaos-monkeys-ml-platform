package com.chaosmonkeys;

import com.chaosmonkeys.DTO.BaseResponse;
import com.chaosmonkeys.algrinputservice.AlgorithmResource;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
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
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.Assert.assertEquals;

public class AlgrInputServiceTest extends JerseyTest{

    private static final String SERVICE_PATH = "services/algr/upload";

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
        ResourceConfig config = new ResourceConfig(AlgorithmResource.class);
        config.register(JacksonFeature.class);
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
        int errorCode = target(SERVICE_PATH).request().get().getStatus();
        assertEquals(405, errorCode);
    }

    /**
     * Test to see that the service does not accept wrong parameter
     */
    @Test
    public void testUnsupportedMediaType() {
        int responseMsg = target(SERVICE_PATH).request().post(Entity.text("asb")).getStatus();
        assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), responseMsg);
    }

    /**
     * Test with wrong parameter type (project id)
     */
    @Test
    public void testBlankParameters(){
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
                    .field("language","R")
                    .field("description","")
                    .field("user_id", "")
                    .field("name", "testname")
                    .bodyPart(filePart);
            // request the response Jersey 2.25 will not recognize multipartEntity.getMediaType()
            Response response = target(SERVICE_PATH).request().accept(MediaType.APPLICATION_JSON).post(Entity.entity(multipartEntity, contentType));
            BaseResponse resEntity = (BaseResponse) response.readEntity(BaseResponse.class);
           // BaseResponse resEntity = (BaseResponse)response.getEntity();

            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals(AlgorithmResource.ERR_BLANK_PARAMS, resEntity.getCode());
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test with unsupported machine learning language
     * should refuse the request
     */
    @Test
    public void testUnsupportedDevLanguage(){
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
                    .field("language","Rust")
                    .field("description","test descritpion")
                    .field("user_id", "gogogo")
                    .field("name", "testname")
                    .bodyPart(filePart);
            // request the response Jersey 2.25 will not recognize multipartEntity.getMediaType()
            Response response = target(SERVICE_PATH).request().accept(MediaType.APPLICATION_JSON).post(Entity.entity(multipartEntity, contentType));
            BaseResponse resEntity = response.readEntity(BaseResponse.class);

            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals(AlgorithmResource.ERR_UNSUPPORTED_LANG, resEntity.getCode());
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test with normal operation
     */
    @Test
    public void testLackOfFormBodyPart() {
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
                    .field("language","R")
                    .field("description","TEST ALGORITHMT DESCRIPTION")
                    .field("user_id", "test_u_id")
                    .field("name", "testname");
            // request the response Jersey 2.25 will not recognize multipartEntity.getMediaType()
            Response response = target(SERVICE_PATH).request().post(Entity.entity(multipartEntity, contentType));
            BaseResponse resEntity =  response.readEntity(BaseResponse.class);

            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals(AlgorithmResource.ERR_FILE_BODYPART_MISSING, resEntity.getCode());

            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test with normal operation
     */
    @Test
    public void testNormalUploadWithoutAllRequiredFiles() {
        MediaType contentType = MediaType.MULTIPART_FORM_DATA_TYPE;
        contentType = Boundary.addBoundary(contentType);
        // declare a file as part of the form data
        FormDataBodyPart filePart;
        try {
            // create test file
            final File testFile = folder.newFile("test.zip");

            ZipOutputStream zipStream;
            OutputStream stream = new FileOutputStream(testFile);
            stream = new BufferedOutputStream(stream);
            zipStream = new ZipOutputStream(stream);
            ZipEntry entry = new ZipEntry("piupiu.java");
            zipStream.putNextEntry(entry);
            // Missing call to 'write'
            zipStream.closeEntry();
            zipStream.close();

            filePart = new FileDataBodyPart("file", testFile);   // pom.xml

            // construct the entire form with all required parameters
            MultiPart multipartEntity = new FormDataMultiPart()
                    .field("language","R")
                    .field("description","TEST ALGORITHMT DESCRIPTION")
                    .field("user_id", "test_u_id")
                    .field("name", "tesdfdtname")
                    .bodyPart(filePart);
            // request the response Jersey 2.25 will not recognize multipartEntity.getMediaType()
            Response response = target(SERVICE_PATH).request().post(Entity.entity(multipartEntity, contentType));
            BaseResponse resEntity = response.readEntity(BaseResponse.class);

            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
            assertEquals(AlgorithmResource.ERR_REQUIRED_FILE_MISSING, resEntity.getCode());
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
            // create test zip file

            final File testFile = folder.newFile("test.zip");

            ZipOutputStream zipStream;
            OutputStream stream = new FileOutputStream(testFile);
            stream = new BufferedOutputStream(stream);
            zipStream = new ZipOutputStream(stream);
            ZipEntry entry = new ZipEntry("Main.R");
            zipStream.putNextEntry(entry);
            // Missing call to 'write'
            zipStream.closeEntry();
            zipStream.close();

            filePart = new FileDataBodyPart("file", testFile);   // pom.xml

            // construct the entire form with all required parameters
            MultiPart multipartEntity = new FormDataMultiPart()
                    .field("language","R")
                    .field("description","TEST ALGORITHMT DESCRIPTION")
                    .field("user_id", "test_u_id")
                    .field("name", "tesdfdtname")
                    .bodyPart(filePart);
            // request the response Jersey 2.25 will not recognize multipartEntity.getMediaType()
            Response response = target(SERVICE_PATH).request().post(Entity.entity(multipartEntity, contentType));
            BaseResponse resEntity = response.readEntity(BaseResponse.class);


            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            assertEquals(AlgorithmResource.CHECK_SUCCESS, resEntity.getCode());
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
