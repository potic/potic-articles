package me.potic.articles.service

import com.codahale.metrics.Counter
import com.codahale.metrics.MetricRegistry
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

import static com.codahale.metrics.MetricRegistry.name

@Service
@Slf4j
class UserService {

    HttpBuilder auth0Rest

    LoadingCache<String, String> cachedPocketSquareId

    Counter fetchPocketSquareIdTotalCount
    Counter fetchPocketSquareIdAuth0Count

    @Autowired
    void initMetrics(MetricRegistry metricRegistry) {
        fetchPocketSquareIdTotalCount = metricRegistry.counter(name(UserService, 'fetchPocketSquareIdByAuth0Token', 'count', 'total'))
        fetchPocketSquareIdAuth0Count = metricRegistry.counter(name(UserService, 'fetchPocketSquareIdByAuth0Token', 'count', 'auth0'))
    }

    @Autowired
    HttpBuilder auth0Rest(@Value('${services.auth0.url}') String auth0ServiceUrl) {
        auth0Rest = HttpBuilder.configure {
            request.uri = auth0ServiceUrl
        }
    }

    @PostConstruct
    void initCachedPocketSquareId() {
        cachedPocketSquareId(Ticker.systemTicker())
    }

    LoadingCache<String, String> cachedPocketSquareId(Ticker ticker) {
        cachedPocketSquareId = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.DAYS)
                .ticker(ticker)
                .build(
                        new CacheLoader<String, String>() {

                            @Override
                            String load(String auth0Token) {
                                doAuth0RequestForPocketSquareId(auth0Token)
                            }
                        }
                )
    }

    String fetchPocketSquareIdByAuth0Token(String auth0Token) {
        log.info 'fetching user pocketSquareId by auth0 token'
        fetchPocketSquareIdTotalCount.inc()

        try {
            return cachedPocketSquareId.get(auth0Token)
        } catch (e) {
            log.error "fetching user pocketSquareId by auth0 token failed: $e.message", e
            throw new RuntimeException('fetching user pocketSquareId by auth0 token failed', e)
        }
    }

    @Timed(name = 'doAuth0RequestForPocketSquareId')
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
            throw new RuntimeException('performing auth0 request to get user pocketSquareId by auth0 token failed', e)
        }
    }
}

