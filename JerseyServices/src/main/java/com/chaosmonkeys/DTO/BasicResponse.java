package com.chaosmonkeys.DTO;

/**
 * BasicResponse DTO for JSON response with only one val
 * such as "{response : OK}" and "{response : ERROR}"
 */
public class BasicResponse {
    //Response field
    private final String response;

    private BasicResponse(BasicResponseBuilder builder) {
        this.response = builder.response;
    }

    public BasicResponse() {
        BasicResponse bResponse = new BasicResponse.BasicResponseBuilder().build();
        this.response = bResponse.getResponse();
    }

    public BasicResponse(String response) {
        BasicResponse bResponse = new BasicResponse.BasicResponseBuilder().build();
        this.response = bResponse.getResponse();
    }

    public String getResponse() {
        return response;
    }

    public static class BasicResponseBuilder {
        private String response;

        public BasicResponseBuilder response(String response) {
            this.response = response;
            return this;
        }

        public BasicResponseBuilder responseOK() {
            this.response = "OK";
            return this;
        }

        public BasicResponseBuilder responseError() {
            this.response = "ERROR";
            return this;
        }

        public BasicResponse build() {
            return new BasicResponse(this);
        }
    }
}

