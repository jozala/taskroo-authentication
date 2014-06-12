package pl.aetas.gtweb.authn.data

import com.mongodb.BasicDBObject
import pl.aetas.gtweb.authn.domain.Role

class UserDaoTest extends DaoTestBase {

    UserDao userDao

    void setup() {
        cleanup()
        userDao = new UserDao(usersCollection)
    }

    void cleanup() {
        usersCollection.drop()
    }

    def "should retrieve from db customer with matching username and password"() {
        given:
        usersCollection.insert(new BasicDBObject([_id: 'someUsername', password: 'givenPassword', enabled: true,
                                                  first_name: 'Fnametest', last_name: 'Lnametest', email: 'some@email.co.uk',
                                                  roles: ['ADMIN', 'USER'], salt: 'someSaltValue']))
        when:
        def user = userDao.findByUsername('someUsername')
        then:
        user.name == 'someUsername'
        user.password == 'givenPassword'
        user.enabled
        user.firstName == 'Fnametest'
        user.lastName == 'Lnametest'
        user.email == 'some@email.co.uk'
        user.roles == [Role.ADMIN, Role.USER].toSet()
        user.salt == 'someSaltValue'

    }

    def "should return customer when customer with given username exists"() {
        given:
        usersCollection.insert(new BasicDBObject([_id: 'someUsername', password: 'givenPassword', enabled: true,
                                                  first_name: 'Fnametest', last_name: 'Lnametest', email: 'some@email.co.uk',
                                                  roles: ['ADMIN', 'USER'], salt: 'someSaltValue']))
        when:
        def user = userDao.findByUsername('someUsername')
        then:
        user.name == 'someUsername'
    }

    def "should return null when customer with given username does not exists"() {
        when:
        def user = userDao.findByUsername('nonExistingCustomer')
        then:
        user == null
    }
}
