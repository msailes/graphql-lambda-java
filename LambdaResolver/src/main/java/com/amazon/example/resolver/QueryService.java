package com.amazon.example.resolver;

import com.amazon.example.resolver.dao.CommentDao;
import com.amazon.example.resolver.dao.PostDao;
import com.amazon.example.resolver.entity.Comment;
import com.amazon.example.resolver.entity.Post;
import com.amazon.example.resolver.util.JsonConverter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QueryService {
    private JsonConverter jsonConverter;
    private PostDao postDao;
    private CommentDao commentDao;

    public QueryService(Connection connection, JsonConverter jsonConverter) {
        this.jsonConverter = jsonConverter;
        this.postDao = new PostDao(connection);
        this.commentDao = new CommentDao(connection);
    }

    String getPostByAuthor(Map<String, String> arguments) throws SQLException {
        String author = arguments.get("author");
        List<Post> posts = postDao.getPostByAuthor(author);
        return jsonConverter.toJson(posts);
    }

    String getPostById(Map<String, String> arguments) throws SQLException {
        String id = arguments.get("id");
        Post post = postDao.getPostById(id);
        return jsonConverter.toJson(post);
    }

    String getCommentsByPost(Map<String, String> source) throws SQLException {
        String postId = source.get("id");
        List<Comment> comments = commentDao.getCommentsByPost(postId);
        return jsonConverter.toJson(comments);
    }

    String getCommentsOnPost(Map<String, String> arguments) throws SQLException {
        String postId = arguments.get("postId");
        List<Comment> comments = commentDao.getCommentsByPost(postId);
        return jsonConverter.toJson(comments);
    }

    String getCommentsByAuthor(Map<String, String> arguments) throws SQLException {
        String author = arguments.get("author");
        List<Comment> comments = commentDao.getCommentsByAuthor(author);
        return jsonConverter.toJson(comments);
    }

    int getNumberOfCommentsOnPost(Map<String, String> arguments) throws SQLException {
        String postId = arguments.get("postId");
        return commentDao.getCommentsByPost(postId).size();
    }

    String createPost(Map<String, String> arguments) throws SQLException {
        Post post = Post.builder()
                .author(arguments.get("author"))
                .content(arguments.get("content"))
                .views(0)
                .id(UUID.randomUUID().toString())
                .build();
        postDao.createPost(post);
        return jsonConverter.toJson(post);
    }

    String createComment(Map<String, String> arguments) throws SQLException {
        Comment comment = Comment.builder()
                .author(arguments.get("author"))
                .content(arguments.get("content"))
                .postId(arguments.get("postId"))
                .upvotes(0)
                .downvotes(0)
                .id(UUID.randomUUID().toString())
                .build();

        commentDao.createComment(comment);
        return jsonConverter.toJson(comment);
    }

    String upvoteComment(Map<String, String> arguments) throws SQLException {
        String id = arguments.get("id");
        commentDao.upVoteComment(id);
        Comment comment = commentDao.getCommentsById(id);
        return jsonConverter.toJson(comment);
    }

    String downVoteComment(Map<String, String> arguments) throws SQLException {
        String id = arguments.get("id");
        commentDao.downVoteComment(id);
        Comment comment = commentDao.getCommentsById(id);
        return jsonConverter.toJson(comment);
    }

    String incrementViewCount(Map<String, String> arguments) throws SQLException {
        String id = arguments.get("id");
        commentDao.upVoteComment(id);
        postDao.incrementViewCount(id);
        Post post = postDao.getPostById(id);
        return jsonConverter.toJson(post);
    }
}