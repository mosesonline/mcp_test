package de.mosesonline.mcptest.mcptest.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * To have this class feels really bad, but it works.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class OAuthProtectedResourceFilter extends OncePerRequestFilter {

    private static final String ENDPOINT = "/.well-known/oauth-protected-resource";

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (ENDPOINT.equals(request.getRequestURI()) && !issuerUri.isBlank()) {
            String resource = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("""
                    {"resource":"%s","authorization_servers":["%s"],"bearer_methods_supported":["header"],"scopes_supported":["openid","mcp:tools"]}
                    """.formatted(resource, issuerUri));
            return;
        }
        chain.doFilter(request, response);
    }
}
