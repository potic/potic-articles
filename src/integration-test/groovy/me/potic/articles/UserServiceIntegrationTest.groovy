package me.potic.articles

import com.google.common.base.Ticker
import com.stehno.ersatz.ErsatzServer
import me.potic.articles.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo
import static java.util.concurrent.TimeUnit.*

@SpringBootTest
@ActiveProfiles('integrationTest')
class UserServiceIntegrationTest extends Specification {

    @Autowired
    UserService userService

    def 'String fetchPocketSquareIdByAuth0Token(String auth0Token)'(){
        setup: 'mock server instead of actual Auth0'
        ErsatzServer ersatz = new ErsatzServer()
        ersatz.expectations {
            get('/userinfo') {
                called equalTo(1)
                header 'Authorization', equalTo('Bearer TEST_TOKEN_28')
                responder {
                    content '{"sub":"google-oauth2","name":"Yaroslav Yermilov","https://potic.me/pocketSquareId":"POCKET_SQUARE_ID_28"}','application/json'
                }
            }
        }
        ersatz.start()

        and: 'instruct service to use mock server'
        userService.auth0Rest(ersatz.httpUrl)

        when: 'fetch pocketSquareId by auth0 token'
        String actualPocketSquareId = userService.fetchPocketSquareIdByAuth0Token('TEST_TOKEN_28')

        then: 'expected token is returned'
        actualPocketSquareId == 'POCKET_SQUARE_ID_28'

        and: 'mock server received expected calls'
        ersatz.verify()

        cleanup: 'stop mock server'
        ersatz.stop()
    }

    def 'String fetchPocketSquareIdByAuth0Token(String auth0Token) - results are cached'(){
        setup: 'mock server instead of actual Auth0'
        ErsatzServer ersatz = new ErsatzServer()
        ersatz.expectations {
            get('/userinfo') {
                called equalTo(1)
                header 'Authorization', equalTo('Bearer TEST_TOKEN_43')
                responder {
                    content '{"sub":"google-oauth2","name":"Yaroslav Yermilov","https://potic.me/pocketSquareId":"POCKET_SQUARE_ID_43"}','application/json'
                }
            }
        }
        ersatz.start()

        and: 'instruct service to use mock server'
        userService.auth0Rest(ersatz.httpUrl)

        when: 'fetch pocketSquareId by auth0 token first time'
        String actualPocketSquareId1 = userService.fetchPocketSquareIdByAuth0Token('TEST_TOKEN_43')

        then: 'expected token is returned'
        actualPocketSquareId1 == 'POCKET_SQUARE_ID_43'

        when: 'fetch pocketSquareId by auth0 token second time'
        String actualPocketSquareId2 = userService.fetchPocketSquareIdByAuth0Token('TEST_TOKEN_43')

        then: 'expected token is returned'
        actualPocketSquareId2 == 'POCKET_SQUARE_ID_43'

        and: 'mock server received expected calls'
        ersatz.verify()

        cleanup: 'stop mock server'
        ersatz.stop()
    }

    def 'String fetchPocketSquareIdByAuth0Token(String auth0Token) - cached results are expiring'(){
        setup: 'mock server instead of actual Auth0'
        ErsatzServer ersatz = new ErsatzServer()
        ersatz.expectations {
            get('/userinfo') {
                called equalTo(2)
                header 'Authorization', equalTo('Bearer TEST_TOKEN_17')
                responder {
                    content '{"sub":"google-oauth2","name":"Yaroslav Yermilov","https://potic.me/pocketSquareId":"POCKET_SQUARE_ID_17"}','application/json'
                }
            }
        }
        ersatz.start()

        and: 'instruct service to use mock server'
        userService.auth0Rest(ersatz.httpUrl)

        and: 'instruct service to use mock time'
        Ticker ticker = Mock()
        ticker.read() >>> [ 0L, 0L, NANOSECONDS.convert(2, DAYS) ]
        userService.cachedPocketSquareId(ticker)

        when: 'fetch pocketSquareId by auth0 token first time'
        String actualPocketSquareId1 = userService.fetchPocketSquareIdByAuth0Token('TEST_TOKEN_17')

        then: 'expected token is returned'
        actualPocketSquareId1 == 'POCKET_SQUARE_ID_17'

        when: 'two days passed, fetch pocketSquareId by auth0 token second time'
        String actualPocketSquareId2 = userService.fetchPocketSquareIdByAuth0Token('TEST_TOKEN_17')

        then: 'expected token is returned'
        actualPocketSquareId2 == 'POCKET_SQUARE_ID_17'

        and: 'mock server received expected calls'
        ersatz.verify()

        cleanup: 'stop mock server'
        ersatz.stop()
    }
}
