package pl.aetas.gtweb.authn.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.springframework.stereotype.Repository;
import pl.aetas.gtweb.authn.domain.Session;

import javax.inject.Inject;
import java.util.Date;
import java.util.UUID;

@Repository
public class SessionDao {

    private DBCollection sessionsCollection;

    @Inject
    public SessionDao(DBCollection sessionsCollection) {
        this.sessionsCollection = sessionsCollection;
    }

    // TODO create Session always with TTL for 30 minutes
    public Session create(String username) {
        String sessionId = UUID.randomUUID().toString();
        BasicDBObject sessionDbObject = new BasicDBObject("_id", sessionId)
                .append("user_id", username)
                .append("create_time", new Date())
                .append("last_accessed_time", new Date());

        sessionsCollection.insert(sessionDbObject);

        return mapDbObjectToSession(sessionDbObject);
    }

    private Session mapDbObjectToSession(DBObject sessionDbObject) {
        String userId = sessionDbObject.get("user_id").toString();
        Date createTime = (Date) sessionDbObject.get("create_time");
        Date lastAccessedTime = (Date) sessionDbObject.get("last_accessed_time");

        Session session = new Session();
        session.setSessionId(sessionDbObject.get("_id").toString());
        session.setUserId(userId);
        session.setCreateTime(createTime);
        session.setLastAccessedTime(lastAccessedTime);

        return session;
    }

    public void remove(String sessionId) {
        sessionsCollection.remove(new BasicDBObject("_id", sessionId));
    }
}
