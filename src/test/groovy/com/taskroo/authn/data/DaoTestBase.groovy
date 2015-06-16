package com.taskroo.authn.data
import com.mongodb.DB
import com.mongodb.DBCollection
import com.taskroo.mongo.MongoConnector
import spock.lang.Specification

class DaoTestBase extends Specification {

    private static final String MONGO_ADDR = System.getenv('MONGO_PORT_27017_TCP_ADDR') ?: 'localhost'
    private static final String MONGO_PORT = System.getenv('MONGO_PORT_27017_TCP_PORT') ?: '27017'
    private static final DB db = new MongoConnector(MONGO_ADDR, MONGO_PORT).getDatabase('taskroo-dao-tests-db')
    public static final DBCollection usersCollection = db.getCollection('users')
    public static final DBCollection securityTokensCollection = db.getCollection('securityTokens')
    public static final DBCollection rememberMeTokensCollection = db.getCollection('rememberMeTokens')
}
