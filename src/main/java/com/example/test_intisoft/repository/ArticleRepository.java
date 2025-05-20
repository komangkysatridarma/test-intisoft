package com.example.test_intisoft.repository;

import com.example.test_intisoft.model.Article;
import com.example.test_intisoft.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByAuthorOrIsPublic(User author, boolean isPublic);
    List<Article> findByIsPublic(boolean isPublic);
}
