package me.potic.articles.controller

import groovy.util.logging.Slf4j
import me.potic.articles.domain.Article
import me.potic.articles.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import org.springframework.web.bind.annotation.*

import java.security.Principal

import static org.springframework.data.mongodb.core.query.Criteria.where
import static org.springframework.data.mongodb.core.query.Query.query

@RestController
@Slf4j
class GetArticlesController {

    @Autowired
    MongoTemplate mongoTemplate

    @Autowired
    UserService userService

    @CrossOrigin
    @GetMapping(path = '/user/me/article/unread')
    @ResponseBody Collection<Article> userUnreadArticles(
            @RequestParam(value = 'cursorId', required = false) String cursorId,
            @RequestParam(value = 'count') Integer count,
            @RequestParam(value = 'minLength', required = false) Integer minLength,
            @RequestParam(value = 'maxLength', required = false) Integer maxLength,
            final Principal principal
    ) {
        String pocketSquareUserId = userService.fetchPocketSquareIdByAuth0Token(principal.token)

        log.info "request to get unread articles for user $pocketSquareUserId"

        CriteriaDefinition unreadQuery
        if (cursorId == null) {
            unreadQuery = where('userId').is(pocketSquareUserId).and('read').is(false)
        } else {
            Article cursorArticle = mongoTemplate.find(query(where('id').is(cursorId)), Article).first()
            unreadQuery = where('userId').is(pocketSquareUserId).and('read').is(false).and('timeAdded').lt(cursorArticle.timeAdded)
        }

        if (minLength != null) {
            unreadQuery = unreadQuery.and('wordCount').gt(minLength)
        }
        if (maxLength != null) {
            unreadQuery = unreadQuery.and('wordCount').lte(maxLength)
        }

        return mongoTemplate.find(
                query(unreadQuery).with(new Sort(Sort.Direction.DESC, 'timeAdded')).limit(count),
                Article
        )
    }
}
