package com.taskroo.authn.service
import com.taskroo.authn.data.SessionDao
import com.taskroo.authn.data.UserDao
import spock.lang.Specification

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
