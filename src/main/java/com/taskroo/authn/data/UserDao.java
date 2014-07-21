package com.taskroo.authn.data;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.springframework.stereotype.Repository;
import com.taskroo.authn.domain.Role;
import com.taskroo.authn.domain.User;

import javax.inject.Inject;
import java.util.Objects;

@Repository
public class UserDao {

    public static final String ID_KEY = "_id";
    public static final String SALT_KEY = "salt";
    public static final String ENABLED_KEY = "enabled";
    public static final String FIRST_NAME_KEY = "first_name";
    public static final String LAST_NAME_KEY = "last_name";
    public static final String PASSWORD_KEY = "password";
    public static final String EMAIL_KEY = "email";
    public static final String ROLES_KEY = "roles";

    private final DBCollection usersCollection;

    @Inject
    public UserDao(DBCollection usersCollection) {
        this.usersCollection = usersCollection;
    }

    public User findByUsername(String username) {
        Objects.requireNonNull(username);
        DBObject userDbObject = usersCollection.findOne(new BasicDBObject(ID_KEY, username));
        if (userDbObject == null) {
            return null;
        }
        return mapUserDbObjectToUser(userDbObject);
    }

    private User mapUserDbObjectToUser(DBObject userDbObject) {

        User.UserBuilder userBuilder =
                User.UserBuilder.start()
                        .username(userDbObject.get(ID_KEY).toString())
                        .setEnabled((Boolean)userDbObject.get(ENABLED_KEY))
                        .firstName(userDbObject.get(FIRST_NAME_KEY).toString())
                        .lastName(userDbObject.get(LAST_NAME_KEY).toString())
                        .password(userDbObject.get(PASSWORD_KEY).toString())
                        .email(userDbObject.get(EMAIL_KEY).toString())
                        .salt(userDbObject.get(SALT_KEY).toString());

        BasicDBList rolesStrings = (BasicDBList) userDbObject.get(ROLES_KEY);
        for (Object roleString : rolesStrings) {
            Role role = Role.valueOf(roleString.toString());
            userBuilder.role(role);
        }

        return userBuilder.build();

    }
}
