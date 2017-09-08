package me.potic.articles

import com.stehno.ersatz.ErsatzServer
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

import static org.hamcrest.Matchers.equalTo

@RunWith(SpringRunner)
@SpringBootTest
class UserServiceIntegrationTest {

    @Test
    void 'say hello'(){
        // setup
        ErsatzServer ersatz = new ErsatzServer()

        ersatz.expectations {
            get('/say/hello'){
                called equalTo(1)
                query 'name','Ersatz'
                responder {
                    content 'Hello Ersatz','text/plain'
                }
            }
        }

        ersatz.start()

        // when
        String result = "${ersatz.httpUrl}/say/hello?name=Ersatz".toURL().text

        // then
        println result
        assert result == 'HelloErsatz'

        ersatz.verify()

        // cleanup
        ersatz.stop()
    }
}
