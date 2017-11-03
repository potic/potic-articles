package me.potic.articles.query

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import groovy.util.logging.Slf4j
import me.potic.articles.domain.Article
import me.potic.articles.domain.PocketArticle
import me.potic.articles.domain.User
import me.potic.articles.service.ArticlesService
import me.potic.articles.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
class ArticleMutation implements GraphQLMutationResolver {

    @Autowired
    ArticlesService articlesService

    @Autowired
    UserService userService

    Article markAsRead(String userId, String articleId) {
        log.info "receive graphql mutation markAsRead(userId=${userId}, articleId=${articleId})"

        try {
            User user = userService.findUserById(userId)
            return articlesService.markArticleAsRead(user, articleId)
        } catch (e) {
            log.error "graphql mutation markAsRead(userId=${userId}, articleId=${articleId}) failed: $e.message", e
            throw new RuntimeException("graphql mutation markAsRead(userId=${userId}, articleId=${articleId}) failed: $e.message", e)
        }
    }

    Article upsertFromPocket(String userId, PocketArticle fromPocket) {
        log.info "receive graphql mutation upsertFromPocket(userId=${userId}, fromPocket=${fromPocket})"

        try {
            return articlesService.upsertFromPocket(userId, fromPocket)
        } catch (e) {
            log.error "graphql mutation upsertFromPocket(userId=${userId}, fromPocket=${fromPocket}) failed: $e.message", e
            throw new RuntimeException("graphql mutation upsertFromPocket(userId=${userId}, fromPocket=${fromPocket}) failed: $e.message", e)
        }
    }

    Article update(Article article) {
        log.info "receive graphql mutation update(article=${article})"

        try {
            return articlesService.updateArticle(article)
        } catch (e) {
            log.error "graphql mutation update(article=${article}) failed: $e.message", e
            throw new RuntimeException("graphql mutation update(article=${article}) failed: $e.message", e)
        }
    }
}
