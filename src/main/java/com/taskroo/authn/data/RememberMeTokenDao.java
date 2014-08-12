package com.taskroo.authn.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
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
        BasicDBObject tokenDb = new BasicDBObject("username", rememberMeToken.getUsername())
                .append("key", rememberMeToken.getHashedKey()).append("create_time", new Date());

        rememberMeTokensCollection.insert(tokenDb);
    }
}
