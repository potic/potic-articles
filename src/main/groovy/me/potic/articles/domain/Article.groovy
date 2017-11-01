package me.potic.articles.domain

import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.springframework.data.annotation.Id

@Builder
@ToString
class Article {

    @Id
    String id

    String userId

    PocketArticle fromPocket

    BasicCard basicCard
}
