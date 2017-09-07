package me.potic.articles.service

import com.codahale.metrics.Counter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.annotation.Timed
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.util.concurrent.TimeUnit

import static com.codahale.metrics.MetricRegistry.name

@Service
@Slf4j
class UserService {

    @Autowired
    HttpBuilder auth0Rest

    Counter fetchPocketSquareIdTotalCount
    Counter fetchPocketSquareIdAuth0Count

    @Autowired
    void initMetrics(MetricRegistry metricRegistry) {
        fetchPocketSquareIdTotalCount = metricRegistry.counter(name(UserService, 'fetchPocketSquareIdByAuth0Token', 'count', 'total'))
        fetchPocketSquareIdAuth0Count = metricRegistry.counter(name(UserService, 'fetchPocketSquareIdByAuth0Token', 'count', 'auth0'))
    }

    LoadingCache<String, String> cachedPocketSquareId = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.DAYS)
            .build(
                new CacheLoader<String, String>() {

                    @Override
                    String load(String auth0Token) {
                        doAuth0RequestForPocketSquareId(auth0Token)
                    }
                }
            )

    @Timed(name = 'fetchPocketSquareIdByAuth0Token')
    String fetchPocketSquareIdByAuth0Token(String auth0Token) {
        log.info 'fetching user pocketSquareId by auth0 token'
        fetchPocketSquareIdTotalCount.inc()

        try {
            return cachedPocketSquareId.get(auth0Token)
        } catch (e) {
            log.error "fetching user pocketSquareId by auth0 token failed: $e.message", e
            throw e
        }
    }

    String doAuth0RequestForPocketSquareId(String auth0Token) {
        log.info 'performing auth0 request to get user pocketSquareId by auth0 token'
        fetchPocketSquareIdAuth0Count.inc()

        try {
            def authResult = auth0Rest.get {
                request.uri.path = '/userinfo'
                request.headers['Authorization'] = 'Bearer ' + auth0Token
            }

            return authResult['https://potic.me/pocketSquareId']
        } catch (e) {
            log.error "performing auth0 request to get user pocketSquareId by auth0 token failed: $e.message", e
            throw e
        }
    }
}

