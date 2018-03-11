package me.potic.articles.controller

import groovy.util.logging.Slf4j
import me.potic.articles.domain.ArticleEvent
import me.potic.articles.domain.Card
import me.potic.articles.domain.PocketArticle
import me.potic.articles.domain.Rank
import me.potic.articles.service.ArticlesService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@Slf4j
class UpdateArticlesController {

    @Autowired
    ArticlesService articlesService

    @PostMapping(path = '/user/{userId}/article/fromPocket')
    @ResponseBody ResponseEntity<Void> upsertFromPocket(@PathVariable String userId, @RequestBody PocketArticle articleFromPocket) {
        log.info "receive POST request for /user/${userId}/article/fromPocket; BODY=${articleFromPocket}"

        try {
            articlesService.upsertFromPocket(userId, articleFromPocket)
            return new ResponseEntity<>(HttpStatus.OK)
        } catch (e) {
            log.error "POST request for /user/${userId}/article/fromPocket; BODY=${articleFromPocket} failed: $e.message", e
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping(path = '/article/{articleId}/event')
    @ResponseBody ResponseEntity<Void> addEventToArticle(@PathVariable String articleId, @RequestBody ArticleEvent articleEvent) {
        log.info "receive POST request for /article/${articleId}/event; body=${articleEvent}"

        try {
            articlesService.addEventToArticle(articleId, articleEvent)
            return new ResponseEntity<>(HttpStatus.OK)
        } catch (e) {
            log.error "POST request for /article/${articleId}/event; body=${articleEvent} failed: $e.message", e
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping(path = '/article/{articleId}/card')
    @ResponseBody ResponseEntity<Void> updateArticleCard(@PathVariable String articleId, @RequestBody Card card) {
        log.info "receive POST request for /article/${articleId}/card; body=${card}"

        try {
            articlesService.updateArticleCard(articleId, card)
            return new ResponseEntity<>(HttpStatus.OK)
        } catch (e) {
            log.error "POST request for /article/${articleId}/card; body=${card} failed: $e.message", e
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @PostMapping(path = '/article/{articleId}/rank')
    @ResponseBody ResponseEntity<Void> addRankToArticle(@PathVariable String articleId, @RequestBody Rank rank) {
        log.info "receive POST request for /article/${articleId}/rank; body=${rank}"

        try {
            articlesService.addRankToArticle(articleId, rank)
            return new ResponseEntity<>(HttpStatus.OK)
        } catch (e) {
            log.error "POST request for /article/${articleId}/rank; body=${rank} failed: $e.message", e
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}