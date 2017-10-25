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
        log.info 'receive GET request for /user/me/article/unread'

        try {
            User user = userService.findUserByAuth0Token(principal.token)
            Collection<Article> response = articlesService.getUserUnreadArticles(user, cursorId, count, minLength, maxLength)

            log.debug "user ${user} requested unread articles, got ${response}"

            return response
        } catch (e) {
            log.error "GET request for /user/me/article/unread failed: $e.message", e
            throw new RuntimeException("GET request for /user/me/article/unread failed: $e.message", e)
        }
    }

    @Timed(name = 'article.search.nonActual')
    @CrossOrigin
    @GetMapping(path = '/article/search/nonActual')
    @ResponseBody Collection<Article> findNonActualArticles(
            @RequestParam(value = 'group') String groupName,
            @RequestParam(value = 'count', required = false) Integer count
    ) {
        log.info "receive GET request for /article/search/nonActual?group=${groupName}&count=${count}"

        try {
            return articlesService.findNonActualArticles(groupName, count)
        } catch (e) {
            log.error "GET request for /article/search/notActual?group=${groupName}&count=${count} failed: $e.message", e
            throw new RuntimeException("GET request for /article/search/nonActual?group=${groupName}&count=${count} failed: $e.message", e)
        }
    }
}
