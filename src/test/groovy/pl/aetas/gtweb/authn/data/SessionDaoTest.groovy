package pl.aetas.gtweb.authn.data
import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import org.joda.time.DateTime
import spock.lang.Specification

class SessionDaoTest extends Specification {

    SessionDao sessionDao

    // mocks
    DBCollection sessionsCollection

    void setup() {
        sessionsCollection = Mock(DBCollection)
        sessionDao = new SessionDao(sessionsCollection)
    }

    def "should save session with current date when creating session"() {
        when:
        sessionDao.create('givenUsername')
        then:
        1 * sessionsCollection.insert({
            ((BasicDBObject)it).get('user_id') == 'givenUsername' &&
            (((BasicDBObject)it).get('create_time') as Date) > DateTime.now().minusSeconds(10).toDate() &&
            (((BasicDBObject)it).get('last_accessed_time') as Date) > DateTime.now().minusSeconds(10).toDate()
        })
    }

    def "should return session object when session has been created"() {
        when:
        def session = sessionDao.create('givenUsername')
        then:
        session.getUserId() == 'givenUsername'
        session.getCreateTime() > DateTime.now().minusSeconds(10).toDate()
        session.getLastAccessedTime() > DateTime.now().minusSeconds(10).toDate()
    }

    def "should remove session from db when asked to remove it"() {
        when:
        sessionDao.remove('someSessionId')
        then:
        1 * sessionsCollection.remove(new BasicDBObject('_id', 'someSessionId'))
    }
}
