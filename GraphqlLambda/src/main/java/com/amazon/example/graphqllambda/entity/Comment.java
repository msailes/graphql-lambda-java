package com.amazon.example.graphqllambda.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Comment {

    String id;
    String postId;
    String author;
    String content;
    Integer upvotes;
    Integer downvotes;

}
