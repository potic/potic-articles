package me.potic.articles

import com.google.common.base.Ticker
import com.stehno.ersatz.ErsatzServer
import me.potic.articles.domain.User
import me.potic.articles.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import static java.util.concurrent.TimeUnit.DAYS
import static java.util.concurrent.TimeUnit.NANOSECONDS
import static org.hamcrest.Matchers.equalTo

@SpringBootTest
@ActiveProfiles('integrationTest')
class UserServiceIntegrationTest extends Specification {

    @Autowired
    UserService userService

    def 'User findUserByAuth0Token(String auth0Token)'(){
        setup: 'mock servers'
        ErsatzServer ersatz = new ErsatzServer()
        ersatz.expectations {
            get('/user/me') {
                called equalTo(1)
                header 'Authorization', equalTo('Bearer TEST_TOKEN_28')
                responder {
                    content '{ "id": "USER_ID_28", "socialIds": [ "google-oauth2|28" ], "pocketAccessToken": "POCKET_TOKEN_28" }','application/json'
                }
            }
        }
        ersatz.start()

        and: 'instruct service to use mock server'
        userService.usersServiceRest(ersatz.httpUrl)

        when: 'find userId by auth0 token'
        User actualUser = userService.findUserByAuth0Token('TEST_TOKEN_28')

        then: 'expected token is returned'
        actualUser.id == 'USER_ID_28'
        actualUser.socialIds == [ "google-oauth2|28" ]
        actualUser.pocketAccessToken == "POCKET_TOKEN_28"

        and: 'mock server received expected calls'
        ersatz.verify()

        cleanup: 'stop mock server'
        ersatz.stop()
    }

    def 'User findUserByAuth0Token(String auth0Token) - results are cached'(){
        setup: 'mock servers'
        ErsatzServer ersatz = new ErsatzServer()
        ersatz.expectations {
            get('/user/me') {
                called equalTo(1)
                header 'Authorization', equalTo('Bearer TEST_TOKEN_43')
                responder {
                    content '{ "id": "USER_ID_43", "socialIds": [ "google-oauth2|43" ], "pocketAccessToken": "POCKET_TOKEN_43" }','application/json'
                }
            }
        }
        ersatz.start()

        and: 'instruct service to use mock server'
        userService.usersServiceRest(ersatz.httpUrl)

        when: 'fetch userId by auth0 token first time'
        User actualUser1 = userService.findUserByAuth0Token('TEST_TOKEN_43')

        then: 'expected token is returned'
        actualUser1.id == 'USER_ID_43'
        actualUser1.socialIds == [ "google-oauth2|43" ]
        actualUser1.pocketAccessToken == "POCKET_TOKEN_43"

        when: 'fetch userId by auth0 token second time'
        User actualUser2 = userService.findUserByAuth0Token('TEST_TOKEN_43')

        then: 'expected token is returned'
        actualUser2.id == 'USER_ID_43'
        actualUser2.socialIds == [ "google-oauth2|43" ]
        actualUser2.pocketAccessToken == "POCKET_TOKEN_43"

        and: 'mock server received expected calls'
        ersatz.verify()

        cleanup: 'stop mock server'
        ersatz.stop()
    }

    def 'User findUserByAuth0Token(String auth0Token) - cached results are expiring'(){
        setup: 'mock servers'
        ErsatzServer ersatz = new ErsatzServer()
        ersatz.expectations {
            get('/user/me') {
                called equalTo(2)
                header 'Authorization', equalTo('Bearer TEST_TOKEN_17')
                responder {
                    content '{ "id": "USER_ID_17", "socialIds": [ "google-oauth2|17" ], "pocketAccessToken": "POCKET_TOKEN_17" }','application/json'
                }
            }
        }
        ersatz.start()

        and: 'instruct service to use mock server'
        userService.usersServiceRest(ersatz.httpUrl)

        and: 'instruct service to use mock time'
        Ticker ticker = Mock()
        ticker.read() >>> [ 0L, 0L, NANOSECONDS.convert(2, DAYS) ]
        userService.cachedUsers(ticker)

        when: 'fetch userId by auth0 token first time'
        User actualUser1 = userService.findUserByAuth0Token('TEST_TOKEN_17')

        then: 'expected token is returned'
        actualUser1.id == 'USER_ID_17'
        actualUser1.socialIds == [ "google-oauth2|17" ]
        actualUser1.pocketAccessToken == "POCKET_TOKEN_17"

        when: 'two days passed, fetch userId by auth0 token second time'
        User actualUser2 = userService.findUserByAuth0Token('TEST_TOKEN_17')

        then: 'expected token is returned'
        actualUser2.id == 'USER_ID_17'
        actualUser2.socialIds == [ "google-oauth2|17" ]
        actualUser2.pocketAccessToken == "POCKET_TOKEN_17"

        and: 'mock server received expected calls'
        ersatz.verify()

        cleanup: 'stop mock server'
        ersatz.stop()
    }
}