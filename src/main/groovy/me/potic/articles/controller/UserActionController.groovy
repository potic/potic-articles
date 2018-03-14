package me.potic.articles.controller

import groovy.util.logging.Slf4j
import me.potic.articles.domain.Article
import me.potic.articles.domain.User
import me.potic.articles.service.ArticlesService
import me.potic.articles.service.FeedbackService
import me.potic.articles.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

import java.security.Principal

@RestController
@Slf4j
class UserActionController {

    @Autowired
    ArticlesService articlesService

    @Autowired
    UserService userService

    @Autowired
    FeedbackService feedbackService

    @CrossOrigin
    @PostMapping(path = '/user/me/article/{articleId}/like')
    @ResponseBody ResponseEntity<Void> likeArticle(@PathVariable String articleId, final Principal principal, @RequestBody(required = false) Map likeArticleRequest) {
        log.info "receive POST request for /user/me/$articleId/like"

        try {
            User user = userService.findUserByAuth0Token(principal.token)
            Article article = articlesService.markArticleAsRead(user, articleId)

            feedbackService.liked(user, article)

            if (likeArticleRequest?.skipIds != null) {
                List<Integer> skipIds = likeArticleRequest.skipIds
                skipIds.takeWhile({ skipId -> skipId != articleId }).forEach({ skipId -> feedbackService.skipped(user, skipId) })
            }

            return new ResponseEntity<>(HttpStatus.OK)
        } catch (e) {
            log.error "POST request for /user/me/$articleId/like failed: $e.message", e
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @CrossOrigin
    @PostMapping(path = '/user/me/article/{articleId}/dislike')
    @ResponseBody ResponseEntity<Void> dislikeArticle(@PathVariable String articleId, final Principal principal) {
        log.info "receive POST request for /user/me/$articleId/dislike"

        try {
            User user = userService.findUserByAuth0Token(principal.token)
            Article article = articlesService.markArticleAsRead(user, articleId)
            feedbackService.disliked(user, article)
            return new ResponseEntity<>(HttpStatus.OK)
        } catch (e) {
            log.error "POST request for /user/me/$articleId/dislike failed: $e.message", e
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}
