package me.potic.articles.service

import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.articles.domain.Article
import me.potic.articles.domain.Card
import me.potic.articles.domain.PocketArticle
import me.potic.articles.domain.User
import org.apache.commons.lang3.RandomUtils
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

    List<Article> getLatestUserUnreadArticles(String userId, List<String> skipIds, Integer count, Integer minLength, Integer maxLength) {
        log.debug "getting $count latest unread articles with min length ${minLength} and max length ${maxLength} for user $userId skipping articles with ids $skipIds..."

        try {
            Criteria[] criteria = []
            criteria += where('userId').is(userId)
            criteria += where('fromPocket.status').ne('1')
            if (skipIds != null && !skipIds.empty) {
                criteria += where('id').nin(skipIds)
            }

            criteria += where('card.pocketId').exists(true)
            criteria += where('card.actual').is(true)

            if (minLength != null) {
                criteria += where('fromPocket.word_count').gt(minLength)
            }
            if (maxLength != null) {
                criteria += where('fromPocket.word_count').lte(maxLength)
            }

            def query = query(new Criteria().andOperator(criteria)).with(new Sort(Sort.Direction.DESC, 'fromPocket.time_added'))
            if (count != null) {
                query = query.limit(count)
            }

            return mongoTemplate.find(query, Article)
        } catch (e) {
            log.error "getting $count latest unread articles with min length ${minLength} and max length ${maxLength} for user $userId skipping articles with ids $skipIds failed: $e.message", e
            throw new RuntimeException("getting $count latest unread articles with min length ${minLength} and max length ${maxLength} for user $userId skipping articles with ids $skipIds failed: $e.message", e)
        }
    }

    List<Article> getRandomUserUnreadArticles(String userId, List<String> skipIds, Integer count) {
        log.debug "getting $count random unread articles for user $userId skipping articles with ids $skipIds..."

        try {
            Criteria[] criteria = []
            criteria += where('userId').is(userId)
            criteria += where('fromPocket.status').ne('1')
            if (skipIds != null && !skipIds.empty) {
                criteria += where('id').nin(skipIds)
            }

            criteria += where('card.pocketId').exists(true)
            criteria += where('card.actual').is(true)

            def query = query(new Criteria().andOperator(criteria))

            int totalCount = mongoTemplate.count(query, Article)

            Set<Article> randomArticles = []

            while (randomArticles.size() < count) {
                int randomSkip = RandomUtils.nextInt(0, totalCount)
                Article randomArticle = mongoTemplate.findOne(query.with(new Sort(Sort.Direction.DESC, 'fromPocket.time_added')).skip(randomSkip).limit(1), Article)
                randomArticles += randomArticle
            }

            return randomArticles as List
        } catch (e) {
            log.error "getting $count random unread articles for user $userId skipping articles with ids $skipIds failed: $e.message", e
            throw new RuntimeException("getting $count random unread articles for user $userId skipping articles with ids $skipIds failed: $e.message", e)
        }
    }

    List<Article> getRankedUserUnreadArticles(String userId, String rankId, List<String> skipIds, Integer count) {
        log.debug "getting $count ranked by ${rankId} unread articles for user $userId skipping articles with ids $skipIds..."

        try {
            Criteria[] criteria = []
            criteria += where('ranks.id').is(rankId)
            criteria += where('userId').is(userId)
            criteria += where('fromPocket.status').ne('1')
            if (skipIds != null && !skipIds.empty) {
                criteria += where('id').nin(skipIds)
            }

            criteria += where('card.pocketId').exists(true)
            criteria += where('card.actual').is(true)

            def query = query(new Criteria().andOperator(criteria))

            List<Article> result = mongoTemplate.find(query, Article)
            result = result.sort({ article -> -article.ranks.find({ rank -> rank.id == rankId }).value })

            if (count != null) {
                result = result.take(count)
            }

            return result
        } catch (e) {
            log.error "getting $count ranked by ${rankId} unread articles for user $userId skipping articles with ids $skipIds failed: $e.message", e
            throw new RuntimeException("getting $count ranked by ${rankId} unread articles for user $userId skipping articles with ids $skipIds failed: $e.message", e)
        }
    }

    void markArticleAsRead(User user, String articleId) {
        log.debug "marking article ${articleId} as read for user ${user.id}"

        try {
            Article readArticle = findArticle(articleId)
            pocketApiRest.post {
                request.uri.path = "/archive/${user.pocketAccessToken}/${readArticle.fromPocket.item_id}"
            }

            mongoTemplate.updateFirst(query(where('id').is(articleId)), update('fromPocket.status', '1'), Article)
        } catch (e) {
            log.error "marking article ${articleId} as read for user ${user.id} failed: $e.message", e
            throw new RuntimeException("marking article ${articleId} as read for user ${user.id} failed: $e.message", e)
        }
    }

    Article upsertFromPocket(String userId, PocketArticle articleFromPocket) {
        log.debug "upserting article $articleFromPocket for user $userId"

        try {
            Article article = findAlreadyIngestedFromPocket(userId, articleFromPocket)

            if (article == null) {
                article = new Article()
                article.userId = userId
            }

            article.fromPocket = articleFromPocket

            if (article.card == null) article.card = new Card()
            article.card.id = article.id
            article.card.actual = false

            mongoTemplate.save(article)

            if (article.card.id == null) {
                article.card.id = article.id
                mongoTemplate.save(article)
            }

            return article
        } catch (e) {
            log.error "upserting article $articleFromPocket for user $userId failed: $e.message", e
            throw new RuntimeException("upserting article $articleFromPocket for user $userId failed: $e.message", e)
        }
    }

    Article findAlreadyIngestedFromPocket(String userId, PocketArticle articleFromPocket) {
        log.debug "checking if article ${articleFromPocket} for user $userId was already ingested from pocket"

        try {
            List<Article> candidates = mongoTemplate.find(query(
                    new Criteria().andOperator(
                            where('userId').is(userId),
                            new Criteria().orOperator(
                                    where('fromPocket.item_id').is(articleFromPocket.item_id),
                                    where('fromPocket.resolved_id').is(articleFromPocket.resolved_id),
                                    where('fromPocket.given_url').is(articleFromPocket.given_url)
                            )
                    )
            ), Article)

            if (candidates.size() > 1) {
                log.warn("there are more than 1 already ingested candidate for user #${userId} and article from pocket ${articleFromPocket}")
            }

            Article tieBreakCandidate = null

            for (Article candidate : candidates) {
                if (candidate.fromPocket.item_id == articleFromPocket.item_id) {
                    return candidate
                }
                if (candidate.fromPocket.resolved_id == articleFromPocket.resolved_id) {
                    return candidate
                }
                if (candidate.fromPocket.given_url == articleFromPocket.given_url) {
                    tieBreakCandidate = candidate
                }
            }

            return tieBreakCandidate
        } catch (e) {
            log.error "checking if article ${articleFromPocket} for user $userId was already ingested from pocket failed: $e.message", e
            throw new RuntimeException("checking if article ${articleFromPocket} for user $userId was already ingested from pocket failed: $e.message", e)
        }
    }

    void updateArticle(Article article) {
        log.debug "updating article ${article}..."

        try {
            Article existing = findArticle(article.id)
            if (article.userId == null) article.userId = existing.userId
            if (article.fromPocket == null) article.fromPocket = existing.fromPocket
            if (article.card == null) article.card = existing.card
            if (article.ranks == null) article.ranks = existing.ranks

            mongoTemplate.save(article)
        } catch (e) {
            log.error "updating article ${article} failed: $e.message", e
            throw new RuntimeException("updating article ${article} failed: $e.message", e)
        }
    }

    Collection<Article> findWithNonActualCard(Integer count) {
        log.debug "getting $count articles with non-actual card..."

        try {
            Query query = query(new Criteria().andOperator(
                    where('fromPocket').ne(null),
                    new Criteria().orOperator(where('card.actual').is(null), where('card.actual').is(false))
            ))

            if (count != null) {
                query = query.limit(count)
            }

            return mongoTemplate.find(query, Article)
        } catch (e) {
            log.error "getting $count articles with non-actual card failed: $e.message", e
            throw new RuntimeException("getting $count articles with non-actual card failed: $e.message", e)
        }
    }

    List<Article> findWithoutRank(String rankId, int count) {
        log.debug "getting $count articles without rank ${rankId}..."

        try {
            Query query = query(where('ranks.id').ne(rankId))

            if (count != null) {
                query = query.limit(count)
            }

            return mongoTemplate.find(query, Article)
        } catch (e) {
            log.error "getting $count articles without rank ${rankId} failed: $e.message", e
            throw new RuntimeException("getting $count articles without rank ${rankId} failed: $e.message", e)
        }
    }

    private Article findArticle(String id) {
        mongoTemplate.find(query(where('id').is(id)), Article).first()
    }
}
