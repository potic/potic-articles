package me.potic.articles.service

import com.codahale.metrics.annotation.Counted
import com.codahale.metrics.annotation.Timed
import com.google.common.base.Ticker
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import java.util.concurrent.TimeUnit

@Service
@Slf4j
class UserService {

    HttpBuilder usersServiceRest

    LoadingCache<String, String> cachedUserIds

    @Autowired
    HttpBuilder usersServiceRest(@Value('${services.users.url}') String usersServiceUrl) {
        usersServiceRest = HttpBuilder.configure {
            request.uri = usersServiceUrl
        }
    }

    @PostConstruct
    void initCachedUserIds() {
        cachedUserIds(Ticker.systemTicker())
    }

    LoadingCache<String, String> cachedUserIds(Ticker ticker) {
        cachedUserIds = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .ticker(ticker)
                .build(
                        new CacheLoader<String, String>() {

                            @Override
                            String load(String auth0Token) {
                                fetchUserIdByAuth0Token(auth0Token)
                            }
                        }
                )
    }

    @Counted(name = 'findUserIdByAuth0Token.total')
    String findUserIdByAuth0Token(String auth0Token) {
        log.info 'finding user id by auth0 token'

        try {
            return cachedUserIds.get(auth0Token)
        } catch (e) {
            log.error "finding user id by auth0 token failed: $e.message", e
            throw new RuntimeException('finding user id by auth0 token failed', e)
        }
    }

    @Counted(name = 'findUserIdByAuth0Token.cacheMiss')
    @Timed(name = 'fetchUserIdByAuth0Token')
    String fetchUserIdByAuth0Token(String auth0Token) {
        log.info 'fetching user id by auth0 token'

        try {
            def user = usersServiceRest.get {
                request.uri.path = '/user/me'
                request.headers['Authorization'] = 'Bearer ' + auth0Token
            }

            return user['id']
        } catch (e) {
            log.error "fetching user id by auth0 token failed: $e.message", e
            throw new RuntimeException('fetching user id by auth0 token failed', e)
        }
    }
}

