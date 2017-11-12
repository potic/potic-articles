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
                                fetchUserById(auth0Token)
                            }
                        }
                )
    }

    @Counted(name = 'findUserById.total')
    User findUserById(String id) {
        log.info "finding user with id=${id}..."

        try {
            return cachedUsers.get(id)
        } catch (e) {
            log.error "finding user with id=${id} failed: $e.message", e
            throw new RuntimeException("finding user with id=${id} failed", e)
        }
    }

    @Counted(name = 'fetchUserById.cacheMiss')
    @Timed(name = 'fetchUserById')
    User fetchUserById(String id) {
        log.info "fetching user with id=${id}..."

        try {
            def response = usersServiceRest.get {
                request.uri.path = "/user/${id}"
            }

            return new User(response)
        } catch (e) {
            log.error "fetching user with id=${id} failed: $e.message", e
            throw new RuntimeException("fetching user with id=${id} failed", e)
        }
    }
}

