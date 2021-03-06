package me.potic.articles

import com.mongodb.Mongo
import com.stehno.ersatz.ErsatzServer
import me.potic.articles.domain.Article
import me.potic.articles.domain.Card
import me.potic.articles.domain.PocketArticle
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
                Article.builder().id('TEST_ARTICLE_1').userId('TEST_USER_1').card(new Card(timestamp: 2000, pocketId: 'POCKET_1', url: 'URL_1', title: 'TITLE_1')).fromPocket(new PocketArticle(item_id: 'POCKET_1', resolved_title: 'TITLE_1', status: '0', word_count: 100, time_added: 1)).build(),
                Article.builder().id('TEST_ARTICLE_2').userId('TEST_USER_1').card(new Card(timestamp: 2000, pocketId: 'POCKET_2', url: 'URL_2', title: 'TITLE_2')).fromPocket(new PocketArticle(item_id: 'POCKET_2', resolved_title: 'TITLE_2', status: '0', word_count: 200, time_added: 2)).build(),
                Article.builder().id('TEST_ARTICLE_3').userId('TEST_USER_1').card(new Card(timestamp: 2000, pocketId: 'POCKET_3', url: 'URL_3', title: 'TITLE_3')).fromPocket(new PocketArticle(item_id: 'POCKET_3', resolved_title: 'TITLE_3', status: '0', word_count: 300, time_added: 3)).build(),
                Article.builder().id('TEST_ARTICLE_4').userId('TEST_USER_1').card(new Card(timestamp: 2000, pocketId: 'POCKET_4', url: 'URL_4', title: 'TITLE_4')).fromPocket(new PocketArticle(item_id: 'POCKET_4', resolved_title: 'TITLE_4', status: '1', word_count: 100, time_added: 4)).build(),
                Article.builder().id('TEST_ARTICLE_5').userId('TEST_USER_2').card(new Card(timestamp: 2000, pocketId: 'POCKET_5', url: 'URL_5', title: 'TITLE_5')).fromPocket(new PocketArticle(item_id: 'POCKET_5', resolved_title: 'TITLE_5', status: '0', word_count: 200, time_added: 5)).build(),
                Article.builder().id('TEST_ARTICLE_6').userId('TEST_USER_2').card(new Card(timestamp: 2000, pocketId: 'POCKET_6', url: 'URL_6', title: 'TITLE_6')).fromPocket(new PocketArticle(item_id: 'POCKET_6', resolved_title: 'TITLE_6', status: '1', word_count: 100, time_added: 6)).build(),

                // same articles but non-actual
                Article.builder().id('TEST_ARTICLE_01').userId('TEST_USER_1').card(new Card(pocketId: 'POCKET_1', url: 'URL_1')).fromPocket(new PocketArticle(item_id: 'POCKET_1', status: '0', word_count: 100, time_added: 1)).build(),
                Article.builder().id('TEST_ARTICLE_02').userId('TEST_USER_1').card(new Card(pocketId: 'POCKET_2', title: 'TITLE_2')).fromPocket(new PocketArticle(item_id: 'POCKET_2', status: '0', word_count: 200, time_added: 2)).build(),
                Article.builder().id('TEST_ARTICLE_03').userId('TEST_USER_1').card(new Card(pocketId: 'POCKET_3')).fromPocket(new PocketArticle(item_id: 'POCKET_3', status: '0', word_count: 300, time_added: 3)).build(),
                Article.builder().id('TEST_ARTICLE_04').userId('TEST_USER_1').card(new Card(pocketId: 'POCKET_4')).fromPocket(new PocketArticle(item_id: 'POCKET_4', status: '1', word_count: 100, time_added: 4)).build(),
                Article.builder().id('TEST_ARTICLE_05').userId('TEST_USER_2').card(new Card(pocketId: 'POCKET_5')).fromPocket(new PocketArticle(item_id: 'POCKET_5', status: '0', word_count: 200, time_added: 5)).build(),
                Article.builder().id('TEST_ARTICLE_06').userId('TEST_USER_2').card(new Card(pocketId: 'POCKET_6')).fromPocket(new PocketArticle(item_id: 'POCKET_6', status: '1', word_count: 100, time_added: 6)).build()
        ]
        mongoTemplate.insert(articles, Article)
    }

    def 'List<Article> getLatestUserUnreadArticles(String userId, List<String> skipIds = null, Integer count, minLength = null, maxLength = null)'() {
        when:
        List<Article> articles = articlesService.getLatestUserUnreadArticles('TEST_USER_1', null, 10, null, null)

        then:
        articles.size() == 3

        with(articles[0]) {
            id == 'TEST_ARTICLE_3'
            userId == 'TEST_USER_1'
            card.pocketId == 'POCKET_3'
            fromPocket.status == '0'
            fromPocket.word_count == 300
            fromPocket.time_added == 3
        }

        with(articles[1]) {
            id == 'TEST_ARTICLE_2'
            userId == 'TEST_USER_1'
            card.pocketId == 'POCKET_2'
            fromPocket.status == '0'
            fromPocket.word_count == 200
            fromPocket.time_added == 2
        }

        with(articles[2]) {
            id == 'TEST_ARTICLE_1'
            userId == 'TEST_USER_1'
            card.pocketId == 'POCKET_1'
            fromPocket.status == '0'
            fromPocket.word_count == 100
            fromPocket.time_added == 1
        }
    }

    def 'List<Article> getLatestUserUnreadArticles(String userId, List<String> skipIds, Integer count, minLength = null, maxLength = null) - limit count'() {
        when:
        List<Article> articles = articlesService.getLatestUserUnreadArticles('TEST_USER_1', null, 1, null, null)

        then:
        articles.size() == 1

        with(articles[0]) {
            id == 'TEST_ARTICLE_3'
            userId == 'TEST_USER_1'
            card.pocketId == 'POCKET_3'
            fromPocket.status == '0'
            fromPocket.word_count == 300
            fromPocket.time_added == 3
        }
    }

    def 'List<Article> getLatestUserUnreadArticles(String userId, List<String> skipIds, Integer count, minLength = null, maxLength = null)'() {
        when:
        List<Article> articles = articlesService.getLatestUserUnreadArticles('TEST_USER_1', ['TEST_ARTICLE_3', 'TEST_ARTICLE_2'], 10, null, null)

        then:
        articles.size() == 1

        with(articles[0]) {
            id == 'TEST_ARTICLE_1'
            userId == 'TEST_USER_1'
            card.pocketId == 'POCKET_1'
            fromPocket.status == '0'
            fromPocket.word_count == 100
            fromPocket.time_added == 1
        }
    }

    def 'List<Article> getLatestUserUnreadArticles(String userId, List<String> skipIds, Integer count, Integer minLength, maxLength = null)'() {
        when:
        List<Article> articles = articlesService.getLatestUserUnreadArticles('TEST_USER_1', null, 10, 250, null)

        then:
        articles.size() == 1

        with(articles[0]) {
            id == 'TEST_ARTICLE_3'
            userId == 'TEST_USER_1'
            card.pocketId == 'POCKET_3'
            fromPocket.status == '0'
            fromPocket.word_count == 300
            fromPocket.time_added == 3
        }
    }

    def 'List<Article> getLatestUserUnreadArticles(String userId, List<String> skipIds, Integer count, minLength = null, Integer maxLength)'() {
        when:
        List<Article> articles = articlesService.getLatestUserUnreadArticles('TEST_USER_1', null, 10, null, 150)

        then:
        articles.size() == 1

        with(articles[0]) {
            id == 'TEST_ARTICLE_1'
            userId == 'TEST_USER_1'
            card.pocketId == 'POCKET_1'
            fromPocket.status == '0'
            fromPocket.word_count == 100
            fromPocket.time_added == 1
        }
    }

    def 'List<Article> getLatestUserUnreadArticles(String userId, List<String> skipIds, Integer count, Integer minLength, Integer maxLength)'() {
        when:
        List<Article> articles = articlesService.getLatestUserUnreadArticles('TEST_USER_1', null, 10, 150, 250)

        then:
        articles.size() == 1

        with(articles[0]) {
            id == 'TEST_ARTICLE_2'
            userId == 'TEST_USER_1'
            card.pocketId == 'POCKET_2'
            fromPocket.status == '0'
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
        actual.fromPocket.status == '1'

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
        actual.fromPocket.status == '0'

        and: 'mock server received expected calls'
        ersatz.verify()

        cleanup: 'stop mock server'
        ersatz.stop()
    }

    def 'Article upsertFromPocket(String userId, PocketArticle articleFromPocket) - new article'() {
        setup:
        String userId = 'TEST_USER_1'
        PocketArticle articleFromPocket = new PocketArticle(
                item_id: 'INGESTED_1',
                resolved_id: 'INGESTED_1',
                given_url: 'URL_1',
                time_added: 1,
                time_updated: 2,
                time_favorited: 3,
                time_read: 4,
                word_count: 5
        )

        when:
        Article actual = articlesService.upsertFromPocket(userId, articleFromPocket)
        actual = mongoTemplate.find(query(where('id').is(actual.id)), Article).first()

        then:
        with(actual) {
            id != null
            userId == 'TEST_USER_1'
            fromPocket.item_id == 'INGESTED_1'
            fromPocket.resolved_id == 'INGESTED_1'
            fromPocket.given_url == 'URL_1'
            fromPocket.time_added == 1
            fromPocket.time_updated == 2
            fromPocket.time_favorited == 3
            fromPocket.time_read == 4
            fromPocket.word_count == 5
        }
    }

    def 'Article upsertFromPocket(String userId, PocketArticle articleFromPocket) - already ingested article'() {
        setup:
        String userId = 'TEST_USER_1'
        PocketArticle articleFromPocket = new PocketArticle(
                item_id: 'ALREADY_INGESTED_1',
                resolved_id: 'ALREADY_INGESTED_1',
                given_url: 'URL_1',
                time_added: 1,
                time_updated: 2,
                time_favorited: 3,
                time_read: 4,
                word_count: 5
        )
        Article alreadyIngested = Article.builder().id('TEST_ARTICLE_1').userId('TEST_USER_1').card(new Card(timestamp: 3000)).fromPocket(new PocketArticle(item_id: 'ALREADY_INGESTED_1', resolved_title: 'TITLE_1', status: '0', word_count: 100, time_added: 1)).build()

        and:
        mongoTemplate.save(alreadyIngested)

        when:
        Article actual = articlesService.upsertFromPocket(userId, articleFromPocket)
        actual = mongoTemplate.find(query(where('id').is(actual.id)), Article).first()

        then:
        with(actual) {
            id == 'TEST_ARTICLE_1'
            userId == 'TEST_USER_1'
            fromPocket.item_id == 'ALREADY_INGESTED_1'
            fromPocket.resolved_id == 'ALREADY_INGESTED_1'
            fromPocket.given_url == 'URL_1'
            fromPocket.time_added == 1
            fromPocket.time_updated == 2
            fromPocket.time_favorited == 3
            fromPocket.time_read == 4
            fromPocket.word_count == 5
        }
    }

    def 'Article upsertFromPocket(String userId, PocketArticle articleFromPocket) - empty article'() {
        setup:
        String userId = 'TEST_USER_1'
        PocketArticle articleFromPocket = new PocketArticle(item_id: 'INGESTED_1')

        when:
        Article actual = articlesService.upsertFromPocket(userId, articleFromPocket)
        actual = mongoTemplate.find(query(where('id').is(actual.id)), Article).first()

        then:
        with(actual) {
            id != null
            userId == 'TEST_USER_1'
            fromPocket.item_id == 'INGESTED_1'
        }
    }
}
