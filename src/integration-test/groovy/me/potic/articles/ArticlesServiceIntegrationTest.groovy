package me.potic.articles

import com.mongodb.Mongo
import com.stehno.ersatz.ErsatzServer
import me.potic.articles.domain.Article
import me.potic.articles.domain.User
import me.potic.articles.service.ArticlesService
import org.junit.Rule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.GenericContainer
import spock.lang.IgnoreIf
import spock.lang.Specification

import static org.hamcrest.Matchers.equalTo
import static org.springframework.data.mongodb.core.query.Criteria.where
import static org.springframework.data.mongodb.core.query.Query.query

@IgnoreIf({ os.windows })
@SpringBootTest
@ActiveProfiles('integrationTest')
class ArticlesServiceIntegrationTest extends Specification {

    @Autowired
    ArticlesService articlesService

    @Value(value = '${mongodb.databaseName}')
    String databaseName

    @Rule
    GenericContainer mongodb = new GenericContainer('mongo:3.4.1').withExposedPorts(27017)

    MongoTemplate mongoTemplate

    def setup() {
        mongoTemplate = new MongoTemplate(new Mongo(mongodb.containerIpAddress, mongodb.getMappedPort(27017)), databaseName)
        articlesService.mongoTemplate = mongoTemplate

        List articles = [
                Article.builder().id('TEST_ARTICLE_1').userId('TEST_USER_1').basicCard([ actual: true ]).fromPocket([ item_id: 'POCKET_1', resolved_title: 'TITLE_1', read: '0', word_count: 100, time_added: 1 ]).build(),
                Article.builder().id('TEST_ARTICLE_2').userId('TEST_USER_1').basicCard([ actual: true ]).fromPocket([ item_id: 'POCKET_2', resolved_title: 'TITLE_2', read: '0', word_count: 200, time_added: 2 ]).build(),
                Article.builder().id('TEST_ARTICLE_3').userId('TEST_USER_1').basicCard([ actual: true ]).fromPocket([ item_id: 'POCKET_3', resolved_title: 'TITLE_3', read: '0', word_count: 300, time_added: 3 ]).build(),
                Article.builder().id('TEST_ARTICLE_4').userId('TEST_USER_1').basicCard([ actual: true ]).fromPocket([ item_id: 'POCKET_4', resolved_title: 'TITLE_4', read: '1', word_count: 100, time_added: 4 ]).build(),
                Article.builder().id('TEST_ARTICLE_5').userId('TEST_USER_2').basicCard([ actual: true ]).fromPocket([ item_id: 'POCKET_5', resolved_title: 'TITLE_5', read: '0', word_count: 200, time_added: 5 ]).build(),
                Article.builder().id('TEST_ARTICLE_6').userId('TEST_USER_2').basicCard([ actual: true ]).fromPocket([ item_id: 'POCKET_6', resolved_title: 'TITLE_6', read: '1', word_count: 100, time_added: 6 ]).build(),

                // same articles but non-actual
                Article.builder().id('TEST_ARTICLE_01').userId('TEST_USER_1').basicCard([ actual: false ]).fromPocket([ item_id: 'POCKET_1', read: '0', word_count: 100, time_added: 1 ]).build(),
                Article.builder().id('TEST_ARTICLE_02').userId('TEST_USER_1').basicCard([ actual: false ]).fromPocket([ item_id: 'POCKET_2', read: '0', word_count: 200, time_added: 2 ]).build(),
                Article.builder().id('TEST_ARTICLE_03').userId('TEST_USER_1').basicCard([ actual: false ]).fromPocket([ item_id: 'POCKET_3', read: '0', word_count: 300, time_added: 3 ]).build(),
                Article.builder().id('TEST_ARTICLE_04').userId('TEST_USER_1').basicCard([ actual: false ]).fromPocket([ item_id: 'POCKET_4', read: '1', word_count: 100, time_added: 4 ]).build(),
                Article.builder().id('TEST_ARTICLE_05').userId('TEST_USER_2').basicCard([ actual: false ]).fromPocket([ item_id: 'POCKET_5', read: '0', word_count: 200, time_added: 5 ]).build(),
                Article.builder().id('TEST_ARTICLE_06').userId('TEST_USER_2').basicCard([ actual: false ]).fromPocket([ item_id: 'POCKET_6', read: '1', word_count: 100, time_added: 6 ]).build()
        ]
        mongoTemplate.insert(articles, Article)
    }

    def 'List<Article> getUserUnreadArticles(User user, cursorId = null, Integer count, minLength = null, maxLength = null)'() {
        when:
        User user1 = new User(id: 'TEST_USER_1')
        List<Article> articles = articlesService.getUserUnreadArticles(user1, null, 10, null, null)

        then:
        articles.size() == 3

        with(articles[0]) {
            id == 'TEST_ARTICLE_3'
            userId == 'TEST_USER_1'
            basicCard.actual == '1'
            fromPocket.read == '0'
            fromPocket.word_count == 300
            fromPocket.time_added == 3
        }

        with(articles[1]) {
            id == 'TEST_ARTICLE_2'
            userId == 'TEST_USER_1'
            basicCard.actual == '1'
            fromPocket.read == '0'
            fromPocket.word_count == 200
            fromPocket.time_added == 2
        }

        with(articles[2]) {
            id == 'TEST_ARTICLE_1'
            userId == 'TEST_USER_1'
            basicCard.actual == '1'
            fromPocket.read == '0'
            fromPocket.word_count == 100
            fromPocket.time_added == 1
        }
    }

