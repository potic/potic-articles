package me.potic.articles.controller

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer
import groovy.util.logging.Slf4j
import me.potic.articles.service.ArticlesService
import me.potic.articles.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

import javax.annotation.PostConstruct
import java.security.Principal

import static com.codahale.metrics.MetricRegistry.name

@RestController
@Slf4j
class UpdateArticlesController {

    @Autowired
    ArticlesService articlesService

    @Autowired
    UserService userService

    @Autowired
    MetricRegistry metricRegistry

    Timer markArticleAsReadTimer

    @PostConstruct
    void initMetrics() {
        markArticleAsReadTimer = metricRegistry.timer(name('request', 'user', 'me', 'article', 'markAsRead'))
    }

    @CrossOrigin
    @PostMapping(path = '/user/me/article/{articleId}/markAsRead')
    void markArticleAsRead(@PathVariable String articleId, final Principal principal) {
        final Timer.Context timerContext = markArticleAsReadTimer.time()
        log.info "receive request for /user/me/$articleId/markAsRead"

        try {
            String pocketSquareUserId = userService.fetchPocketSquareIdByAuth0Token(principal.token)
            articlesService.markArticleAsRead(pocketSquareUserId, articleId)
        } finally {
            long time = timerContext.stop()
            log.info "request for /user/me/$articleId/markAsRead took ${time / 1_000_000}ms"
        }
    }
}
