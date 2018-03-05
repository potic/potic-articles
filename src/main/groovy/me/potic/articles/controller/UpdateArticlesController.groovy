package me.potic.articles.controller

import groovy.util.logging.Slf4j
import me.potic.articles.domain.Article
import me.potic.articles.domain.ArticleEvent
import me.potic.articles.domain.PocketArticle
import me.potic.articles.domain.User
import me.potic.articles.service.ArticlesService
import me.potic.articles.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import java.security.Principal

@RestController
@Slf4j
class UpdateArticlesController {

    @Autowired
    ArticlesService articlesService

    @Autowired
    UserService userService

    @CrossOrigin
    @PostMapping(path = '/user/me/article/{articleId}/markAsRead')
    void markArticleAsRead(@PathVariable String articleId, final Principal principal) {
        log.info "receive POST request for /user/me/$articleId/markAsRead"

        try {
            User user = userService.findUserByAuth0Token(principal.token)
            articlesService.markArticleAsRead(user, articleId)
        } catch (e) {
            log.error "POST request for /user/me/$articleId/markAsRead failed: $e.message", e
            throw new RuntimeException("POST request for /user/me/$articleId/markAsRead failed: $e.message", e)
        }
    }

    @PostMapping(path = '/user/{userId}/article/fromPocket')
    void upsertFromPocket(@PathVariable String userId, @RequestBody PocketArticle articleFromPocket) {
        log.info "receive POST request for /user/${userId}/article/fromPocket; BODY=${articleFromPocket}"

        try {
            articlesService.upsertFromPocket(userId, articleFromPocket)
        } catch (e) {
            log.error "POST request for /user/${userId}/article/fromPocket; BODY=${articleFromPocket} failed: $e.message", e
            throw new RuntimeException("POST request for /user/${userId}/article/fromPocket; BODY=${articleFromPocket} failed: $e.message", e)
        }
    }

    @PostMapping(path = '/article/{articleId}/event')
    @ResponseBody ResponseEntity<Void> addEventToArticle(@PathVariable String articleId, @RequestBody ArticleEvent articleEvent) {
        log.info "receive POST request for /article/${articleId}/event; body=${articleEvent}"

        try {
            articlesService.addEventToArticle(articleId, articleEvent)
            return new ResponseEntity<>(HttpStatus.OK)
        } catch (e) {
            log.error "POST request for /article/${articleId}/event; body=${articleEvent} failed: $e.message", e
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PutMapping(path = '/article')
    void updateArticle(@RequestBody Article article) {
        log.info "receive PUT request for /article; BODY=${article}"

        try {
            articlesService.updateArticle(article)
        } catch (e) {
            log.error "PUT request for /article; BODY=${article} failed: $e.message", e
            throw new RuntimeException("PUT request for /article; BODY=${article} failed: $e.message", e)
        }
    }
}