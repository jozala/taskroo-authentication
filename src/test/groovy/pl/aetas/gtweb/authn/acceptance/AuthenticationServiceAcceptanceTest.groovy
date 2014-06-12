package pl.aetas.gtweb.authn.acceptance
import com.mongodb.BasicDBObject
import com.mongodb.DB
import com.mongodb.DBCollection
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import pl.aetas.gtweb.mongo.MongoConnector
import pl.aetas.testing.RunJetty
import spock.lang.Specification

@RunJetty
class AuthenticationServiceAcceptanceTest extends Specification {


    static final String USERNAME = 'someUsername'
    static final String PASSWORD = 'secretPass'

    static RESTClient client = new RESTClient('http://localhost:8080/')
    private static final DB db = new MongoConnector('mongodb://localhost').getDatabase('gtweb')
    public static final DBCollection sessionsCollection = db.getCollection("sessions")
    public static final DBCollection usersCollection = db.getCollection("users")

    def setupSpec() {
        def userMap = [_id: USERNAME, password: 'B/HBxKhgF8mALBaOt+KUQRvWthU=', enabled: true, first_name: 'fname', last_name: 'lname',
                       email: 'email@aetas.pl', roles: ['USER'], salt: 'F8Sgh1uJmuld8J7t9R+JOgq+vn8=']
        usersCollection.insert(new BasicDBObject(userMap))
        client.handler.failure = { it }
    }

    def "should return 201 and session when new session has been created"() {
        when: 'sending correct login and password to service'
        def response = client.post(
                path: 'authToken/login',
                body: [username: USERNAME, password: PASSWORD],
                requestContentType: ContentType.JSON)
        then: "response is 201 with session information"
        response.status == 201
        response.data.sessionId != null
        and: "session gets created in the DB"
        sessionsCollection.count(new BasicDBObject('_id', response.data.sessionId)) == 1
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

    def "should remove session and return 204 when deleting session"() {
        given: 'session exists'
        HttpResponseDecorator createResponse = client.post(
                path: 'authToken/login',
                body: [username: USERNAME, password: PASSWORD],
                requestContentType: ContentType.JSON)
        def sessionId = createResponse.data.sessionId
        when:
        def response = client.delete(path: "authToken/$sessionId")
        then:
        response.status == 204
        sessionsCollection.findOne(new BasicDBObject('_id', sessionId)) == null
    }

    void cleanupSpec() {
        usersCollection.remove(new BasicDBObject('_id', USERNAME))
    }
}
