package me.potic.articles.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.springframework.data.annotation.Id

@Builder
@EqualsAndHashCode
@ToString
class Article {

    @Id
    String id

    String userId

    PocketArticle fromPocket

    BasicCard basicCard
}
