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

    List<Article> getWithNonActualCard(Integer count) {
        log.info "receive graphql query withNonActualBasicCard(count=${count})"

        try {
            return articlesService.findWithNonActualCard(count)
        } catch (e) {
            log.error "graphql query withNonActualBasicCard(count=${count}) failed: $e.message", e
            throw new RuntimeException("graphql query withNonActualBasicCard(count=${count}) failed: $e.message", e)
        }
    }

    List<Article> getWithoutRank(String rankId, Integer count) {
        log.info "receive graphql query withoutRank(rankId=${rankId}, count=${count})"

        try {
            return articlesService.findWithoutRank(rankId, count)
        } catch (e) {
            log.error "graphql query withoutRank(rankId=${rankId}, count=${count}) failed: $e.message", e
            throw new RuntimeException("graphql query withoutRank(rankId=${rankId}, count=${count}) failed: $e.message", e)
        }
    }
}
