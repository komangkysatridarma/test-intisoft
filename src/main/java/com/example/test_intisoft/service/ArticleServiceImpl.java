package com.example.test_intisoft.service;

import com.example.test_intisoft.model.ArticleDTO;
import com.example.test_intisoft.model.Article;
import com.example.test_intisoft.model.Role;
import com.example.test_intisoft.model.User;
import com.example.test_intisoft.repository.ArticleRepository;
import com.example.test_intisoft.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public ArticleServiceImpl(ArticleRepository articleRepository,
                              UserRepository userRepository,
                              AuditLogService auditLogService) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('EDITOR') or hasRole('CONTRIBUTOR')")
    public ArticleDTO createArticle(ArticleDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Article article = new Article();
        article.setTitle(dto.title());
        article.setContent(dto.content());
        article.setAuthor(author);
        article.setPublic(dto.isPublic());
        article.setCreatedAt(LocalDateTime.now());
        article.setUpdatedAt(LocalDateTime.now());

        Article savedArticle = articleRepository.save(article);
        auditLogService.log("CREATE", "ARTICLE", savedArticle.getId(), username,
                "Created article: " + savedArticle.getTitle());

        return toDTO(savedArticle);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN') or " +
            "(hasRole('EDITOR') and @articleRepository.findById(#id).orElseThrow().author.username == authentication.name) or " +
            "(hasRole('CONTRIBUTOR') and @articleRepository.findById(#id).orElseThrow().author.username == authentication.name)")
    public ArticleDTO updateArticle(Long id, ArticleDTO dto) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_CONTRIBUTOR"))) {
            if (!article.getAuthor().getUsername().equals(username)) {
                throw new AccessDeniedException("You can only update your own articles");
            }
        }

        article.setTitle(dto.title());
        article.setContent(dto.content());
        article.setPublic(dto.isPublic());
        article.setUpdatedAt(LocalDateTime.now());

        Article updatedArticle = articleRepository.save(article);
        auditLogService.log("UPDATE", "ARTICLE", updatedArticle.getId(), username,
                "Updated article: " + updatedArticle.getTitle());

        return toDTO(updatedArticle);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('SUPER_ADMIN') or " +
            "(hasRole('EDITOR') and @articleRepository.findById(#id).orElseThrow().author.username == authentication.name)")
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article not found"));

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        articleRepository.deleteById(id);
        auditLogService.log("DELETE", "ARTICLE", id, username,
                "Deleted article: " + article.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article not found"));

        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_VIEWER"))) {
        }

        return toDTO(article);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleDTO> getAllArticles() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Article> articles;

        if (currentUser.getRole() == Role.SUPER_ADMIN) {
            articles = articleRepository.findAll();
        } else if (currentUser.getRole() == Role.EDITOR) {
            articles = articleRepository.findAll();
        } else if (currentUser.getRole() == Role.CONTRIBUTOR) {
            articles = articleRepository.findByAuthorOrIsPublic(currentUser, true);
        } else {
            articles = articleRepository.findByIsPublic(true);
        }

        return articles.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('VIEWER')")
    public List<ArticleDTO> getAllPublicArticles() {
        return articleRepository.findAll().stream()
                .filter(Article::isPublic)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ArticleDTO toDTO(Article article) {
        return new ArticleDTO(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getAuthor().getId(),
                article.isPublic()
        );
    }
}
