package pl.aetas.gtweb.authn.integration
import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import org.joda.time.DateTime
import pl.aetas.gtweb.authn.data.SessionDao
import pl.aetas.gtweb.mongo.MongoConnector
import spock.lang.Specification

class SessionDaoTest extends Specification {

    static MongoConnector mongoConnector = new MongoConnector('mongodb://localhost')
    static DBCollection sessionsCollection = mongoConnector.gtWebDatabase.getCollection('sessions')

    SessionDao sessionDao

    void setup() {
        sessionDao = new SessionDao(sessionsCollection)
    }

    def "should save session with current date when creating session"() {
        when:
        def createdSession = sessionDao.create('givenUsername')
        then:
        def sessionDBObject = sessionsCollection.findOne(new BasicDBObject('_id', createdSession.sessionId))
        sessionDBObject.get('user_id') == 'givenUsername'
        (sessionDBObject.get('create_time') as Date) > DateTime.now().minusSeconds(10).toDate()
        (sessionDBObject.get('last_accessed_time') as Date) > DateTime.now().minusSeconds(10).toDate()
    }

    def "should return session object when session has been created"() {
        when:
        def session = sessionDao.create('givenUsername')
        then:
        session.getSessionId() != null
        session.getUserId() == 'givenUsername'
        session.getCreateTime() > DateTime.now().minusSeconds(10).toDate()
        session.getLastAccessedTime() > DateTime.now().minusSeconds(10).toDate()
    }

    def "should remove session from db when asked to remove it"() {
        given:
        def session = sessionDao.create('givenUsername')
        when:
        sessionDao.remove(session.sessionId)
        then:
        sessionsCollection.findOne(new BasicDBObject('_id', session.getSessionId())) == null
    }
}
