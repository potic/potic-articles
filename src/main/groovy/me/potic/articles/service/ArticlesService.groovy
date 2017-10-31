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
import org.springframework.data.mongodb.core.query.Query
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
            criteria += where('fromPocket.status').ne('1')
            if (cursorId != null) {
                Article cursorArticle = mongoTemplate.find(query(where('id').is(cursorId)), Article).first()
                criteria += where('fromPocket.time_added').lt(cursorArticle.fromPocket.time_added)
            }

            if (minLength != null) {
                criteria += where('fromPocket.word_count').gt(minLength)
            }
            if (maxLength != null) {
                criteria += where('fromPocket.word_count').lte(maxLength)
            }

            criteria += where('basicCard.actual').is(true)

            return mongoTemplate.find(
                    query(new Criteria().andOperator(criteria)).with(new Sort(Sort.Direction.DESC, 'fromPocket.time_added')).limit(count),
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
                request.uri.path = "/archive/${user.pocketAccessToken}/${readArticle.fromPocket.item_id}"
            }

            mongoTemplate.updateFirst(query(where('id').is(articleId)), update('fromPocket.status', '1'), Article)
        } catch (e) {
            log.error "marking article $articleId as read for user $user.id failed: $e.message", e
            throw new RuntimeException("marking article $articleId as read for user $user.id failed: $e.message", e)
        }
    }

    @Timed(name = 'upsertFromPocket')
    Article upsertFromPocket(String userId, Map articleFromPocket) {
        log.info "upserting article $articleFromPocket for user $userId"

        try {
            Article article = findAlreadyIngestedFromPocket(userId, articleFromPocket)

            if (article == null) {
                article = new Article()
                article.userId = userId
            }

            article.fromPocket = articleFromPocket
            if (article.fromPocket.time_added != null) article.fromPocket.time_added = Long.parseLong(article.fromPocket.time_added)
            if (article.fromPocket.time_updated != null) article.fromPocket.time_updated = Long.parseLong(article.fromPocket.time_updated)
            if (article.fromPocket.time_favorited != null) article.fromPocket.time_favorited = Long.parseLong(article.fromPocket.time_favorited)
            if (article.fromPocket.time_read != null) article.fromPocket.time_read = Long.parseLong(article.fromPocket.time_read)
            if (article.fromPocket.word_count != null) article.fromPocket.word_count = Long.parseLong(article.fromPocket.word_count)

            if (article.basicCard == null) article.basicCard = [:]
            article.basicCard.id = article.id
            article.basicCard.actual = false

            mongoTemplate.save(article)

            if (article.basicCard.id == null) {
                article.basicCard.id = article.id
                mongoTemplate.save(article)
            }

            return article
        } catch (e) {
            log.error "upserting article $articleFromPocket for user $userId failed: $e.message", e
            throw new RuntimeException("upserting article $articleFromPocket for user $userId failed: $e.message", e)
        }
    }

    @Timed(name = 'alreadyIngestedFromPocket')
    Article findAlreadyIngestedFromPocket(String userId, Map articleFromPocket) {
        log.info "checking if article for user $userId was already ingested from pocket"

        try {
            List<Article> candidates = mongoTemplate.find(query(
                    new Criteria().andOperator(
                            where('userId').is(userId),
                            new Criteria().orOperator(
                                    where('fromPocket.item_id').is(articleFromPocket['item_id']),
                                    where('fromPocket.resolved_id').is(articleFromPocket['resolved_id']),
                                    where('fromPocket.given_url').is(articleFromPocket['given_url'])
                            )
                    )
            ), Article)

            if (candidates.size() > 1) {
                log.warn("there are more than 1 already ingested candidate for user #${userId} and article from pocket ${articleFromPocket}")
            }

            Article tieBreakCandidate = null

            for (Article candidate : candidates) {
                if (candidate['fromPocket']['item_id'] == articleFromPocket['item_id']) {
                    return candidate
                }
                if (candidate['fromPocket']['resolved_id'] == articleFromPocket['resolved_id']) {
                    return candidate
                }
                if (candidate['fromPocket']['given_url'] == articleFromPocket['given_url']) {
                    tieBreakCandidate = candidate
                }
            }

            return tieBreakCandidate
        } catch (e) {
            log.error "checking if article for user $userId was already ingested from pocket failed: $e.message", e
            throw new RuntimeException("checking if article for user $userId was already ingested from pocket failed: $e.message", e)
        }
    }

    @Timed(name = 'updateArticle')
    void updateArticle(Article article) {
        log.info "updating article ${article}..."

        try {
            mongoTemplate.save(article)
        } catch (e) {
            log.error "updating article ${article} failed: $e.message", e
            throw new RuntimeException("updating article ${article} failed: $e.message", e)
        }
    }

    @Timed(name = 'findNonActualArticles')
    Collection<Article> findNonActualArticles(String groupName, Integer count) {
        log.info "getting $count non-actual articles for group $groupName..."

        try {
            Query query = query(new Criteria().andOperator(
                    where('fromPocket').ne(null),
                    new Criteria().orOperator(where('basicCard.actual').is(null), where('basicCard.actual').is(false))
            ))

            if (count != null) {
                query = query.limit(count)
            }

            return mongoTemplate.find(query, Article)
        } catch (e) {
            log.error "getting $count non-actual articles for group $groupName failed: $e.message", e
            throw new RuntimeException("getting $count non-actual articles for group $groupName failed: $e.message", e)
        }
    }
}
