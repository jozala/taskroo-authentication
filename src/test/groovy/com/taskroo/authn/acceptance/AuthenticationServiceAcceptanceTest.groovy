package com.taskroo.authn.acceptance

import com.mongodb.BasicDBObject
import com.mongodb.DB
import com.mongodb.DBCollection
import com.taskroo.authn.domain.RememberMeToken
import com.taskroo.mongo.MongoConnector
import com.taskroo.testing.RunJetty
import groovyx.net.http.ContentType
import groovyx.net.http.RESTClient
import spock.lang.Specification

@RunJetty
class AuthenticationServiceAcceptanceTest extends Specification {


    static final String USERNAME = 'someUsername'
    static final String PASSWORD = 'secretPass'

    static RESTClient client = new RESTClient('http://localhost:8080/')
    private static final DB db = new MongoConnector('mongodb://localhost').getDatabase('taskroo')
    public static final DBCollection securityTokensCollection = db.getCollection("securityTokens")
    public static final DBCollection usersCollection = db.getCollection("users")
    public static final DBCollection rememberMeTokensCollections = db.getCollection("rememberMeTokens")

    def setupSpec() {
        cleanup()
        def userMap = [_id: USERNAME, password: 'B/HBxKhgF8mALBaOt+KUQRvWthU=', enabled: true, first_name: 'fname', last_name: 'lname',
                       email: 'email@aetas.pl', roles: ['USER'], salt: 'F8Sgh1uJmuld8J7t9R+JOgq+vn8=']
        usersCollection.insert(new BasicDBObject(userMap))
        client.handler.failure = { it }
    }

    void cleanup() {
        rememberMeTokensCollections.remove(new BasicDBObject('user_id', USERNAME))
        securityTokensCollection.remove(new BasicDBObject('user_id', USERNAME))
    }

    def "should return 201 and security token when new security token has been created"() {
        when: 'sending correct login and password to service'
        def response = client.post(
                path: 'authToken/login',
                body: [username: USERNAME, password: PASSWORD],
                requestContentType: ContentType.JSON)
        then: "response is 201 with security token information"
        response.status == 201
        response.data.id != null
        and: "security token gets created in the DB"
        securityTokensCollection.count(new BasicDBObject('_id', response.data.id)) == 1
    }

    def "should create security token in DB with rememberMeToken when login request sent with rememberMe set to true"() {
        when: 'sending correct login and password to service'
        def response = client.post(
                path: 'authToken/login',
                body: [username: USERNAME, password: PASSWORD, rememberMe: true],
                requestContentType: ContentType.JSON)
        then: "security token gets created in the DB with rememberMeToken"
        def securityTokenDb = securityTokensCollection.findOne(new BasicDBObject('_id', response.data.id))
        securityTokenDb.get('rememberme_token') != null
        securityTokenDb.get('rememberme_token') == response.data.rememberMeToken
    }

    def "should return 400 (bad request) when username not given"() {
        when: 'sending only password to service'
        def response = client.post(
                path: 'authToken/login',
                body: [password: PASSWORD],
                requestContentType: ContentType.JSON)
        then:
        response.status == 400
    }

    def "should return 401 (unauthorized) when non-existing username sent"() {
        when: 'sending incorrect login and password to service'
        def response = client.post(
                path: 'authToken/login',
                body: [username: 'nonExistingUsername', password: PASSWORD],
                requestContentType: ContentType.JSON)
        then:
        response.status == 401
    }

    def "should return 401 (unauthorized) when user exists, but incorrect password given"() {
        given:

        when: 'sending incorrect login and password to service'
        def response = client.post(
                path: 'authToken/login',
                body: [username: USERNAME, password: 'incorrect'],
                requestContentType: ContentType.JSON)
        then: 'response code is 401'
        response.status == 401
    }

    def "should remove security token and return 204 when deleting security token"() {
        given: 'security token exists'
        def createResponse = client.post(
                path: 'authToken/login',
                body: [username: USERNAME, password: PASSWORD],
                requestContentType: ContentType.JSON)
        def securityTokenId = createResponse.data.id
        when:
        def response = client.delete(path: "authToken/$securityTokenId")
        then:
        response.status == 204
        securityTokensCollection.findOne(new BasicDBObject('_id', securityTokenId)) == null
    }

    def "should return 404 when trying to delete non-existing security token"() {
        when:
        def response = client.delete(path: "authToken/nonExistingSecTokenId")
        then:
        response.status == 404
    }

    def "should remove rememberMeToken used to create security token when deleting security token"() {
        given: 'rememberMeToken exists for existing customer'
        def response = client.post(
                path: 'authToken/login',
                body: [username: USERNAME, password: PASSWORD, rememberMe: true],
                requestContentType: ContentType.JSON)
        when: 'user sends request to logout'
        client.delete(path: "authToken/$response.data.id")
        then: 'rememberMeToken related to the given security token is removed from DB'
        String tokenString = response.data.rememberMeToken
        def rememberMeToken = new RememberMeToken(tokenString.split(':')[0], tokenString.split(':')[1])
        rememberMeTokensCollections.count(new BasicDBObject('user_id': USERNAME, key: rememberMeToken.getHashedKey())) == 0
    }

