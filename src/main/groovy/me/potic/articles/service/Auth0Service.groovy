package me.potic.articles.service

import com.codahale.metrics.annotation.Timed
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
@Slf4j
class Auth0Service {

    HttpBuilder auth0Rest

    @Autowired
    HttpBuilder auth0Rest(@Value('${services.auth0.url}') String auth0ServiceUrl) {
        auth0Rest = HttpBuilder.configure {
            request.uri = auth0ServiceUrl
        }
    }

    @Timed(name = 'getSocialId')
    String getSocialId(String auth0Token) {
        try {
            log.info 'performing auth0 request to get user socialId by auth0 token'

            def authResult = auth0Rest.get {
                request.uri.path = '/userinfo'
                request.headers['Authorization'] = 'Bearer ' + auth0Token
            }

            return authResult['sub']
        } catch (e) {
            log.error "performing auth0 request to get user socialId by auth0 token failed: $e.message", e
            throw new RuntimeException('performing auth0 request to get user socialId by auth0 token failed', e)
        }
    }
}
