package me.potic.articles.service

import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@Slf4j
class UserService {

    @Autowired
    HttpBuilder auth0Rest

    String fetchPocketSquareIdByAuth0Token(String auth0Token) {
        def authResult = auth0Rest.get {
            request.uri.path = '/userinfo'
            request.headers['Authorization'] = 'Bearer ' + auth0Token
        }

        return authResult['https://potic.me/pocketSquareId']
    }
}

