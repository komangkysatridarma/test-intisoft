package com.example.test_intisoft.model;

public record ArticleDTO(
        Long id,
        String title,
        String content,
        Long authorId,
        boolean isPublic
) {
}