    def "should create hashed rememberMe token in the database when login request sent with rememberMe set to true"() {
        when: 'sending correct login and password to service with rememberMe set to true'
        def response = client.post(
                path: 'authToken/login',
                body: [username: USERNAME, password: PASSWORD, rememberMe: true],
                requestContentType: ContentType.JSON)
        then: "response is 201"
        response.status == 201
        and: "rememberMe token gets created in the DB"
        rememberMeTokensCollections.count(new BasicDBObject('user_id', USERNAME)) == 1
    }

    def "should return rememberMe token in response when login request sent with rememberMe set to true"() {
        when: 'sending correct login and password to service with rememberMe set to true'
        def response = client.post(
                path: 'authToken/login',
                body: [username: USERNAME, password: PASSWORD, rememberMe: true],
                requestContentType: ContentType.JSON)
        rememberMeTokensCollections.findOne().get('key')
        then: "response is 201"
        response.status == 201
        and: "rememberMe token is returned to user"
        response.data.rememberMeToken =~ /$USERNAME:[a-zA-Z0-9]{32}/
    }

    def "should not create rememberMeToken entry in DB when login request is sent with rememberMe set to false"() {
        when: 'sending correct login and password to service with rememberMe set to false'
        client.post(
                path: 'authToken/login',
                body: [username: USERNAME, password: PASSWORD, rememberMe: false],
                requestContentType: ContentType.JSON)
        then: "rememberMe token is not created in the DB"
        rememberMeTokensCollections.count(new BasicDBObject('user_id', USERNAME)) == 0
    }

    def "should create security token when request with existing rememberMeToken received"() {
        given: 'rememberMeToken exists for existing customer'
        def response = client.post(
                path: 'authToken/login',
                body: [username: USERNAME, password: PASSWORD, rememberMe: true],
                requestContentType: ContentType.JSON)
        String rememberMeTokenString = response.data.rememberMeToken
        when: 'request is send to create security token using rememberMeToken'
        response = client.post(path: 'authToken/loginWithRememberMe',
                    body: rememberMeTokenString,
                    requestContentType: ContentType.TEXT)
        then: 'response is 201 with securityToken ID'
        response.status == 201
        and: "security token gets created in the DB"
        securityTokensCollection.count(new BasicDBObject([_id: response.data.id, user_id: USERNAME])) == 1
    }

    def "should return 401 when trying to create a security token with incorrect token"() {
        when: 'request is send to create security token using incorrect rememberMeToken'
        def response = client.post(path: 'authToken/loginWithRememberMe',
                body: "$USERNAME:incorrectKey34567890123456789012",
                requestContentType: ContentType.TEXT)
        then: 'response should be 401'
        response.status == 401
        and: 'security token is not created'
        securityTokensCollection.count(new BasicDBObject('user_id', USERNAME)) == 0
    }

    def "should return 400 when trying to create security token with invalid rememberMe token"() {
        when: 'request is send to create security token using invalid rememberMeToken'
        def response = client.post(path: 'authToken/loginWithRememberMe',
                body: "$USERNAME:keyWithTooLessCharacters",
                requestContentType: ContentType.TEXT)
        then: 'response should be 400'
        response.status == 400
        and: 'security token is not created'
        securityTokensCollection.count(new BasicDBObject('user_id', USERNAME)) == 0
    }

    def "should remove rememberMeToken from DB when rememberMeToken is used"() {
        given: 'rememberMeToken exists for existing customer'
        def response = client.post(
                path: 'authToken/login',
                body: [username: USERNAME, password: PASSWORD, rememberMe: true],
                requestContentType: ContentType.JSON)
        String rememberMeTokenString = response.data.rememberMeToken
        when: 'request is send to create security token using rememberMeToken'
        client.post(path: 'authToken/loginWithRememberMe',
                body: rememberMeTokenString,
                requestContentType: ContentType.TEXT)
        then: 'token is removed from DB'
        def firstRememberMeToken = new RememberMeToken(rememberMeTokenString.split(':')[0], rememberMeTokenString.split(':')[1])
        rememberMeTokensCollections.count(new BasicDBObject('user_id': USERNAME, key: firstRememberMeToken.getHashedKey())) == 0
    }

    def "should create and return new rememberMeToken when login using rememberMeToken"() {
        given: 'rememberMeToken exists for existing customer'
        def firstResponse = client.post(
                path: 'authToken/login',
                body: [username: USERNAME, password: PASSWORD, rememberMe: true],
                requestContentType: ContentType.JSON)
        String rememberMeTokenString = firstResponse.data.rememberMeToken
        when: 'request is send to create security token using rememberMeToken'
        def response = client.post(path: 'authToken/loginWithRememberMe',
                body: rememberMeTokenString,
                requestContentType: ContentType.TEXT)
        then: 'new token key is returned'
        response.data.rememberMeToken =~ /$USERNAME:[a-zA-Z0-9]{32}/
        response.data.rememberMeToken != firstResponse.data.rememberMeToken
        and: 'new token is created in DB'
        rememberMeTokensCollections.count(new BasicDBObject('user_id', USERNAME)) == 1
    }

    void cleanupSpec() {
        usersCollection.remove(new BasicDBObject('_id', USERNAME))
    }
}
