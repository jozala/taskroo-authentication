package com.taskroo.authn.domain

import org.apache.commons.codec.binary.Base64
import spock.lang.Specification

import java.security.MessageDigest

class RememberMeTokenTest extends Specification {

    def "should create concatenated username and key with separator character when converting to string"() {
        when:
        def rememberMeToken = new RememberMeToken('someUsername', 'someRandomString')
        def rememberMeTokenString = rememberMeToken.toString()
        then:
        rememberMeTokenString == 'someUsername:someRandomString'
    }

    def "should create remember me token with long 32 characters long string as a key"() {
        when:
        def rememberMeToken = RememberMeToken.createNew('testUser')
        then:
        rememberMeToken.getKey().length() == 32
    }

    def "should hash key with SHA-1"() {
        given:
        def notHashedKey = '12345678901234567890123456789012'
        def rememberMeToken = new RememberMeToken('testUser', notHashedKey)
        when:
        def hashedKey = rememberMeToken.getHashedKey()
        then:
        hashedKey ==  Base64.encodeBase64String(MessageDigest.getInstance("SHA-1").digest(Base64.decodeBase64(notHashedKey)))
    }

    def "should create token from string with username and key separated by colon"() {
        given:
        String rememberMeTokenString = "user123:qwertyuiopasdfghjklABzxcvbnmqwer"
        when:
        def rememberMeToken = RememberMeToken.fromString(rememberMeTokenString)
        then:
        rememberMeToken.username == 'user123'
        rememberMeToken.key == 'qwertyuiopasdfghjklABzxcvbnmqwer'
    }

    def "should throw illegal format exception when token key is less then 32 characters long"() {
        given:
        String rememberMeTokenString = "user123:tooShortTokenKey"
        when:
        RememberMeToken.fromString(rememberMeTokenString)
        then:
        thrown(IllegalArgumentException)
    }

    def "should throw exception when token string does not contains colon"() {
        given:
        String rememberMeTokenString = "user123;qwertyuiopasdfghjklABzxcvbnmqwer"
        when:
        RememberMeToken.fromString(rememberMeTokenString)
        then:
        thrown(IllegalArgumentException)
    }
}
