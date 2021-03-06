package me.potic.articles.query

import com.coxautodev.graphql.tools.GraphQLQueryResolver
import groovy.util.logging.Slf4j
import me.potic.articles.domain.Article
import me.potic.articles.service.ArticlesService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
class ArticleQuery implements GraphQLQueryResolver {

    @Autowired
    ArticlesService articlesService

    List<Article> getLatestUnread(String userId, List<String> skipIds, Integer count, Integer minLength, Integer maxLength) {
        log.info "receive graphql query latestUnread(userId=${userId}, skipIds=${skipIds}, count=${count}, minLength=${minLength}, maxLength=${maxLength})"

        try {
            return articlesService.getLatestUserUnreadArticles(userId, skipIds, count, minLength, maxLength)
        } catch (e) {
            log.error "graphql query latestUnread(userId=${userId}, skipIds=${skipIds}, count=${count}, minLength=${minLength}, maxLength=${maxLength}) failed: $e.message", e
            throw new RuntimeException("graphql query latestUnread(userId=${userId}, skipIds=${skipIds}, count=${count}, minLength=${minLength}, maxLength=${maxLength}) failed: $e.message", e)
        }
    }

    List<Article> getRandomUnread(String userId, List<String> skipIds, Integer count) {
        log.info "receive graphql query randomUnread(userId=${userId}, skipIds=${skipIds}, count=${count})"

        try {
            return articlesService.getRandomUserUnreadArticles(userId, skipIds, count)
        } catch (e) {
            log.error "graphql query randomUnread(userId=${userId}, skipIds=${skipIds}, count=${count}) failed: $e.message", e
            throw new RuntimeException("graphql query randomUnread(userId=${userId}, skipIds=${skipIds}, count=${count}) failed: $e.message", e)
        }
    }

    List<Article> getRankedUnread(String userId, String rankId, List<String> skipIds, Integer count) {
        log.info "receive graphql query rankedUnread(userId=${userId}, rankId=${rankId}, skipIds=${skipIds}, count=${count})"

        try {
            return articlesService.getRankedUserUnreadArticles(userId, rankId, skipIds, count)
        } catch (e) {
            log.error "graphql query rankedUnread(userId=${userId}, rankId=${rankId}, skipIds=${skipIds}, count=${count}) failed: $e.message", e
            throw new RuntimeException("graphql query rankedUnread(userId=${userId}, rankId=${rankId}, skipIds=${skipIds}, count=${count}) failed: $e.message", e)
        }
    }

    List<Article> getWithOldestCard(Integer count) {
        log.info "receive graphql query withOldestCard(count=${count})"

        try {
            return articlesService.findWithOldestCard(count)
        } catch (e) {
            log.error "graphql query withOldestCard(count=${count}) failed: $e.message", e
            throw new RuntimeException("graphql query withOldestCard(count=${count}) failed: $e.message", e)
        }
    }

    List<Article> getWithOldestRank(String rankId, Integer count) {
        log.info "receive graphql query withOldestRank(rankId=${rankId}, count=${count})"

        try {
            return articlesService.findWithOldestRank(rankId, count)
        } catch (e) {
            log.error "graphql query withOldestRank(rankId=${rankId}, count=${count}) failed: $e.message", e
            throw new RuntimeException("graphql query withOldestRank(rankId=${rankId}, count=${count}) failed: $e.message", e)
        }
    }

    List<Article> getWithEvents(Integer count) {
        log.info "receive graphql query getWithEvents(count=${count})"

        try {
            return articlesService.findWithEvents(count)
        } catch (e) {
            log.error "graphql query getWithEvents(count=${count}) failed: $e.message", e
            throw new RuntimeException("graphql query getWithEvents(count=${count}) failed: $e.message", e)
        }
    }
}
