package me.potic.articles

import com.mongodb.Mongo
import me.potic.articles.domain.Article
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

    def setup() {
        MongoTemplate mongoTemplate = new MongoTemplate(new Mongo(mongodb.containerIpAddress, mongodb.getMappedPort(27017)), databaseName)
        articlesService.mongoTemplate = mongoTemplate

        List articles = [
                Article.builder().id('TEST_ARTICLE_1').userId('TEST_USER_1').read(false).wordCount(100).timeAdded(1).build(),
                Article.builder().id('TEST_ARTICLE_2').userId('TEST_USER_1').read(false).wordCount(200).timeAdded(2).build(),
                Article.builder().id('TEST_ARTICLE_3').userId('TEST_USER_1').read(false).wordCount(300).timeAdded(3).build(),
                Article.builder().id('TEST_ARTICLE_4').userId('TEST_USER_1').read(true).wordCount(100).timeAdded(4).build(),
                Article.builder().id('TEST_ARTICLE_5').userId('TEST_USER_2').read(false).wordCount(200).timeAdded(5).build(),
                Article.builder().id('TEST_ARTICLE_6').userId('TEST_USER_2').read(true).wordCount(100).timeAdded(6).build()
        ]
        mongoTemplate.insert(articles, Article)
    }

    def 'Collection<Article> getUserUnreadArticles(String pocketSquareUserId, cursorId = null, Integer count, minLength = null, maxLength = null)'() {
        when:
        List<Article> articles = articlesService.getUserUnreadArticles('TEST_USER_1', null, 10, null, null)

        then:
        articles.size() == 3

        with(articles[0]) {
            id == 'TEST_ARTICLE_3___________'
            userId == 'TEST_USER_1'
            read == false
            wordCount == 300
            timeAdded == 3
        }

        with(articles[1]) {
            id == 'TEST_ARTICLE_2'
            userId == 'TEST_USER_1'
            read == false
            wordCount == 200
            timeAdded == 2
        }

        with(articles[2]) {
            id == 'TEST_ARTICLE_1'
            userId == 'TEST_USER_1'
            read == false
            wordCount == 100
            timeAdded == 1
        }
    }

    def 'Collection<Article> getUserUnreadArticles(String pocketSquareUserId, cursorId = null, Integer count, minLength = null, maxLength = null) - limit count'() {
        when:
        List<Article> articles = articlesService.getUserUnreadArticles('TEST_USER_1', null, 1, null, null)

        then:
        articles.size() == 1

        with(articles[0]) {
            id == 'TEST_ARTICLE_3'
            userId == 'TEST_USER_1'
            read == false
            wordCount == 300
            timeAdded == 3
        }
    }

    def 'Collection<Article> getUserUnreadArticles(String pocketSquareUserId, String cursorId, Integer count, minLength = null, maxLength = null)'() {
        when:
        List<Article> articles = articlesService.getUserUnreadArticles('TEST_USER_1', 2, 1, null, null)

        then:
        articles.size() == 1

        with(articles[0]) {
            id == 'TEST_ARTICLE_1'
            userId == 'TEST_USER_1'
            read == false
            wordCount == 100
            timeAdded == 1
        }
    }

    def 'Collection<Article> getUserUnreadArticles(String pocketSquareUserId, String cursorId, Integer count, Integer minLength, maxLength = null)'() {
        when:
        List<Article> articles = articlesService.getUserUnreadArticles('TEST_USER_1', null, 10, 250, null)

        then:
        articles.size() == 1

        with(articles[0]) {
            id == 'TEST_ARTICLE_3'
            userId == 'TEST_USER_1'
            read == false
            wordCount == 300
            timeAdded == 3
        }
    }

    def 'Collection<Article> getUserUnreadArticles(String pocketSquareUserId, String cursorId, Integer count, minLength = null, Integer maxLength)'() {
        when:
        List<Article> articles = articlesService.getUserUnreadArticles('TEST_USER_1', null, 10, null, 150)

        then:
        articles.size() == 1

        with(articles[0]) {
            id == 'TEST_ARTICLE_1'
            userId == 'TEST_USER_1'
            read == false
            wordCount == 100
            timeAdded == 1
        }
    }

    def 'Collection<Article> getUserUnreadArticles(String pocketSquareUserId, String cursorId, Integer count, Integer minLength, Integer maxLength)'() {
        when:
        List<Article> articles = articlesService.getUserUnreadArticles('TEST_USER_1', null, 10, 150, 250)

        then:
        articles.size() == 1

        with(articles[0]) {
            id == 'TEST_ARTICLE_2'
            userId == 'TEST_USER_1'
            read == false
            wordCount == 200
            timeAdded == 2
        }
    }
}
