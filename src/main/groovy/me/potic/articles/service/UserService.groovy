package me.potic.articles.service

import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.articles.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import static me.potic.articles.utils.Utils.maskForLog

@Service
@Slf4j
class UserService {

    HttpBuilder usersServiceRest

    @Autowired
    HttpBuilder usersServiceRest(@Value('${services.users.url}') String usersServiceUrl) {
        usersServiceRest = HttpBuilder.configure {
            request.uri = usersServiceUrl
        }
    }

    User findUserByAuth0Token(String auth0Token) {
        log.debug "finding user by auth0 token ${maskForLog(auth0Token)}"

        try {
            def response = usersServiceRest.get {
                request.uri.path = '/user/me'
                request.headers['Authorization'] = 'Bearer ' + auth0Token
            }

            return new User(response)
        } catch (e) {
            log.error "finding user by auth0 token ${maskForLog(auth0Token)} failed: $e.message", e
            throw new RuntimeException("finding user by auth0 token ${maskForLog(auth0Token)} failed", e)
        }
    }
}
