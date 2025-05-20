package com.example.test_intisoft.service;

import com.example.test_intisoft.model.ArticleDTO;

import java.util.List;

public interface ArticleService {
    ArticleDTO createArticle(ArticleDTO dto);
    ArticleDTO updateArticle(Long id, ArticleDTO dto);
    void deleteArticle(Long id);
    ArticleDTO getArticleById(Long id);
    List<ArticleDTO> getAllArticles();
    List<ArticleDTO> getAllPublicArticles();
}