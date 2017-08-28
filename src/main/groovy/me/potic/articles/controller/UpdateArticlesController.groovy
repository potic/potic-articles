package me.potic.articles.controller

import groovy.util.logging.Slf4j
import me.potic.articles.domain.Article
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

import static org.springframework.data.mongodb.core.query.Criteria.where
import static org.springframework.data.mongodb.core.query.Query.query
import static org.springframework.data.mongodb.core.query.Update.update

@RestController
@Slf4j
class UpdateArticlesController {

    @Autowired
    MongoTemplate mongoTemplate

    @CrossOrigin
    @PostMapping(path = '/article/{id}/markAsRead')
    void markArticleAsReady(@PathVariable String id) {
        mongoTemplate.updateFirst(query(where(id).is(id)), update('read', true), Article)

        // TODO: make a call to Pocket API
    }
}
