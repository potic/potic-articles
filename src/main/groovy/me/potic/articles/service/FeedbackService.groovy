package me.potic.articles.service

import groovy.util.logging.Slf4j
import me.potic.articles.domain.Article
import me.potic.articles.domain.ArticleEvent
import me.potic.articles.domain.ArticleEventType
import me.potic.articles.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.time.LocalDateTime

@Service
@Slf4j
class FeedbackService {

    @Autowired
    ArticlesService articlesService

    void liked(User user, Article article) {
        log.debug "emitting LIKED event for user ${user} and article ${article}..."

        try {
            ArticleEvent articleEvent = new ArticleEvent()
            articleEvent.type = ArticleEventType.LIKED
            articleEvent.articleId = article.id
            articleEvent.userId = user.id
            articleEvent.timestamp = LocalDateTime.now().toString()

            articlesService.addEventToArticle(article.id, articleEvent)
        } catch (e) {
            log.error "emitting LIKED event for user ${user} and article ${article} failed: $e.message", e
            throw new RuntimeException("emitting LIKED event for user ${user} and article ${article} failed: $e.message", e)
        }
    }

    void disliked(User user, Article article) {
        log.debug "emitting DISLIKED event for user ${user} and article ${article}..."

        try {
            ArticleEvent articleEvent = new ArticleEvent()
            articleEvent.type = ArticleEventType.DISLIKED
            articleEvent.articleId = article.id
            articleEvent.userId = user.id
            articleEvent.timestamp = LocalDateTime.now().toString()

            articlesService.addEventToArticle(article.id, articleEvent)
        } catch (e) {
            log.error "emitting DISLIKED event for user ${user} and article ${article} failed: $e.message", e
            throw new RuntimeException("emitting DISLIKED event for user ${user} and article ${article} failed: $e.message", e)
        }
    }
}
