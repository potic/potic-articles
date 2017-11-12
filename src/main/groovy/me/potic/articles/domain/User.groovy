package me.potic.articles.domain

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.springframework.data.annotation.Id

@ToString(includes = [ 'id' ])
@EqualsAndHashCode
class User {

    @Id
    String id

    Collection<String> socialIds

    String pocketAccessToken
}
