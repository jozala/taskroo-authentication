package pl.aetas.gtweb.authn.data;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.springframework.stereotype.Repository;
import pl.aetas.gtweb.authn.domain.Role;
import pl.aetas.gtweb.authn.domain.User;

import javax.inject.Inject;
import java.util.Objects;

@Repository
public class UserDao {
    private final DBCollection usersCollection;

    @Inject
    public UserDao(DBCollection usersCollection) {
        this.usersCollection = usersCollection;
    }

    public User findEnabled(String username, String encryptedPassword) {
        Objects.requireNonNull(username);
        Objects.requireNonNull(encryptedPassword);
        DBObject userDbObject = usersCollection.findOne(new BasicDBObject("_id", username)
                .append("password", encryptedPassword).append("enabled", true));
        if (userDbObject == null) {
            return null;
        }
        return mapUserDbObjectToUser(userDbObject);
    }

    private User mapUserDbObjectToUser(DBObject userDbObject) {

        User.UserBuilder userBuilder =
                User.UserBuilder.start()
                        .username(userDbObject.get("_id").toString())
                        .setEnabled((Boolean)userDbObject.get("enabled"))
                        .firstName(userDbObject.get("first_name").toString())
                        .lastName(userDbObject.get("last_name").toString())
                        .password(userDbObject.get("password").toString())
                        .email(userDbObject.get("email").toString());

        BasicDBList rolesStrings = (BasicDBList) userDbObject.get("roles");
        for (Object roleString : rolesStrings) {
            Role role = Role.valueOf(roleString.toString());
            userBuilder.role(role);
        }

        return userBuilder.build();

    }
}
