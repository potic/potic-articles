package me.potic.articles.service

import com.codahale.metrics.annotation.Counted
import com.codahale.metrics.annotation.Timed
import com.google.common.base.Ticker
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import me.potic.articles.domain.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import java.util.concurrent.TimeUnit

@Service
@Slf4j
class UserService {

    HttpBuilder usersServiceRest

    LoadingCache<String, User> cachedUsers

    @Autowired
    HttpBuilder usersServiceRest(@Value('${services.users.url}') String usersServiceUrl) {
        usersServiceRest = HttpBuilder.configure {
            request.uri = usersServiceUrl
        }
    }

    @PostConstruct
    void initCachedUserIds() {
        cachedUsers(Ticker.systemTicker())
    }

    LoadingCache<String, User> cachedUsers(Ticker ticker) {
        cachedUsers = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .ticker(ticker)
                .build(
                        new CacheLoader<String, User>() {

                            @Override
                            User load(String auth0Token) {
                                fetchUserByAuth0Token(auth0Token)
                            }
                        }
                )
    }

    @Counted(name = 'findUserByAuth0Token.total')
    User findUserByAuth0Token(String auth0Token) {
        log.info 'finding user by auth0 token'

        try {
            return cachedUsers.get(auth0Token)
        } catch (e) {
            log.error "finding user by auth0 token failed: $e.message", e
            throw new RuntimeException('finding user by auth0 token failed', e)
        }
    }

    @Counted(name = 'findUserByAuth0Token.cacheMiss')
    @Timed(name = 'fetchUserByAuth0Token')
    User fetchUserByAuth0Token(String auth0Token) {
        log.info 'fetching user by auth0 token'

        try {
            def response = usersServiceRest.get {
                request.uri.path = '/user/me'
                request.headers['Authorization'] = 'Bearer ' + auth0Token
            }

            return new User(response)
        } catch (e) {
            log.error "fetching user by auth0 token failed: $e.message", e
            throw new RuntimeException('fetching user by auth0 token failed', e)
        }
    }
}
