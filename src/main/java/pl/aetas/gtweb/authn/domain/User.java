package pl.aetas.gtweb.authn.domain;

import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class User implements Principal {

    private final String username;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String password;
    private final Set<Role> roles;
    private final boolean enabled;
    private final String salt;

    private User(String username, String email, String firstName, String lastName, String password, Set<Role> roles,
                 boolean enabled, String salt) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.roles = Collections.unmodifiableSet(roles);
        this.enabled = enabled;
        this.salt = salt;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPassword() {
        return password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getName() {
        return getUsername();
    }

    public String getSalt() {
        return salt;
    }

    public static class UserBuilder {

        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String password;
        private Set<Role> roles = new HashSet<>();
        private boolean enabled;
        private String salt;

        private UserBuilder() {
            // use factory method start() instead
        }

        public static UserBuilder start() {
            return new UserBuilder();
        }

        public UserBuilder username(final String username) {

            this.username = username;
            return this;
        }

        public UserBuilder firstName(final String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserBuilder lastName(final String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserBuilder password(final String password) {
            this.password = password;
            return this;
        }

        public UserBuilder email(final String email) {
            this.email = email;
            return this;
        }

        public UserBuilder role(final Role role) {
            this.roles.add(role);
            return this;
        }

        public UserBuilder setEnabled(final boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public UserBuilder salt(String salt) {
            this.salt = salt;
            return this;
        }

        public User build() {
            requireNonNull(username, "Username has to be specified first. Actual [" + username + "]");
            requireNonNull(email, "E-mail has to be specified first. Actual [" + email + "]");
            requireNonNull(firstName, "First name has to be specified first. Actual [" + firstName + "]");
            requireNonNull(lastName, "Last name has to be specified first. Actual [" + lastName + "]");
            requireNonNull(password, "Password has to be specified first. Actual [" + password + "]");
            requireNonNull(roles, "Roles has to be specified first. Actual [" + roles + "]");
            requireNonNull(salt, "Salt has to be specified first. Actual [" + salt + "]");
            return new User(username, email, firstName, lastName, password, roles, enabled, salt);
        }

    }

}
