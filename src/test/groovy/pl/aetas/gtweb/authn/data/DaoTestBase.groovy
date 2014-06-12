package pl.aetas.gtweb.authn.data
import com.mongodb.DB
import com.mongodb.DBCollection
import pl.aetas.gtweb.mongo.MongoConnector
import spock.lang.Specification

class DaoTestBase extends Specification {

    private static final DB db = new MongoConnector('mongodb://localhost').getDatabase('gtweb-dao-tests-db')
    public static final DBCollection usersCollection = db.getCollection('users')
    public static final DBCollection sessionsCollection = db.getCollection('sessions')
}
