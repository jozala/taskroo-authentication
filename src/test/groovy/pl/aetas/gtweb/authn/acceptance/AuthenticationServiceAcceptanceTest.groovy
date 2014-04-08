package pl.aetas.gtweb.authn.acceptance
import com.mongodb.BasicDBObject
import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import pl.aetas.gtweb.mongo.MongoConnector
import spock.lang.Specification

class AuthenticationServiceAcceptanceTest extends Specification {


    static final String USERNAME = 'someUsername'
    static final String PASSWORD = 'secretPass'

    static RESTClient client = new RESTClient('http://localhost:8080/')
    static MongoConnector mongoConnector = new MongoConnector('mongodb://localhost')

    def setupSpec() {
        def userMap = [_id: USERNAME, password: PASSWORD, enabled: true, first_name: 'fname', last_name: 'lname',
                       email: 'email@aetas.pl', roles: ['USER']]
        mongoConnector.gtWebDatabase.getCollection('users').insert(new BasicDBObject(userMap))
        client.handler.failure = { it }
    }

    def "should return 201 and session when new session has been created"() {
        when: 'sending correct login and password to service'
        HttpResponseDecorator response = client.post(
                path: 'session',
                body: [username: USERNAME, password: PASSWORD],
                requestContentType: ContentType.URLENC)
        then:
        response.status == 201
        response.data.sessionId != null
    }

    def "should return 401 (unauthorized) when incorrect user details sent"() {
        when: 'sending incorrect login and password to service'
        HttpResponseDecorator response = client.post(
                path: 'session',
                body: [username: 'someName', password: 'incorrect'],
                requestContentType: ContentType.URLENC)
        then:
        response.status == 401
    }

    def "should remove session and return 204 when deleting session"() {
        given: 'session exists'
        HttpResponseDecorator createResponse = client.post(
                path: 'session',
                body: [username: USERNAME, password: PASSWORD],
                requestContentType: ContentType.URLENC)
        def sessionId = createResponse.data.sessionId
        when:
        def response = client.delete(path: "session/$sessionId")
        then:
        response.status == 204
        mongoConnector.gtWebDatabase.getCollection('sessions').findOne(new BasicDBObject('_id', sessionId)) == null
    }

    void cleanupSpec() {
        mongoConnector.gtWebDatabase.getCollection('users').remove(new BasicDBObject('_id', USERNAME))
    }
}
