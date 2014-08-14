package com.taskroo.authn.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.WriteResult;
import com.taskroo.authn.domain.RememberMeToken;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.Date;
import java.util.Objects;

@Repository
public class RememberMeTokenDao {

    private final DBCollection rememberMeTokensCollection;

    @Inject
    public RememberMeTokenDao(DBCollection rememberMeTokensCollection) {
        this.rememberMeTokensCollection = Objects.requireNonNull(rememberMeTokensCollection);
    }

    public void saveToken(RememberMeToken rememberMeToken) {
        Objects.requireNonNull(rememberMeToken);
        BasicDBObject tokenDb = new BasicDBObject("user_id", rememberMeToken.getUsername())
                .append("key", rememberMeToken.getHashedKey()).append("create_time", new Date());

        rememberMeTokensCollection.insert(tokenDb);
    }

    public boolean tokenExists(RememberMeToken rememberMeToken) {
        Objects.requireNonNull(rememberMeToken);
        long count = rememberMeTokensCollection.count(new BasicDBObject("user_id", rememberMeToken.getUsername())
                .append("key", rememberMeToken.getHashedKey()));
        return count > 0;
    }

    public void remove(RememberMeToken rememberMeToken) {
        WriteResult result = rememberMeTokensCollection.remove(new BasicDBObject("user_id", rememberMeToken.getUsername())
                .append("key", rememberMeToken.getHashedKey()));
        if (result.getN() == 0) {
            throw new IllegalStateException("Trying to remove non-existing rememberMeToken");
        }
    }
}
