package me.potic.articles.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.time.LocalDateTime

@EqualsAndHashCode
@ToString(includeNames = true)
class ArticleEvent {

    String userId

    String articleId

    ArticleEventType type

    String timestamp
}
