package repo.provider;

import repo.constants.EModule;

import javax.annotation.Nullable;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.security.Principal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class BasicSecurityContextRequestFilter implements ContainerRequestFilter {

    private static final String BASIC = "Basic";

    /**
     * The beginning of the path
     */
    private static final String BEGINNING_PATH = "com/paymytable/sdk/";

    private final Map<String, User> userMap = new HashMap<>();

    public void add(User user) {
        userMap.put(user.authentication, user);
    }

    public void addAll(Collection<User> users) {
        for(User user: users) {
            add(user);
        }
    }

    @Override
    public void filter(ContainerRequestContext containerRequest) throws WebApplicationException {

        final String authorization = containerRequest.getHeaderString(HttpHeaders.AUTHORIZATION);

        System.out.println("authorization : "+authorization);
        if(authorization != null && authorization.startsWith(BASIC)) {
            final User user = userMap.get(authorization.substring(BASIC.length() + 1));
            final boolean secure = containerRequest.getUriInfo().getBaseUri().getScheme().equals("https");
            containerRequest.setSecurityContext(new BasicSecurityContext(user, secure));

            @Nullable EModule module = getModuleFromPath(containerRequest.getUriInfo().getPath());
            if (user != null) {
                if (!user.hasAccess(module)) {
                    containerRequest.abortWith(Response.status(Response.Status.FORBIDDEN)
                            .build());
                }
            } else {
                containerRequest.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                        .build());
            }
        }
    }

    private class BasicSecurityContext implements SecurityContext {
        private final User user;
        private final boolean secure;

        BasicSecurityContext(User user, boolean secure) {
            this.user = user;
            this.secure = secure;
        }

        @Override
        public Principal getUserPrincipal() {
            return user.principal;
        }

        @Override
        public boolean isUserInRole(String role) {
            return user.roles.contains(role);
        }

        @Override
        public boolean isSecure() {
            return secure;
        }

        @Override
        public String getAuthenticationScheme() {
            return BASIC_AUTH;
        }
    }

    /**
     * @param path the path of the library, is always like this "com/paymytable/sdk/[libraryName]/[version]/[libraryName]-[version].pom"
     *             with [libraryName] the name of the library (core, coreui etc) and [version] the version (1.0.0, 1.0.1 etc)
     * @return the module of the library
     */
    @Nullable
    private EModule getModuleFromPath(String path) {
        String libraryName = getLibraryNameFromPath(path);
        return EModule.create(libraryName);
    }

    /**
     * @param path the path of the library, is always like this "com/paymytable/sdk/[libraryName]/[version]/[libraryName]-[version].pom"
     *             with [libraryName] the name of the library (core, coreui etc) and [version] the version (1.0.0, 1.0.1 etc)
     * @return the name of the library
     */
    private String getLibraryNameFromPath(String path) {
        // We remove the beginning of the path
        String name = path.replace(BEGINNING_PATH, "");

        // We get the start of the path until the first "/"
        name = name.substring(0, name.indexOf("/"));

        return name;
    }

    /**
     * Contains exact word
     * https://stackoverflow.com/questions/25417363/java-string-contains-matches-exact-word
     */
    private boolean containsExact(String source, String subItem){
        String pattern = "\\b"+subItem+"\\b";
        Pattern p= Pattern.compile(pattern);
        Matcher m=p.matcher(source);
        return m.find();
    }
}