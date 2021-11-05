package com.amazon.example.resolver;

import java.util.Map;
import com.amazon.example.resolver.util.DBUtil;
import com.amazon.example.resolver.util.JsonConverter;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handler for requests to Lambda function.
 */
public class LambdaHandler implements RequestHandler<Map<String, Object>, String> {

    private static final Logger logger = LogManager.getLogger(LambdaHandler.class);
    private QueryService queryService;
    private JsonConverter jsonConverter;
    private DBUtil dbUtil;

    public LambdaHandler() {
        this.jsonConverter = new JsonConverter();
        this.dbUtil = new DBUtil(this.jsonConverter);
        this.queryService = new QueryService(dbUtil.getConnection(), jsonConverter);
    }

    public String handleRequest(final Map<String, Object> input, final Context context) {

        logger.info(input.toString());

        String operation = input.get("field").toString();
        Map<String, String> arguments = (Map<String, String>) input.get("arguments");
        Map<String, String> source = (Map<String, String>) input.get("source");
        logger.info("Arguments :: " + arguments);
        logger.info("Source :: " + source);

        try {
            if (operation.equals("getPost")) {
                return queryService.getPostById(arguments);
            }
            if(operation.equals("getPostByAuthor")) {
                return queryService.getPostByAuthor(arguments);
            }
            if(operation.equals("commentsByPost")) {
                return queryService.getCommentsByPost(source);
            }
            if(operation.equals("getCommentsOnPost")) {
                return queryService.getCommentsOnPost(arguments);
            }
            if(operation.equals("getCommentsByAuthor")) {
                return queryService.getCommentsByAuthor(arguments);
            }
            if(operation.equals("getNumberOfCommentsOnPost")) {
                return queryService.getNumberOfCommentsOnPost(arguments) + "";
            }
            if(operation.equals("addPost")) {
                return queryService.createPost(arguments);
            }
            if(operation.equals("incrementViewCount")) {
                return queryService.incrementViewCount(arguments);
            }
            if(operation.equals("createComment")) {
                return queryService.createComment(arguments);
            }
            if(operation.equals("upVoteComment")) {
                return queryService.upvoteComment(arguments);
            }
            if(operation.equals("downVoteComment")) {
                return queryService.downVoteComment(arguments);
            } else {
                throw new RuntimeException("Unknown Operation!");
            }

        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }


}
