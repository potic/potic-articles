package me.potic.articles.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames = true)
class Rank {

    String id

    double value

    String timestamp
}
