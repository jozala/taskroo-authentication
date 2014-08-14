package com.taskroo.authn.data

import com.mongodb.BasicDBObject
import com.taskroo.authn.domain.RememberMeToken
import org.apache.commons.codec.binary.Base64
import org.joda.time.DateTime

import java.security.MessageDigest

class RememberMeTokenDaoTest extends DaoTestBase {

    RememberMeTokenDao rememberMeTokenDao;

    void setup() {
        rememberMeTokenDao = new RememberMeTokenDao(rememberMeTokensCollection);
        cleanup()
    }

    void cleanup() {
        rememberMeTokensCollection.drop()
    }

    def "should create token in DB with given user ID and hashed key"() {
        given:
        def sha1HashedKey = MessageDigest.getInstance("SHA-1").digest(Base64.decodeBase64('someRandomString1234565432101234'))
        when:
        rememberMeTokenDao.saveToken(new RememberMeToken('testUserName', 'someRandomString1234565432101234'))
        then:
        def token = rememberMeTokensCollection.findOne()
        token.get('user_id') == 'testUserName'
        token.get('key') == Base64.encodeBase64String(sha1HashedKey);
    }

    def "should create token in DB with create_time set to now"() {
        when:
        rememberMeTokenDao.saveToken(new RememberMeToken('testUserName', 'someRandomString1234565432101234'))
        then:
        def token = rememberMeTokensCollection.findOne()
        token.get('create_time') > DateTime.now().minusSeconds(30).toDate()
        token.get('create_time') < DateTime.now().plusSeconds(30).toDate()
    }

    def "should return true when token exists in DB"() {
        given:
        rememberMeTokenDao.saveToken(new RememberMeToken('testUserName', 'someRandomString1234565432101234'))
        when:
        def exists = rememberMeTokenDao.tokenExists(new RememberMeToken('testUserName', 'someRandomString1234565432101234'))
        then:
        exists
    }

    def "should return false when token does not exist in DB"() {
        when:
        def exists = rememberMeTokenDao.tokenExists(new RememberMeToken('testUserName', 'someRandomString1234565432101234'))
        then:
        !exists
    }

    def "should remove rememberMeToken from DB when exists"() {
        given:
        rememberMeTokenDao.saveToken(new RememberMeToken('testUserName', 'someRandomString1234565432101234'))
        when:
        rememberMeTokenDao.remove(new RememberMeToken('testUserName', 'someRandomString1234565432101234'))
        then:
        rememberMeTokensCollection.count(new BasicDBObject(user_id: 'testUserName')) == 0
    }

    def "should throw exception when trying to remove non-existing token"() {
        when:
        rememberMeTokenDao.remove(new RememberMeToken('testUserName', 'someRandomString1234565432101234'))
        then:
        thrown(IllegalStateException)
    }
}
