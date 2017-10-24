package me.potic.articles.domain

import groovy.transform.builder.Builder
import org.springframework.data.annotation.Id

@Builder
class Article {

    @Id
    String id

    String userId

    Map<String, Object> fromPocket

    Map<String, Object> basicCard
}
