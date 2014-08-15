package com.taskroo.authn.data
import com.mongodb.BasicDBObject
import com.taskroo.authn.domain.SecurityToken
import org.joda.time.DateTime
import com.taskroo.authn.domain.Role
import com.taskroo.authn.domain.User

class SecurityTokenDaoTest extends DaoTestBase {

    SecurityTokenDao securityTokenDao

    void setup() {
        securityTokenDao = new SecurityTokenDao(securityTokensCollection)
    }

    void cleanup() {
        securityTokensCollection.drop()
    }

    def "should insert security token in DB"() {
        given:
        User user = User.UserBuilder.start().username('givenUsername').role(Role.ADMIN).role(Role.USER)
                .email('test@example.com').firstName('Mariusz').lastName('Jozala').password('unknown').salt('blablabla')
                .build()
        SecurityToken securityToken = SecurityToken.create(user)
        when:
        securityTokenDao.insert(securityToken)
        then:
        def securityTokenDBObject = securityTokensCollection.findOne()
        !securityTokenDBObject.get('_id').isEmpty()
        securityTokenDBObject.get('user_id') == 'givenUsername'
        (securityTokenDBObject.get('create_time') as Date) > DateTime.now().minusSeconds(10).toDate()
        (securityTokenDBObject.get('last_accessed_time') as Date) > DateTime.now().minusSeconds(10).toDate()
    }

    def "should insert security token in DB with rememberMe token"() {
        given:
        User user = User.UserBuilder.start().username('givenUsername').role(Role.ADMIN).role(Role.USER)
                .email('test@example.com').firstName('Mariusz').lastName('Jozala').password('unknown').salt('blablabla')
                .build()
        SecurityToken securityToken = SecurityToken.createWithRememberMeToken(user)
        when:
        securityTokenDao.insert(securityToken)
        then:
        def securityTokenDBObject = securityTokensCollection.findOne()
        !securityTokenDBObject.get('_id').isEmpty()
        securityTokenDBObject.get('rememberme_token') == securityToken.getRememberMeToken()
    }

    def "should save roles of user when creating a security token"() {
        given:
        User user = User.UserBuilder.start().username('givenUsername').role(Role.ADMIN).role(Role.USER)
                .email('test@example.com').firstName('Mariusz').lastName('Jozala').password('unknown').salt('blablabla')
                .build()
        SecurityToken securityToken = SecurityToken.create(user)
        when:
        securityTokenDao.insert(securityToken)
        then:
        def securityTokenDBObject = securityTokensCollection.findOne()
        securityTokenDBObject.get('roles').toSet() == [Role.ADMIN.intValue(), Role.USER.intValue()].toSet()
    }

    def "should remove security token from db when asked to remove it"() {
        given:
        User user = User.UserBuilder.start().username('givenUsername').role(Role.ADMIN).role(Role.USER)
                .email('test@example.com').firstName('Mariusz').lastName('Jozala').password('unknown').salt('blablabla')
                .build()
        SecurityToken securityToken = SecurityToken.create(user)
        securityTokenDao.insert(securityToken)
        when:
        securityTokenDao.remove(securityToken.id)
        then:
        securityTokensCollection.findOne(new BasicDBObject('_id', securityToken.getId())) == null
    }

    def "should find token with given id when token exists"() {
        given:
        User user = User.UserBuilder.start().username('givenUsername').role(Role.USER)
                .email('test@example.com').firstName('Mariusz').lastName('Jozala').password('unknown').salt('blablabla')
                .build()
        SecurityToken securityToken = SecurityToken.create(user)
        securityTokenDao.insert(securityToken)
        when:
        def securityTokenFromDb = securityTokenDao.findById(securityToken.getId())
        then:
        securityTokenFromDb.getUserId() == securityToken.getUserId()
        securityTokenFromDb.getId() == securityToken.getId()
        securityTokenFromDb.userRoles == [Role.USER.intValue()].toSet()
    }

    def "should throw exception when trying to find non-existing security token"() {
        when:
        securityTokenDao.findById("nonExistingSecurityTokenId")
        then:
        thrown(NonExistingResourceOperationException)
    }
}
