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

    List<Article> getLatestUnread(String userId, List<String> skipIds, Integer count) {
        log.info "receive graphql query latestUnread(userId=${userId}, skipIds=${skipIds}, count=${count})"

        try {
            return articlesService.getLatestUserUnreadArticles(userId, skipIds, count)
        } catch (e) {
            log.error "graphql query latestUnread(userId=${userId}, skipIds=${skipIds}, count=${count}) failed: $e.message", e
            throw new RuntimeException("graphql query latestUnread(userId=${userId}, skipIds=${skipIds}, count=${count}) failed: $e.message", e)
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
}
