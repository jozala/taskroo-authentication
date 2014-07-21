package com.taskroo.authn.data
import com.mongodb.BasicDBObject
import org.joda.time.DateTime
import com.taskroo.authn.domain.Role
import com.taskroo.authn.domain.User

class SessionDaoTest extends DaoTestBase {

    SessionDao sessionDao

    void setup() {
        sessionDao = new SessionDao(sessionsCollection)
    }

    def "should save session with current date when creating session"() {
        given:
        User user = User.UserBuilder.start().username('givenUsername').role(Role.ADMIN).role(Role.USER)
                .email('test@example.com').firstName('Mariusz').lastName('Jozala').password('unknown').salt('blablabla')
                .build()
        when:
        def createdSession = sessionDao.create(user)
        then:
        def sessionDBObject = sessionsCollection.findOne(new BasicDBObject('_id', createdSession.sessionId))
        sessionDBObject.get('user_id') == 'givenUsername'
        (sessionDBObject.get('create_time') as Date) > DateTime.now().minusSeconds(10).toDate()
        (sessionDBObject.get('last_accessed_time') as Date) > DateTime.now().minusSeconds(10).toDate()
    }

    def "should return session object when session has been created"() {
        given:
        User user = User.UserBuilder.start().username('givenUsername').role(Role.ADMIN).role(Role.USER)
                .email('test@example.com').firstName('Mariusz').lastName('Jozala').password('unknown').salt('blablabla')
                .build()
        when:
        def session = sessionDao.create(user)
        then:
        session.getSessionId() != null
        session.getUserId() == 'givenUsername'
        session.getCreateTime() > DateTime.now().minusSeconds(10).toDate()
        session.getLastAccessedTime() > DateTime.now().minusSeconds(10).toDate()
    }

    def "should save roles of user when creating a session"() {
        given:
        User user = User.UserBuilder.start().username('givenUsername').role(Role.ADMIN).role(Role.USER)
                .email('test@example.com').firstName('Mariusz').lastName('Jozala').password('unknown').salt('blablabla')
                .build()
        when:
        def createdSession = sessionDao.create(user)
        then:
        def sessionDBObject = sessionsCollection.findOne(new BasicDBObject('_id', createdSession.sessionId))
        sessionDBObject.get('roles').toSet() == [Role.ADMIN.intValue(), Role.USER.intValue()].toSet()
    }

    def "should remove session from db when asked to remove it"() {
        given:
        User user = User.UserBuilder.start().username('givenUsername').role(Role.ADMIN).role(Role.USER)
                .email('test@example.com').firstName('Mariusz').lastName('Jozala').password('unknown').salt('blablabla')
                .build()
        def session = sessionDao.create(user)
        when:
        sessionDao.remove(session.sessionId)
        then:
        sessionsCollection.findOne(new BasicDBObject('_id', session.getSessionId())) == null
    }
}
