package me.potic.articles.controller

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import me.potic.articles.service.ArticlesService
import me.potic.articles.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

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
            String pocketSquareUserId = userService.fetchPocketSquareIdByAuth0Token(principal.token)
            articlesService.markArticleAsRead(pocketSquareUserId, articleId)
        } catch (e) {
            log.error "request for /user/me/$articleId/markAsRead failed: $e.message", e
            throw new RuntimeException("request for /user/me/$articleId/markAsRead failed: $e.message", e)
        }
    }
}
