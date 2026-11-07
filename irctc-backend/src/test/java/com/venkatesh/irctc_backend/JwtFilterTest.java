package com.venkatesh.irctc_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.venkatesh.irctc.config.JwtFilter;
import com.venkatesh.irctc.entity.User;
import com.venkatesh.irctc.repository.UserRepository;
import com.venkatesh.irctc.util.JwtUtil;

class JwtFilterTest {

    private final JwtUtil jwtUtil = new JwtUtil("local-test-secret-that-is-at-least-32-bytes");

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loadsUserRoleFromDatabaseForValidToken() throws Exception {
        User admin = new User("admin", "admin@example.com", "encoded", "ROLE_ADMIN");
        JwtFilter filter = filterFor(Optional.of(admin));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwtUtil.generateToken("admin"));
        AtomicBoolean chainContinued = new AtomicBoolean(false);

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            chainContinued.set(true);
            assertEquals("admin", SecurityContextHolder.getContext().getAuthentication().getName());
            assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream().anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
        });

        assertTrue(chainContinued.get());
    }

    @Test
    void ignoresMalformedTokenAndContinuesUnauthenticated() throws Exception {
        JwtFilter filter = filterFor(Optional.empty());
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer not-a-valid-token");
        AtomicBoolean chainContinued = new AtomicBoolean(false);

        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            chainContinued.set(true);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        });

        assertTrue(chainContinued.get());
    }

    private JwtFilter filterFor(Optional<User> user) {
        JwtFilter filter = new JwtFilter();
        ReflectionTestUtils.setField(filter, "jwtUtil", jwtUtil);
        UserRepository repository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(), new Class<?>[] { UserRepository.class },
                (proxy, method, args) -> {
                    if ("findByUsername".equals(method.getName())) {
                        return user;
                    }
                    if ("hashCode".equals(method.getName())) {
                        return System.identityHashCode(proxy);
                    }
                    if ("equals".equals(method.getName())) {
                        return proxy == args[0];
                    }
                    if ("toString".equals(method.getName())) {
                        return "Test UserRepository";
                    }
                    return null;
                });
        ReflectionTestUtils.setField(filter, "userRepository", repository);
        return filter;
    }
}
