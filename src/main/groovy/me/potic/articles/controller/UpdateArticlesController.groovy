package me.potic.articles.controller

import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.articles.domain.Article
import me.potic.articles.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

import java.security.Principal

import static org.springframework.data.mongodb.core.query.Criteria.where
import static org.springframework.data.mongodb.core.query.Query.query
import static org.springframework.data.mongodb.core.query.Update.update

@RestController
@Slf4j
class UpdateArticlesController {

    @Autowired
    MongoTemplate mongoTemplate

    @Autowired
    UserService userService

    @Autowired
    HttpBuilder pocketApiRest

    @CrossOrigin
    @PostMapping(path = '/user/me/article/{articleId}/markAsRead')
    void markArticleAsReady(@PathVariable String articleId, final Principal principal) {
        String pocketSquareUserId = userService.fetchPocketSquareIdByAuth0Token(principal.token)
        log.info "request to mark article $articleId as read for user $pocketSquareUserId"

        Article readArticle = mongoTemplate.find(query(where('id').is(articleId)), Article).first()
        pocketApiRest.post {
            request.uri.path = "/archive/$pocketSquareUserId/${readArticle.pocketId}"
        }

        mongoTemplate.updateFirst(query(where('id').is(articleId)), update('read', true), Article)
    }
}
