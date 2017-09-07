package me.potic.articles

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.Reporter
import com.codahale.metrics.Slf4jReporter
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics
import groovyx.net.http.HttpBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

import java.util.concurrent.TimeUnit

@EnableMetrics(proxyTargetClass = true)
@SpringBootApplication
class Application {

    static void main(String[] args) {
        SpringApplication.run(Application, args)
    }

    @Bean
    HttpBuilder pocketApiRest(@Value('${services.pockerApi.url}') String pocketApiServiceUrl) {
        HttpBuilder.configure {
            request.uri = pocketApiServiceUrl
        }
    }

    @Bean
    HttpBuilder auth0Rest(@Value('${services.auth0.url}') String auth0ServiceUrl) {
        HttpBuilder.configure {
            request.uri = auth0ServiceUrl
        }
    }

    @Bean
    Reporter slf4jMetricsReporter(MetricRegistry metricRegistry) {
        final Slf4jReporter reporter = Slf4jReporter.forRegistry(metricRegistry)
                .outputTo(LoggerFactory.getLogger('me.potic.articles.metrics'))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build()
        reporter.start(1, TimeUnit.MINUTES)

        return reporter
    }
}