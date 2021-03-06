package repo.provider;

import com.google.common.base.Preconditions;
import com.google.common.io.BaseEncoding;
import repo.constants.EModule;
import repo.constants.Roles;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


final public class User {

    final Principal principal;
    final String authentication;
    final List<String> roles;
    final List<EModule> modules;

    private User(Builder builder) {
        final String basic = builder.username + ":" + builder.password;
        authentication = BaseEncoding.base64().encode(basic.getBytes(StandardCharsets.UTF_8));
        principal = builder.principal;
        roles = new ArrayList<>(builder.roles);
        modules = new ArrayList<>(builder.modules);
    }

    /**
     * @param module the module
     * @return true if the user has access to the module
     */
    boolean hasAccess(@Nullable EModule module) {
        // If the user can write, it can access all modules
        if (roles.contains(Roles.WRITE)) {
            return true;
        } else if (module != null) {
            return modules.contains(module);
        }
        return false;
    }

    final public static class Builder {
        private String username, password;
        private List<String> roles = new ArrayList<>();
        private Principal principal;
        private List<EModule> modules = new ArrayList<>();

        public Builder credentials(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        public Builder role(String role) {
            Preconditions.checkNotNull("role", role);
            this.roles.add(role);
            return this;
        }

        public Builder principal(Principal principal) {
            this.principal = principal;
            return this;
        }

        public Builder module(String module) {
            Preconditions.checkNotNull("module", module);
            this.modules.add(EModule.create(module));
            return this;
        }

        public User build() {
            Preconditions.checkNotNull("username", username);
            Preconditions.checkNotNull("password", username);

            if(principal == null) {
                principal = new BasicPrincipal(username);
            }

            return new User(this);
        }
    }

    private static class BasicPrincipal implements Principal {

        final String name;

        private BasicPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof BasicPrincipal && obj.hashCode() == hashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
