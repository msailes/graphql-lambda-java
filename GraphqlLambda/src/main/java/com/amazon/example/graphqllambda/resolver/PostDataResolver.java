package com.amazon.example.graphqllambda.resolver;

import com.amazon.example.graphqllambda.dao.PostDao;
import com.amazon.example.graphqllambda.entity.Post;
import graphql.schema.DataFetcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.util.UUID;

public class PostDataResolver {

    private static final Logger logger = LogManager.getLogger(PostDataResolver.class);

    private PostDao postDao;

    public PostDataResolver(Connection connection) {
        this.postDao = new PostDao(connection);
    }

    public DataFetcher getPostByIdFetcher() {
        return dataFetchingEnvironment -> {
            String postId = dataFetchingEnvironment.getArgument("id");
            return postDao.getPostById(postId);
        };
    }

    public DataFetcher getPostByAuthorFetcher() {
        return dataFetchingEnvironment -> {
            String author = dataFetchingEnvironment.getArgument("author");
            return postDao.getPostByAuthor(author);
        };
    }

    public DataFetcher createPostFetcher() {
        return dataFetchingEnvironment -> {
            String author = dataFetchingEnvironment.getArgument("author");
            String content = dataFetchingEnvironment.getArgument("content");
            String uuid = UUID.randomUUID().toString();
            Post post = Post.builder()
                    .id(uuid)
                    .author(author)
                    .content(content)
                    .views(0)
                    .build();
            postDao.createPost(post);
            return post;
        };
    }

    public DataFetcher incrementViewCounterFetcher() {
        return dataFetchingEnvironment -> {
            String postId = dataFetchingEnvironment.getArgument("id");
            postDao.incrementViewCount(postId);
            return postDao.getPostById(postId);
        };
    }


}
