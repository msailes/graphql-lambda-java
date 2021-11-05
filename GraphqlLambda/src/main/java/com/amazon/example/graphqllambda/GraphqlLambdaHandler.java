package com.amazon.example.graphqllambda;

import com.amazon.example.graphqllambda.util.DBUtil;
import com.amazon.example.graphqllambda.util.GraphQLUtil;
import com.amazon.example.graphqllambda.util.JsonConverter;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.common.net.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;


public class GraphqlLambdaHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    static String CONTNET_TYPE_JSON = "application/json";
    static String CONTNET_TYPE_GRAPHQL = "application/graphql";
    static String CONTENT_TYPE = "content-type";
    private static final Logger logger = LogManager.getLogger(GraphqlLambdaHandler.class);

    private JsonConverter jsonConverter;
    private GraphQLUtil graphQLUtil;

    public GraphqlLambdaHandler() {
        this.jsonConverter = new JsonConverter();
        DBUtil dbUtil = new DBUtil(jsonConverter);
        this.graphQLUtil = new GraphQLUtil(jsonConverter, dbUtil.getConnection());
    }


    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        logger.info("::: Event :::" + jsonConverter.toJson(event));
        String httpMethod = event.getRequestContext().getHttp().getMethod();
        if (httpMethod.equals("POST")) {
            return APIGatewayV2HTTPResponse.builder()
                    .withBody(handlePostRequest(event))
                    .withStatusCode(200)
                    .build();
        } else {
            return APIGatewayV2HTTPResponse.builder()
                    .withBody("Operation not supported. Only GET or POST is supported!")
                    .withStatusCode(501)
                    .build();
        }

    }

    private String handlePostRequest(APIGatewayV2HTTPEvent event) {

        if (event.getHeaders().get(CONTENT_TYPE).equals(CONTNET_TYPE_JSON)) {

            Map<String, Object> graphQLParams = jsonConverter.fromJson(event.getBody(), Map.class);
            Object query = graphQLParams.get("query");
            Object operationName = graphQLParams.get("operationName");
            Object variablesJson = graphQLParams.get("variables");

            String queryStr = "";
            if (query != null) {
                queryStr = query.toString();
            }

            logger.info("QUERY :: " + queryStr.replaceAll("\\n", ""));
            logger.info("OP NAME :: " + operationName);
            logger.info("VARS :: " + variablesJson);

            return graphQLUtil.processGraphQlRequest(queryStr, operationName == null ? null : operationName.toString(), variablesJson == null ? null : variablesJson.toString());
        } else if (event.getHeaders().get(HttpHeaders.CONTENT_TYPE).equals(CONTNET_TYPE_GRAPHQL)) {
            return graphQLUtil.processGraphQlRequest(event.getBody(), null, null);
        } else {
            throw new RuntimeException("Invalid or Missing Content-type header !!");
        }
    }


}
