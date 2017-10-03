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

    def 'String findUserIdByAuth0Token(String auth0Token)'(){
        setup: 'mock servers'
        ErsatzServer ersatz = new ErsatzServer()
        ersatz.expectations {
            get('/user/me') {
                called equalTo(1)
                header 'Authorization', equalTo('Bearer TEST_TOKEN_28')
                responder {
                    content '{"socialId":"google-oauth2|28","name":"Yaroslav Yermilov","id":"USER_ID_28"}','application/json'
                }
            }
        }
        ersatz.start()

        and: 'instruct service to use mock server'
        userService.usersServiceRest(ersatz.httpUrl)

        when: 'find userId by auth0 token'
        String actualUserId = userService.findUserIdByAuth0Token('TEST_TOKEN_28')

        then: 'expected token is returned'
        actualUserId == 'USER_ID_28'

        and: 'mock server received expected calls'
        ersatz.verify()

        cleanup: 'stop mock server'
        ersatz.stop()
    }

    def 'String findUserIdByAuth0Token(String auth0Token) - results are cached'(){
        setup: 'mock servers'
        ErsatzServer ersatz = new ErsatzServer()
        ersatz.expectations {
            get('/user/me') {
                called equalTo(1)
                header 'Authorization', equalTo('Bearer TEST_TOKEN_43')
                responder {
                    content '{"socialId":"google-oauth2|43","name":"Yaroslav Yermilov","id":"USER_ID_43"}','application/json'
                }
            }
        }
        ersatz.start()

        and: 'instruct service to use mock server'
        userService.usersServiceRest(ersatz.httpUrl)

        when: 'fetch userId by auth0 token first time'
        String actualUserId1 = userService.findUserIdByAuth0Token('TEST_TOKEN_43')

        then: 'expected token is returned'
        actualUserId1 == 'USER_ID_43'

        when: 'fetch userId by auth0 token second time'
        String actualUserId2 = userService.findUserIdByAuth0Token('TEST_TOKEN_43')

        then: 'expected token is returned'
        actualUserId2 == 'USER_ID_43'

        and: 'mock server received expected calls'
        ersatz.verify()

        cleanup: 'stop mock server'
        ersatz.stop()
    }

    def 'String findUserIdByAuth0Token(String auth0Token) - cached results are expiring'(){
        setup: 'mock servers'
        ErsatzServer ersatz = new ErsatzServer()
        ersatz.expectations {
            get('/user/me') {
                called equalTo(2)
                header 'Authorization', equalTo('Bearer TEST_TOKEN_17')
                responder {
                    content '{"socialId":"google-oauth2|17","name":"Yaroslav Yermilov","id":"USER_ID_17"}','application/json'
                }
            }
        }
        ersatz.start()

        and: 'instruct service to use mock server'
        userService.usersServiceRest(ersatz.httpUrl)

        and: 'instruct service to use mock time'
        Ticker ticker = Mock()
        ticker.read() >>> [ 0L, 0L, NANOSECONDS.convert(2, DAYS) ]
        userService.cachedUserIds(ticker)

        when: 'fetch userId by auth0 token first time'
        String actualUserId1 = userService.findUserIdByAuth0Token('TEST_TOKEN_17')

        then: 'expected token is returned'
        actualUserId1 == 'USER_ID_17'

        when: 'two days passed, fetch userId by auth0 token second time'
        String actualUserId2 = userService.findUserIdByAuth0Token('TEST_TOKEN_17')

        then: 'expected token is returned'
        actualUserId2 == 'USER_ID_17'

        and: 'mock server received expected calls'
        ersatz.verify()

        cleanup: 'stop mock server'
        ersatz.stop()
    }
}
