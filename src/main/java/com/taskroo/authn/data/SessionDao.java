package com.taskroo.authn.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.taskroo.authn.domain.Session;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Repository
public class SessionDao {

    private static final String USER_ID_KEY = "user_id";
    private static final String ID_KEY = "_id";
    private static final String ROLES_KEY = "roles";
    private static final String CREATE_TIME_KEY = "create_time";
    private static final String LAST_ACCESSED_TIME_KEY = "last_accessed_time";
    private static final String REMEMBERME_TOKEN_KEY = "rememberme_token";

    private DBCollection sessionsCollection;

    @Inject
    public SessionDao(DBCollection sessionsCollection) {
        this.sessionsCollection = sessionsCollection;
    }

    public void remove(String sessionId) {
        sessionsCollection.remove(new BasicDBObject(ID_KEY, sessionId));
    }

    public void insert(Session session) {
        BasicDBObject sessionDbObject = new BasicDBObject(ID_KEY, session.getSessionId())
                .append(ROLES_KEY, session.getUserRoles())
                .append(USER_ID_KEY, session.getUserId())
                .append(CREATE_TIME_KEY, session.getCreateTime())
                .append(LAST_ACCESSED_TIME_KEY, session.getCreateTime());

        if (session.getRememberMeToken() != null) {
            sessionDbObject.append(REMEMBERME_TOKEN_KEY, session.getRememberMeToken());
        }

        sessionsCollection.insert(sessionDbObject);

    }

    public Session findById(String sessionId) throws NonExistingResourceOperationException {
        DBObject sessionDbObject = sessionsCollection.findOne(new BasicDBObject(ID_KEY, sessionId));
        if (sessionDbObject == null) {
            throw new NonExistingResourceOperationException("Session not found in DB");
        }
        String userId = sessionDbObject.get(USER_ID_KEY).toString();
        List<Integer> userRoles = (List<Integer>) sessionDbObject.get(ROLES_KEY);
        Date createTime = (Date) sessionDbObject.get(CREATE_TIME_KEY);
        Date lastAccessedTime = (Date) sessionDbObject.get(LAST_ACCESSED_TIME_KEY);
        String rememberMeToken = null;
        if (sessionDbObject.get(REMEMBERME_TOKEN_KEY) != null) {
            rememberMeToken = sessionDbObject.get(REMEMBERME_TOKEN_KEY).toString();
        }

        return new Session(sessionDbObject.get(ID_KEY).toString(),
                userId, new HashSet<>(userRoles), createTime, lastAccessedTime, rememberMeToken);
    }
}
