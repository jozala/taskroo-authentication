package com.taskroo.authn.data
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

    def "should create token in DB with given username and hashed key"() {
        given:
        def sha1HashedKey = MessageDigest.getInstance("SHA-1").digest(Base64.decodeBase64('someRandomString1234565432101234'))
        when:
        rememberMeTokenDao.saveToken(new RememberMeToken('testUserName', 'someRandomString1234565432101234'))
        then:
        def token = rememberMeTokensCollection.findOne()
        token.get('username') == 'testUserName'
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
}
