package me.potic.articles.controller

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import me.potic.articles.domain.Article
import me.potic.articles.domain.User
import me.potic.articles.service.ArticlesService
import me.potic.articles.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import java.security.Principal

@RestController
@Slf4j
class UpdateArticlesController {

    @Autowired
    ArticlesService articlesService

    @Autowired
    UserService userService

    @Timed(name = 'user.me.article.markAsRead')
    @CrossOrigin
    @PostMapping(path = '/user/me/article/{articleId}/markAsRead')
    void markArticleAsRead(@PathVariable String articleId, final Principal principal) {
        log.info "receive request for /user/me/$articleId/markAsRead"

        try {
            User user = userService.findUserByAuth0Token(principal.token)
            articlesService.markArticleAsRead(user, articleId)
        } catch (e) {
            log.error "request for /user/me/$articleId/markAsRead failed: $e.message", e
            throw new RuntimeException("request for /user/me/$articleId/markAsRead failed: $e.message", e)
        }
    }

    @Timed(name = 'user.article.fromPocket')
    @CrossOrigin
    @PostMapping(path = '/user/{userId}/article/fromPocket')
    void upsertFromPocket(@PathVariable String userId, @RequestBody Map articleFromPocket) {
        log.info "receive POST request for /user/${userId}/article/fromPocket; BODY=${articleFromPocket}"

        try {
            articlesService.upsertFromPocket(userId, articleFromPocket)
        } catch (e) {
            log.error "POST request for /user/${userId}/article/fromPocket; BODY=${articleFromPocket} failed: $e.message", e
            throw new RuntimeException("POST request for /user/${userId}/article/fromPocket; BODY=${articleFromPocket} failed: $e.message", e)
        }
    }

    @Timed(name = 'user.article.PUT')
    @CrossOrigin
    @PutMapping(path = '/article')
    void updateArticle(@RequestBody Article article) {
        log.info "receive PUT request for /article"

        try {
            articlesService.updateArticle(article)
        } catch (e) {
            log.error "PUT request for /article failed: $e.message", e
            throw new RuntimeException("PUT request for /article failed: $e.message", e)
        }
    }
}
