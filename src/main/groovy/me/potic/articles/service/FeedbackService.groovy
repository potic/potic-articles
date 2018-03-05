package me.potic.articles.service

import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.articles.domain.Article
import me.potic.articles.domain.ArticleEvent
import me.potic.articles.domain.ArticleEventType
import me.potic.articles.domain.User
import me.potic.sections.domain.Article
import me.potic.sections.domain.ArticleEvent
import me.potic.sections.domain.ArticleEventType
import me.potic.sections.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import java.time.LocalDateTime

@Service
@Slf4j
class FeedbackService {

    @Autowired
    ArticlesService articlesService

    void read(User user, Article article) {
        log.debug "emitting READ event for user ${user} and article ${article}..."

        try {
            ArticleEvent articleEvent = new ArticleEvent()
            articleEvent.type = ArticleEventType.READ
            articleEvent.articleId = article.id
            articleEvent.userId = user.id
            articleEvent.timestamp = LocalDateTime.now()

            articlesService.addEventToArticle(articleId, articleEvent)
        } catch (e) {
            log.error "emitting READ event for user ${user} and article ${article} failed: $e.message", e
            throw new RuntimeException("emitting READ event for user ${user} and article ${article} failed: $e.message", e)
        }
    }
}
