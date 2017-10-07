package me.potic.articles.service

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.articles.domain.Article
import me.potic.articles.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Service

import static org.springframework.data.mongodb.core.query.Criteria.where
import static org.springframework.data.mongodb.core.query.Query.query
import static org.springframework.data.mongodb.core.query.Update.update

@Service
@Slf4j
class ArticlesService {

    @Autowired
    MongoTemplate mongoTemplate

    HttpBuilder pocketApiRest

    @Autowired
    HttpBuilder pocketApiRest(@Value('${services.pocketApi.url}') String pocketApiServiceUrl) {
        pocketApiRest = HttpBuilder.configure {
            request.uri = pocketApiServiceUrl
        }
    }

    @Timed(name = 'getUserUnreadArticles')
    List<Article> getUserUnreadArticles(User user, String cursorId, Integer count, Integer minLength, Integer maxLength) {
        log.info "getting $count unread articles for user $user.id starting from $cursorId with length between $minLength and $maxLength"

        try {
            Criteria[] criteria = []
            criteria += where('userId').is(user.id)
            criteria += where('read').is(false)
            if (cursorId != null) {
                Article cursorArticle = mongoTemplate.find(query(where('id').is(cursorId)), Article).first()
                criteria += where('timeAdded').lt(cursorArticle.timeAdded)
            }

            if (minLength != null) {
                criteria += where('wordCount').gt(minLength)
            }
            if (maxLength != null) {
                criteria += where('wordCount').lte(maxLength)
            }

            criteria += where('title').ne(null)
            criteria += where('title').ne('')

            return mongoTemplate.find(
                    query(new Criteria().andOperator(criteria)).with(new Sort(Sort.Direction.DESC, 'timeAdded')).limit(count),
                    Article
            )
        } catch (e) {
            log.error "getting $count unread articles for user $user.id starting from $cursorId with length between $minLength and $maxLength failed: $e.message", e
            throw new RuntimeException("getting $count unread articles for user $user.id starting from $cursorId with length between $minLength and $maxLength failed: $e.message", e)
        }
    }

    @Timed(name = 'markArticleAsRead')
    void markArticleAsRead(User user, String articleId) {
        log.info "marking article $articleId as read for user $user.id"

        try {
            Article readArticle = mongoTemplate.find(query(where('id').is(articleId)), Article).first()
            pocketApiRest.post {
                request.uri.path = "/archive/${user.pocketAccessToken}/${readArticle.pocketId}"
            }

            mongoTemplate.updateFirst(query(where('id').is(articleId)), update('read', true), Article)
        } catch (e) {
            log.error "marking article $articleId as read for user $user.id failed: $e.message", e
            throw new RuntimeException("marking article $articleId as read for user $user.id failed: $e.message", e)
        }
    }
}
