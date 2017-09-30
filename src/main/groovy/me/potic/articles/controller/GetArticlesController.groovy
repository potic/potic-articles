package me.potic.articles.controller

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import me.potic.articles.domain.Article
import me.potic.articles.service.ArticlesService
import me.potic.articles.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import java.security.Principal

@RestController
@Slf4j
class GetArticlesController {

    @Autowired
    ArticlesService articlesService

    @Autowired
    UserService userService

    @Timed(name = 'user.me.article.unread')
    @CrossOrigin
    @GetMapping(path = '/user/me/article/unread')
    @ResponseBody Collection<Article> userUnreadArticles(
            @RequestParam(value = 'cursorId', required = false) String cursorId,
            @RequestParam(value = 'count') Integer count,
            @RequestParam(value = 'minLength', required = false) Integer minLength,
            @RequestParam(value = 'maxLength', required = false) Integer maxLength,
            final Principal principal
    ) {
        log.info 'receive request for /user/me/article/unread'

        try {
            String userId = userService.findUserIdByAuth0Token(principal.token)
            return articlesService.getUserUnreadArticles(userId, cursorId, count, minLength, maxLength)
        } catch (e) {
            log.error "request for /user/me/article/unread failed: $e.message", e
            throw new RuntimeException("request for /user/me/article/unread failed: $e.message", e)
        }
    }
}
