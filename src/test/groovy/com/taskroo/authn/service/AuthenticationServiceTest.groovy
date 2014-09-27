package com.taskroo.authn.service
import com.taskroo.authn.data.RememberMeTokenDao
import com.taskroo.authn.data.SecurityTokenDao
import com.taskroo.authn.data.UserDao
import com.taskroo.authn.domain.RememberMeToken
import com.taskroo.authn.domain.Role
import com.taskroo.authn.domain.User
import com.taskroo.authn.domain.UserCredentials
import spock.lang.Specification

import javax.ws.rs.WebApplicationException

class AuthenticationServiceTest extends Specification {

    AuthenticationService authenticationService;

    // mocks
    SecurityTokenDao securityTokenDao = Mock(SecurityTokenDao)
    UserDao userDao = Mock(UserDao)
    RememberMeTokenDao rememberMeTokenDao = Mock(RememberMeTokenDao)

    void setup() {
        authenticationService = new AuthenticationService(securityTokenDao, userDao, rememberMeTokenDao)
    }

    def "should throw WebAppException with 401 and information about disabled user when trying to login as a not enabled user"() {
        given:
        userDao.findByUsername('testUser1') >> Optional.of(User.UserBuilder.start().username('testUser1').role(Role.ADMIN).role(Role.USER)
                .email('test@example.com').firstName('Mariusz').lastName('Unknown').password('NJXnikEk49cgMAEIVcSe6w9BaIo=').salt('F8Sgh1uJmuld8J7t9R+JOgq+vn8=')
                .build())
        when:
        authenticationService.login(new UserCredentials('testUser1', 'pass', false))
        then:
        def e = thrown(WebApplicationException)
        e.response.status == 401
        e.response.entity.contains('User is not active')
    }

    def "should throw WebAppException with 401 and information about disabled user when trying to login as a not enabled user with remember me token"() {
        given:
        userDao.findByUsername('testUser1') >> Optional.of(User.UserBuilder.start().username('testUser1').role(Role.ADMIN).role(Role.USER)
                .email('test@example.com').firstName('Mariusz').lastName('Unknown').password('NJXnikEk49cgMAEIVcSe6w9BaIo=').salt('F8Sgh1uJmuld8J7t9R+JOgq+vn8=')
                .build())
        def rememberMeToken = RememberMeToken.createNew('testUser1')
        when:
        authenticationService.createSecurityTokenWithRememberMeToken(rememberMeToken.toString())
        then:
        def e = thrown(WebApplicationException)
        e.response.status == 401
        e.response.entity.contains('User is not active')
    }
}
