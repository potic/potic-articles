package me.potic.articles.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true)
class Card {

    String id

    String pocketId

    boolean actual

    String url

    String title

    String source

    String excerpt

    CardImage image
}
