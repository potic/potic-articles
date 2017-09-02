package me.potic.articles.controller

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer
import groovy.util.logging.Slf4j
import me.potic.articles.domain.Article
import me.potic.articles.service.ArticlesService
import me.potic.articles.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

import javax.annotation.PostConstruct
import java.security.Principal

import static com.codahale.metrics.MetricRegistry.name

@RestController
@Slf4j
class GetArticlesController {

    @Autowired
    ArticlesService articlesService

    @Autowired
    UserService userService

    @Autowired
    MetricRegistry metricRegistry

    Timer userUnreadArticlesTimer

    @PostConstruct
    void initMetrics() {
        userUnreadArticlesTimer = metricRegistry.timer(name('request', 'user', 'me', 'article', 'unread'))
    }

    @CrossOrigin
    @GetMapping(path = '/user/me/article/unread')
    @ResponseBody Collection<Article> userUnreadArticles(
            @RequestParam(value = 'cursorId', required = false) String cursorId,
            @RequestParam(value = 'count') Integer count,
            @RequestParam(value = 'minLength', required = false) Integer minLength,
            @RequestParam(value = 'maxLength', required = false) Integer maxLength,
            final Principal principal
    ) {
        final Timer.Context timerContext = userUnreadArticlesTimer.time()
        log.info "receive request for /user/me/article/unread"

        try {
            String pocketSquareUserId = userService.fetchPocketSquareIdByAuth0Token(principal.token)
            return articlesService.getUserUnreadArticles(pocketSquareUserId, cursorId, count, minLength, maxLength)
        } finally {
            long time = timerContext.stop()
            log.info "request for /user/me/article/unread took ${time / 1_000_000}ms"
        }
    }
}
