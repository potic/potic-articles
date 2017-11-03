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

    List<Article> getUnread(String userId, String cursorId, Integer count, Integer minLength, Integer maxLength) {
        log.info "receive graphql query unread(userId=${userId}, cursorId=${cursorId}, count=${count}, minLength=${minLength}, maxLength=${maxLength})"

        try {
            return articlesService.getUserUnreadArticles(userId, cursorId, count, minLength, maxLength)
        } catch (e) {
            log.error "graphql query unread(userId=${userId}, cursorId=${cursorId}, count=${count}, minLength=${minLength}, maxLength=${maxLength}) failed: $e.message", e
            throw new RuntimeException("graphql query unread(userId=${userId}, cursorId=${cursorId}, count=${count}, minLength=${minLength}, maxLength=${maxLength}) failed: $e.message", e)
        }
    }

    List<Article> getWithNonActualBasicCard(Integer count) {
        log.info "receive graphql query withNonActualBasicCard(count=${count})"

        try {
            return articlesService.findWithNonActualBasicCard(count)
        } catch (e) {
            log.error "graphql query withNonActualBasicCard(count=${count}) failed: $e.message", e
            throw new RuntimeException("graphql query withNonActualBasicCard(count=${count}) failed: $e.message", e)
        }
    }
}
