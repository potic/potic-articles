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

    def 'Collection<Article> getUserUnreadArticles(String pocketSquareUserId, String cursorId, Integer count, Integer minLength, Integer maxLength)'() {
        setup:
        MongoTemplate mongoTemplate = new MongoTemplate(new Mongo(mongodb.containerIpAddress, mongodb.getMappedPort(27017)), databaseName)
        articlesService.mongoTemplate = mongoTemplate

        and:
        Article article1 = Article.builder().id('TEST_ARTICLE_1').userId('TEST_USER_1').read(false).wordCount(100).build()
        mongoTemplate.insert(article1)

        when:
        List<Article> articles = articlesService.getUserUnreadArticles('TEST_USER_1', null, 1, null, null)

        then:
        articles.size() == 1
        articles[0].id == 'TEST_ARTICLE_1'
        articles[0].userId == 'TEST_USER_1'
        articles[0].read == false
        articles[0].wordCount == 100
    }
}
