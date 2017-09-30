package me.potic.articles

import com.stehno.ersatz.ErsatzServer
import me.potic.articles.service.Auth0Service
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo

@SpringBootTest
@ActiveProfiles('integrationTest')
class Auth0ServiceTest extends Specification {

    @Autowired
    Auth0Service auth0Service

    def 'String getSocialId(String auth0Token)'(){
        setup: 'mock server instead of actual Auth0'
        ErsatzServer ersatz = new ErsatzServer()
        ersatz.expectations {
            get('/userinfo') {
                called equalTo(1)
                header 'Authorization', equalTo('Bearer TEST_TOKEN_28')
                responder {
                    content '{"sub":"google-oauth2|28","name":"Yaroslav Yermilov"}','application/json'
                }
            }
        }
        ersatz.start()

        and: 'instruct service to use mock server'
        auth0Service.auth0Rest(ersatz.httpUrl)

        when: 'get socialId by auth0 token'
        String actualSocialId = auth0Service.getSocialId('TEST_TOKEN_28')

        then: 'expected token is returned'
        actualSocialId == 'google-oauth2|28'

        and: 'mock server received expected calls'
        ersatz.verify()

        cleanup: 'stop mock server'
        ersatz.stop()
    }
}