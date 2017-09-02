package me.potic.articles.service

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Timer
import groovy.util.logging.Slf4j
import groovyx.net.http.HttpBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

import static com.codahale.metrics.MetricRegistry.name

@Service
@Slf4j
class UserService {

    @Autowired
    HttpBuilder auth0Rest

    @Autowired
    MetricRegistry metricRegistry

    Timer fetchPocketSquareIdByAuth0Token

    @PostConstruct
    void initMetrics() {
        fetchPocketSquareIdByAuth0Token = metricRegistry.timer(name('service', 'user', 'fetchPocketSquareIdByAuth0Token'))
    }

    String fetchPocketSquareIdByAuth0Token(String auth0Token) {
        final Timer.Context timerContext = fetchPocketSquareIdByAuth0Token.time()
        log.info "fetching user pocketSquareId by auth0 token"

        try {
            def authResult = auth0Rest.get {
                request.uri.path = '/userinfo'
                request.headers['Authorization'] = 'Bearer ' + auth0Token
            }

            return authResult['https://potic.me/pocketSquareId']
        } finally {
            long time = timerContext.stop()
            log.info "fetching user pocketSquareId by auth0 token took ${time / 1_000_000}ms"
        }
    }
}

