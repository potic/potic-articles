package me.potic.articles.controller

import groovy.util.logging.Slf4j
import me.potic.articles.domain.Article
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import org.springframework.web.bind.annotation.*

import static org.springframework.data.mongodb.core.query.Criteria.where
import static org.springframework.data.mongodb.core.query.Query.query

@RestController
@Slf4j
class GetArticlesController {

    @Autowired
    MongoTemplate mongoTemplate

    @CrossOrigin
    @GetMapping(path = '/article/byUserId/{userId}/unread')
    @ResponseBody Collection<Article> getUnreadByUserId(
            @PathVariable String userId,
            @RequestParam('cursorId') String cursorId,
            @RequestParam('count') Integer count,
            @RequestParam(value = 'minLength', required = false) Integer minLength,
            @RequestParam(value = 'maxLength', required = false) Integer maxLength
    ) {
        Article cursorArticle = mongoTemplate.find(query(where('id').is(cursorId)), Article)

        CriteriaDefinition unreadQuery = where('userId').is(userId).and('read').is(false).and('timeAdded').lt(cursorArticle.timeAdded)
        if (minLength != null) {
            unreadQuery = unreadQuery.and('wordCount').gt(minLength)
        }
        if (maxLength != null) {
            unreadQuery = unreadQuery.and('wordCount').lte(maxLength)
        }

        mongoTemplate.find(
                query(unreadQuery).with(new Sort(Sort.Direction.DESC, 'timeAdded')).limit(count),
                Article
        )
    }
}
