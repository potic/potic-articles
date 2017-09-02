package me.potic.articles.service

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.articles.domain.Article
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

import static com.codahale.metrics.MetricRegistry.name
import static org.springframework.data.mongodb.core.query.Criteria.where
import static org.springframework.data.mongodb.core.query.Query.query
import static org.springframework.data.mongodb.core.query.Update.update

@Service
@Slf4j
class ArticlesService {

    @Autowired
    MongoTemplate mongoTemplate

    @Autowired
    HttpBuilder pocketApiRest

    @Autowired
    MetricRegistry metricRegistry

    Timer getUserUnreadArticlesTimer

    Timer markArticleAsReadTimer

    @PostConstruct
    void initMetrics() {
        getUserUnreadArticlesTimer = metricRegistry.timer(name('service', 'articles', 'getUserUnreadArticles'))
        markArticleAsReadTimer = metricRegistry.timer(name('service', 'articles', 'markArticleAsRead'))
    }

    Collection<Article> getUserUnreadArticles(String pocketSquareUserId, String cursorId, Integer count, Integer minLength, Integer maxLength) {
        final Timer.Context timerContext = getUserUnreadArticlesTimer.time()
        log.info "getting $count unread articles for user $pocketSquareUserId starting from $cursorId with length between $minLength and $maxLength"

        try {
            CriteriaDefinition unreadQuery
            if (cursorId == null) {
                unreadQuery = where('userId').is(pocketSquareUserId).and('read').is(false)
            } else {
                Article cursorArticle = mongoTemplate.find(query(where('id').is(cursorId)), Article).first()
                unreadQuery = where('userId').is(pocketSquareUserId).and('read').is(false).and('timeAdded').lt(cursorArticle.timeAdded)
            }

            if (minLength != null) {
                unreadQuery = unreadQuery.and('wordCount').gt(minLength)
            }
            if (maxLength != null) {
                unreadQuery = unreadQuery.and('wordCount').lte(maxLength)
            }

            return mongoTemplate.find(
                    query(unreadQuery).with(new Sort(Sort.Direction.DESC, 'timeAdded')).limit(count),
                    Article
            )
        } finally {
            long time = timerContext.stop()
            log.info "getting $count unread articles for user $pocketSquareUserId starting from $cursorId with length between $minLength and $maxLength took ${time / 1_000_000}ms"
        }
    }

    void markArticleAsRead(String pocketSquareUserId, String articleId) {
        final Timer.Context timerContext = markArticleAsReadTimer.time()
        log.info "marking article $articleId as read for user $pocketSquareUserId"

        try {
            Article readArticle = mongoTemplate.find(query(where('id').is(articleId)), Article).first()
            pocketApiRest.post {
                request.uri.path = "/archive/$pocketSquareUserId/${readArticle.pocketId}"
            }

            mongoTemplate.updateFirst(query(where('id').is(articleId)), update('read', true), Article)
        } finally {
            long time = timerContext.stop()
            log.info "marking article $articleId as read for user $pocketSquareUserId took ${time / 1_000_000}ms"
        }
    }
}
