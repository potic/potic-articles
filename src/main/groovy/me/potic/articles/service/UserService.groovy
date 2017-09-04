package me.potic.articles.service

import com.codahale.metrics.Counter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct
import java.util.concurrent.TimeUnit

import static com.codahale.metrics.MetricRegistry.name

@Service
@Slf4j
class UserService {

    @Autowired
    HttpBuilder auth0Rest

    @Autowired
    MetricRegistry metricRegistry

    Timer fetchPocketSquareIdByAuth0TokenTimer
    Counter fetchPocketSquareIdTotalRequests
    Counter fetchPocketSquareIdAuth0Requests

    @PostConstruct
    void initMetrics() {
        fetchPocketSquareIdByAuth0TokenTimer = metricRegistry.timer(name('service', 'user', 'fetchPocketSquareIdByAuth0Token'))
        fetchPocketSquareIdTotalRequests = metricRegistry.counter(name('service', 'user', 'fetchPocketSquareIdByAuth0Token', 'requests', 'total'))
        fetchPocketSquareIdAuth0Requests = metricRegistry.counter(name('service', 'user', 'fetchPocketSquareIdByAuth0Token', 'requests', 'auth0'))
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

    String fetchPocketSquareIdByAuth0Token(String auth0Token) {
        final Timer.Context timerContext = fetchPocketSquareIdByAuth0TokenTimer.time()
        log.info "fetching user pocketSquareId by auth0 token"
        fetchPocketSquareIdTotalRequests.inc()

        try {
            return cachedPocketSquareId.get(auth0Token)
        } finally {
            long time = timerContext.stop()
            log.info "fetching user pocketSquareId by auth0 token took ${time / 1_000_000}ms"
        }
    }

    String doAuth0RequestForPocketSquareId(String auth0Token) {
        log.info "performing auth0 request to get user pocketSquareId by auth0 token"
        fetchPocketSquareIdAuth0Requests.inc()

        def authResult = auth0Rest.get {
            request.uri.path = '/userinfo'
            request.headers['Authorization'] = 'Bearer ' + auth0Token
        }

        return authResult['https://potic.me/pocketSquareId']
    }
}

