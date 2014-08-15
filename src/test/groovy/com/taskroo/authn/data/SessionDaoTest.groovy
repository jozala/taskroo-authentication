package com.taskroo.authn.data
import com.mongodb.BasicDBObject
import com.taskroo.authn.domain.Session
import org.joda.time.DateTime
import com.taskroo.authn.domain.Role
import com.taskroo.authn.domain.User

class SessionDaoTest extends DaoTestBase {

    SessionDao sessionDao

    void setup() {
        sessionDao = new SessionDao(sessionsCollection)
    }

    void cleanup() {
        sessionsCollection.drop()
    }

    def "should insert session in DB"() {
        given:
        User user = User.UserBuilder.start().username('givenUsername').role(Role.ADMIN).role(Role.USER)
                .email('test@example.com').firstName('Mariusz').lastName('Jozala').password('unknown').salt('blablabla')
                .build()
        Session session = Session.create(user)
        when:
        sessionDao.insert(session)
        then:
        def sessionDBObject = sessionsCollection.findOne()
        !sessionDBObject.get('_id').isEmpty()
        sessionDBObject.get('user_id') == 'givenUsername'
        (sessionDBObject.get('create_time') as Date) > DateTime.now().minusSeconds(10).toDate()
        (sessionDBObject.get('last_accessed_time') as Date) > DateTime.now().minusSeconds(10).toDate()
    }

    def "should insert session in DB with rememberMe token"() {
        given:
        User user = User.UserBuilder.start().username('givenUsername').role(Role.ADMIN).role(Role.USER)
                .email('test@example.com').firstName('Mariusz').lastName('Jozala').password('unknown').salt('blablabla')
                .build()
        Session session = Session.createWithRememberMeToken(user)
        when:
        sessionDao.insert(session)
        then:
        def sessionDBObject = sessionsCollection.findOne()
        !sessionDBObject.get('_id').isEmpty()
        sessionDBObject.get('rememberme_token') == session.getRememberMeToken()
    }

    def "should save roles of user when creating a session"() {
        given:
        User user = User.UserBuilder.start().username('givenUsername').role(Role.ADMIN).role(Role.USER)
                .email('test@example.com').firstName('Mariusz').lastName('Jozala').password('unknown').salt('blablabla')
                .build()
        Session session = Session.create(user)
        when:
        sessionDao.insert(session)
        then:
        def sessionDBObject = sessionsCollection.findOne()
        sessionDBObject.get('roles').toSet() == [Role.ADMIN.intValue(), Role.USER.intValue()].toSet()
    }

    def "should remove session from db when asked to remove it"() {
        given:
        User user = User.UserBuilder.start().username('givenUsername').role(Role.ADMIN).role(Role.USER)
                .email('test@example.com').firstName('Mariusz').lastName('Jozala').password('unknown').salt('blablabla')
                .build()
        Session session = Session.create(user)
        sessionDao.insert(session)
        when:
        sessionDao.remove(session.sessionId)
        then:
        sessionsCollection.findOne(new BasicDBObject('_id', session.getSessionId())) == null
    }

    def "should find token with given id when token exists"() {
        given:
        User user = User.UserBuilder.start().username('givenUsername').role(Role.USER)
                .email('test@example.com').firstName('Mariusz').lastName('Jozala').password('unknown').salt('blablabla')
                .build()
        Session session = Session.create(user)
        sessionDao.insert(session)
        when:
        def sessionFromDb = sessionDao.findById(session.getSessionId())
        then:
        sessionFromDb.getUserId() == session.getUserId()
        sessionFromDb.getSessionId() == session.getSessionId()
        sessionFromDb.userRoles == [Role.USER.intValue()].toSet()
    }

    def "should throw exception when trying to find non-existing session"() {
        when:
        sessionDao.findById("nonExistingSessionId")
        then:
        thrown(NonExistingResourceOperationException)
    }
}
