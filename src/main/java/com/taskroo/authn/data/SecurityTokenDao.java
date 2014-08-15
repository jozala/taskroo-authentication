package com.taskroo.authn.data;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.taskroo.authn.domain.SecurityToken;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

@Repository
public class SecurityTokenDao {

    private static final String USER_ID_KEY = "user_id";
    private static final String ID_KEY = "_id";
    private static final String ROLES_KEY = "roles";
    private static final String CREATE_TIME_KEY = "create_time";
    private static final String LAST_ACCESSED_TIME_KEY = "last_accessed_time";
    private static final String REMEMBERME_TOKEN_KEY = "rememberme_token";

    private DBCollection securityTokensCollection;

    @Inject
    public SecurityTokenDao(DBCollection securityTokensCollection) {
        this.securityTokensCollection = securityTokensCollection;
    }

    public void remove(String securityTokenId) {
        securityTokensCollection.remove(new BasicDBObject(ID_KEY, securityTokenId));
    }

    public void insert(SecurityToken securityToken) {
        BasicDBObject securityTokenDbObject = new BasicDBObject(ID_KEY, securityToken.getId())
                .append(ROLES_KEY, securityToken.getUserRoles())
                .append(USER_ID_KEY, securityToken.getUserId())
                .append(CREATE_TIME_KEY, securityToken.getCreateTime())
                .append(LAST_ACCESSED_TIME_KEY, securityToken.getCreateTime());

        if (securityToken.getRememberMeToken() != null) {
            securityTokenDbObject.append(REMEMBERME_TOKEN_KEY, securityToken.getRememberMeToken());
        }

        securityTokensCollection.insert(securityTokenDbObject);

    }

    public SecurityToken findById(String securityTokenId) throws NonExistingResourceOperationException {
        DBObject securityTokenDbObject = securityTokensCollection.findOne(new BasicDBObject(ID_KEY, securityTokenId));
        if (securityTokenDbObject == null) {
            throw new NonExistingResourceOperationException("Security token not found in DB");
        }
        String userId = securityTokenDbObject.get(USER_ID_KEY).toString();
        List<Integer> userRoles = (List<Integer>) securityTokenDbObject.get(ROLES_KEY);
        Date createTime = (Date) securityTokenDbObject.get(CREATE_TIME_KEY);
        Date lastAccessedTime = (Date) securityTokenDbObject.get(LAST_ACCESSED_TIME_KEY);
        String rememberMeToken = null;
        if (securityTokenDbObject.get(REMEMBERME_TOKEN_KEY) != null) {
            rememberMeToken = securityTokenDbObject.get(REMEMBERME_TOKEN_KEY).toString();
        }

        return new SecurityToken(securityTokenDbObject.get(ID_KEY).toString(),
                userId, new HashSet<>(userRoles), createTime, lastAccessedTime, rememberMeToken);
    }
}
