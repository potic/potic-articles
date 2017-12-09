package me.potic.articles

import com.stehno.ersatz.ErsatzServer
import me.potic.articles.domain.User
import me.potic.articles.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

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
}