package com.taskroo.authn.domain

import spock.lang.Specification

class UserCredentialsTest extends Specification {

    def "should throw exception when trying to create UserCredentials with empty username"() {
        when:
        new UserCredentials(null, 'pass')
        then:
        thrown(NullPointerException)

    }
}
