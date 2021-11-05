package com.amazon.example.graphqllambda.resolver;

import com.amazon.example.graphqllambda.dao.CommentDao;
import com.amazon.example.graphqllambda.entity.Comment;
import com.amazon.example.graphqllambda.entity.Post;
import graphql.schema.DataFetcher;

import java.sql.Connection;
import java.util.UUID;

public class CommentsDataResolver {

    private CommentDao commentDao;

    public CommentsDataResolver(Connection connection) {
        this.commentDao = new CommentDao(connection);
    }

    public DataFetcher getCommentByPostFetcher() {
        return dataFetchingEnvironment -> {
            Post post = dataFetchingEnvironment.getSource();
            return commentDao.getCommentsByPost(post.getId());
        };
    }

    public DataFetcher getCommentsCountFetcher() {
        return dataFetchingEnvironment -> {
            String postId = dataFetchingEnvironment.getArgument("postId");
            return commentDao.getCommentsByPost(postId).size();
        };
    }

    public DataFetcher getCommentByAuthorFetcher() {
        return dataFetchingEnvironment -> {
            String author = dataFetchingEnvironment.getArgument("author");
            return commentDao.getCommentsByAuthor(author);
        };
    }

    public DataFetcher getCommentByPostIdFetcher() {
        return dataFetchingEnvironment -> {
            String postId = dataFetchingEnvironment.getArgument("postId");
            return commentDao.getCommentsByAuthor(postId);
        };
    }

    public DataFetcher createCommentFetcher() {
        return dataFetchingEnvironment -> {
            Comment comment = Comment.builder()
                    .id(UUID.randomUUID().toString())
                    .downvotes(0)
                    .upvotes(0)
                    .postId(dataFetchingEnvironment.getArgument("postId"))
                    .content(dataFetchingEnvironment.getArgument("content"))
                    .author(dataFetchingEnvironment.getArgument("author"))
                    .build();
            commentDao.createComment(comment);
            return comment;
        };
    }

    public DataFetcher upvoteCommentFetcher() {
        return dataFetchingEnvironment -> {
            String commentId = dataFetchingEnvironment.getArgument("id");
            commentDao.upVoteComment(commentId);
            return commentDao.getCommentsById(commentId);
        };
    }

    public DataFetcher downvoteCommentFetcher() {
        return dataFetchingEnvironment -> {
            String commentId = dataFetchingEnvironment.getArgument("id");
            commentDao.downVoteComment(commentId);
            return commentDao.getCommentsById(commentId);
        };
    }

}
