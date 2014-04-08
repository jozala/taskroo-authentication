package pl.aetas.gtweb.authn.data

import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import com.mongodb.DBCollection
import pl.aetas.gtweb.authn.domain.Role
import spock.lang.Specification

class UserDaoTest extends Specification {

    UserDao userDao

    // mocks
    DBCollection usersCollection

    void setup() {
        usersCollection = Mock(DBCollection)
        userDao = new UserDao(usersCollection)

    }

    def "should retrieve from db enabled customers with matching username and password"() {
        given:
        BasicDBList rolesList = new BasicDBList()
        rolesList << 'ADMIN' << 'USER'
        when:
        userDao.findEnabled('someUsername', 'secretPass')
        then:
        1 * usersCollection.findOne(new BasicDBObject([_id: 'someUsername', password: 'secretPass', enabled: true])) >>
                new BasicDBObject([_id: 'someUsername', password: 'givenPassword', enabled: true,
                                   first_name: 'Fnametest', last_name: 'Lnametest', email: 'some@email.co.uk',
                                   roles: new BasicDBList() + ['ADMIN', 'USER']])

    }

    def "should retrieve customer with mapped properties"() {
        given:
        usersCollection.findOne(new BasicDBObject([_id: 'someUsername', password: 'secretPass', enabled: true])) >>
                new BasicDBObject([_id: 'someUsername', password: 'givenPassword', enabled: true,
                                   first_name: 'Fnametest', last_name: 'Lnametest', email: 'some@email.co.uk',
                                   roles: new BasicDBList() + ['ADMIN', 'USER']])

        when:
        def user = userDao.findEnabled('someUsername', 'secretPass')
        then:
        user.username == 'someUsername'
        user.email == 'some@email.co.uk'
        user.enabled
        user.firstName == 'Fnametest'
        user.lastName == 'Lnametest'
        user.roles == [Role.ADMIN, Role.USER] as Set
    }

    def "should return null when customer has not been found"() {
        given:
        usersCollection.findOne(_) >> null
        when:
        def user = userDao.findEnabled('nonExistingCustomer', 'somePassword')
        then:
        user == null
    }
}
