package me.potic.articles.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true)
class Card {

    String id

    Long timestamp

    String pocketId

    String url

    String title

    String source

    String excerpt

    Long addedTimestamp

    CardImage image
}
