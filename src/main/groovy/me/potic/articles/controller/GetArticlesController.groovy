package me.potic.articles.controller

import groovy.util.logging.Slf4j
import me.potic.articles.domain.Article
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
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
    @ResponseBody Collection<Article> getUnreadByUserId(@PathVariable String userId, @RequestParam('offset') Integer offset, @RequestParam('limit') Integer limit) {
        mongoTemplate.find(query(where('userId').is(userId).and('read').is(false)).with(new Sort(Sort.Direction.DESC, 'timeAdded')).skip(offset).limit(limit), Article)
    }
}
