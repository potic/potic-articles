package me.potic.articles

import groovyx.net.http.HttpBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

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
}