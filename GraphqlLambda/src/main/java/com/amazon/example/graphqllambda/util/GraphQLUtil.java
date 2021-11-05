package com.amazon.example.graphqllambda.util;

import com.amazon.example.graphqllambda.resolver.CommentsDataResolver;
import com.amazon.example.graphqllambda.resolver.PostDataResolver;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.sql.Connection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GraphQLUtil {

    static String SCHEMA_FILE_NAME = "schema.graphqls";
    private static final Logger logger = LogManager.getLogger(GraphQLUtil.class);

    private GraphQL graphQL;
    private JsonConverter jsonConverter;
    private Connection connection;

    public GraphQLUtil(JsonConverter jsonConverter, Connection connection) {
        this.connection = connection;
        this.graphQL = GraphQL.newGraphQL(buildSchema()).build();
        this.jsonConverter = jsonConverter;
    }

    public GraphQL getGraphQL() {
        return this.graphQL;
    }

    private GraphQLSchema buildSchema() {
        try {
            URL url = Resources.getResource(SCHEMA_FILE_NAME);
            String sdl = Resources.toString(url, Charsets.UTF_8);
            TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
            RuntimeWiring runtimeWiring = buildWiring();
            SchemaGenerator schemaGenerator = new SchemaGenerator();
            return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private RuntimeWiring buildWiring() {
        PostDataResolver postDataResolver = new PostDataResolver(this.connection);
        CommentsDataResolver commentsDataResolver = new CommentsDataResolver(this.connection);
        return RuntimeWiring.newRuntimeWiring()
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("getPost", postDataResolver.getPostByIdFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Post")
                        .dataFetcher("comments", commentsDataResolver.getCommentByPostFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("getPostsByAuthor", postDataResolver.getPostByAuthorFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("getCommentsOnPost", commentsDataResolver.getCommentByPostIdFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("getNumberOfCommentsOnPost", commentsDataResolver.getCommentsCountFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("getCommentsByAuthor", commentsDataResolver.getCommentByAuthorFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("createPost", postDataResolver.createPostFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("incrementViewCount", postDataResolver.incrementViewCounterFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("createComment", commentsDataResolver.createCommentFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("upvoteComment", commentsDataResolver.upvoteCommentFetcher()))
                .type(TypeRuntimeWiring.newTypeWiring("Mutation")
                        .dataFetcher("downvoteComment", commentsDataResolver.downvoteCommentFetcher()))
                .build();
    }

    public String processGraphQlRequest(String query, String operationName, String variablesJson) {
        Map<String, Object> variableMap = convertVariablesJson(variablesJson);

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(query)
                .operationName(operationName)
                .variables(variableMap).build();

        CompletableFuture<ExecutionResult> customExecutionResult = graphQL.executeAsync(executionInput);
        Object returnObj = customExecutionResult.thenApply(ExecutionResult::toSpecification);
        String result = jsonConverter.toJson(returnObj);
        return result.substring(10, result.length() - 1);
    }

    private Map<String, Object> convertVariablesJson(String jsonMap) {
        if (jsonMap == null) {
            return Collections.emptyMap();
        }
        return jsonConverter.fromJson(jsonMap, Map.class);
    }


}