    def 'List<Article> getUserUnreadArticles(User user, cursorId = null, Integer count, minLength = null, maxLength = null) - limit count'() {
        when:
        User user1 = new User(id: 'TEST_USER_1')
        List<Article> articles = articlesService.getUserUnreadArticles(user1, null, 1, null, null)

        then:
        articles.size() == 1

        with(articles[0]) {
            id == 'TEST_ARTICLE_3'
            userId == 'TEST_USER_1'
            basicCard.actual == '1'
            fromPocket.read == '0'
            fromPocket.word_count == 300
            fromPocket.time_added == 3
        }
    }

    def 'List<Article> getUserUnreadArticles(User user, String cursorId, Integer count, minLength = null, maxLength = null)'() {
        when:
        User user1 = new User(id: 'TEST_USER_1')
        List<Article> articles = articlesService.getUserUnreadArticles(user1, 'TEST_ARTICLE_2', 1, null, null)

        then:
        articles.size() == 1

        with(articles[0]) {
            id == 'TEST_ARTICLE_1'
            userId == 'TEST_USER_1'
            basicCard.actual == '1'
            fromPocket.read == '0'
            fromPocket.word_count == 100
            fromPocket.time_added == 1
        }
    }

    def 'List<Article> getUserUnreadArticles(User user, String cursorId, Integer count, Integer minLength, maxLength = null)'() {
        when:
        User user1 = new User(id: 'TEST_USER_1')
        List<Article> articles = articlesService.getUserUnreadArticles(user1, null, 10, 250, null)

        then:
        articles.size() == 1

        with(articles[0]) {
            id == 'TEST_ARTICLE_3'
            userId == 'TEST_USER_1'
            basicCard.actual == '1'
            fromPocket.read == '0'
            fromPocket.word_count == 300
            fromPocket.time_added == 3
        }
    }

    def 'List<Article> getUserUnreadArticles(User user, String cursorId, Integer count, minLength = null, Integer maxLength)'() {
        when:
        User user1 = new User(id: 'TEST_USER_1')
        List<Article> articles = articlesService.getUserUnreadArticles(user1, null, 10, null, 150)

        then:
        articles.size() == 1

        with(articles[0]) {
            id == 'TEST_ARTICLE_1'
            userId == 'TEST_USER_1'
            basicCard.actual == '1'
            fromPocket.read == '0'
            fromPocket.word_count == 100
            fromPocket.time_added == 1
        }
    }

    def 'List<Article> getUserUnreadArticles(User user, String cursorId, Integer count, Integer minLength, Integer maxLength)'() {
        when:
        User user1 = new User(id: 'TEST_USER_1')
        List<Article> articles = articlesService.getUserUnreadArticles(user1, null, 10, 150, 250)

        then:
        articles.size() == 1

        with(articles[0]) {
            id == 'TEST_ARTICLE_2'
            userId == 'TEST_USER_1'
            basicCard.actual == '1'
            fromPocket.read == '0'
            fromPocket.word_count == 200
            fromPocket.time_added == 2
        }
    }

    def 'void markArticleAsRead(User user, String articleId)'() {
        setup: 'mock server instead of actual potic-pocket-api'
        User user1 = new User(id: 'TEST_USER_1', pocketAccessToken: 'POCKET_TOKEN_1')

        ErsatzServer ersatz = new ErsatzServer()
        ersatz.expectations {
            post('/archive/POCKET_TOKEN_1/POCKET_2') {
                called equalTo(1)
                responder {
                    content 'OK','plain/text'
                }
            }
        }
        ersatz.start()

        and: 'instruct service to use mock server'
        articlesService.pocketApiRest(ersatz.httpUrl)

        when: 'mark article as read'
        articlesService.markArticleAsRead(user1, 'TEST_ARTICLE_2')

        then: 'record in mongodb is updated'
        Article actual = mongoTemplate.find(query(where('id').is('TEST_ARTICLE_2')), Article).first()
        actual.fromPocket.read == '1'

        and: 'mock server received expected calls'
        ersatz.verify()

        cleanup: 'stop mock server'
        ersatz.stop()
    }

    def 'void markArticleAsRead(User user, String articleId) - call to potic-pocket-api failed'() {
        setup: 'mock server that fails to answer requests instead of actual potic-pocket-api'
        User user1 = new User(id: 'TEST_USER_1', pocketAccessToken: 'POCKET_TOKEN_1')

        ErsatzServer ersatz = new ErsatzServer()
        ersatz.expectations {
            post('/archive/POCKET_TOKEN_1/POCKET_2') {
                called equalTo(1)
                responder {
                    code(500)
                }
            }
        }
        ersatz.start()

        and: 'instruct service to use mock server'
        articlesService.pocketApiRest(ersatz.httpUrl)

        when: 'mark article as read'
        articlesService.markArticleAsRead(user1, 'TEST_ARTICLE_2')

        then: 'exception is thrown'
        thrown(RuntimeException)

        and: 'record in mongodb is not updated'
        Article actual = mongoTemplate.find(query(where('id').is('TEST_ARTICLE_2')), Article).first()
        actual.fromPocket.read == '0'

        and: 'mock server received expected calls'
        ersatz.verify()

        cleanup: 'stop mock server'
        ersatz.stop()
    }
}
