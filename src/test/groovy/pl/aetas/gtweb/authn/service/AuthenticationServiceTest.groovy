package pl.aetas.gtweb.authn.service
import pl.aetas.gtweb.authn.data.SessionDao
import pl.aetas.gtweb.authn.data.UserDao
import pl.aetas.gtweb.authn.domain.Session
import pl.aetas.gtweb.authn.domain.User
import spock.lang.Specification

import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response

class AuthenticationServiceTest extends Specification {

    AuthenticationService authenticationService

    // mocks
    SessionDao sessionDao
    UserDao userDao

    def setup() {
        sessionDao = Mock(SessionDao)
        userDao = Mock(UserDao)
        authenticationService = new AuthenticationService(sessionDao, userDao)
    }

    def "should create session in DB when username and password are correct"() {
        given:
        userDao.findEnabled('goodUsername', 'secretPass') >> Mock(User)
        when:
        authenticationService.login('goodUsername', 'secretPass')
        then:
        1 * sessionDao.create('goodUsername') >> Mock(Session)
    }

    def "should return response with session when session has been created"() {
        given:
        userDao.findEnabled('goodUsername', 'secretPass') >> Mock(User)
        def expectedSession = Mock(Session)
        sessionDao.create('goodUsername') >> expectedSession
        when:
        Response response = authenticationService.login('goodUsername', 'secretPass')
        then:
        response.getStatus() == 201
        response.getEntity() == expectedSession
    }

    def "should throw exception when user with given password has not been found"() {
        given:
        userDao.findEnabled('goodUsername', 'secretPass') >> null
        when:
        authenticationService.login('goodUsername', 'secretPass')
        then:
        thrown(WebApplicationException)
    }

    def "should remove session when logout"() {
        when:
        authenticationService.logout('someSessionId')
        then:
        1 * sessionDao.remove('someSessionId')
    }

    def "should return response with no content (204) code when logout"() {
        when:
        def response = authenticationService.logout('someSessionId')
        then:
        response.status == 204
    }
}
