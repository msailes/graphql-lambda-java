package com.amazon.example.resolver.entity;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Post {

    String id;
    String author;
    String content;
    Integer views;
    List<Comment> comments;
}
