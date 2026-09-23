package com.multi.shop.api.multi_shop_api.security;

import com.multi.shop.api.multi_shop_api.users.controllers.UserController;
import com.multi.shop.api.multi_shop_api.users.mappers.UserMapperImpl;
import com.multi.shop.api.multi_shop_api.users.entities.User;
import com.multi.shop.api.multi_shop_api.users.enums.Field;
import com.multi.shop.api.multi_shop_api.users.repositories.UserRepository;
import com.multi.shop.api.multi_shop_api.users.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(OwnershipRulesTest.Config.class)
class OwnershipRulesTest {
    @Autowired
    private UserController userController;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        reset(userService, userRepository);
        User juan = user("juan-id", "juan@mail.com", 3001112233L);
        User ana = user("ana-id", "ana@mail.com", 3004445566L);
        when(userRepository.findById("juan-id")).thenReturn(Optional.of(juan));
        when(userRepository.findById("ana-id")).thenReturn(Optional.of(ana));
        when(userService.delete(any())).thenReturn(Optional.of(juan));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void aUserCanDeleteTheirOwnAccount() {
        signIn("juan@mail.com", "ROLE_USER");

        assertThatCode(() -> userController.delete("juan-id")).doesNotThrowAnyException();
    }

    @Test
    void aUserSignedInWithTheirPhoneIsAlsoTheOwner() {
        signIn("3001112233", "ROLE_USER");

        assertThatCode(() -> userController.delete("juan-id")).doesNotThrowAnyException();
    }

    @Test
    void aUserCannotTouchSomeoneElsesAccount() {
        signIn("juan@mail.com", "ROLE_USER");

        assertThatThrownBy(() -> userController.delete("ana-id")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> userController.updatePassword(null, "ana-id")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> userController.updateProfileImage("ana-id", null)).isInstanceOf(AccessDeniedException.class);
        verify(userService, never()).delete(any());
    }

    @Test
    void anAdminCanTouchAnyAccount() {
        signIn("admin@mail.com", "ROLE_USER", "ROLE_ADMIN");

        assertThatCode(() -> userController.delete("ana-id")).doesNotThrowAnyException();
    }

    @Test
    void onlyAnAdminCanSearchUsersOrSeeTheirStats() {
        signIn("juan@mail.com", "ROLE_USER");

        assertThatThrownBy(() -> userController.userSearch(Pageable.unpaged(), "a", false, null, Field.NAME))
            .isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> userController.userStats(false)).isInstanceOf(AccessDeniedException.class);
    }

    private void signIn(String identity, String... roles) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            identity, null, List.of(roles).stream().map(SimpleGrantedAuthority::new).toList()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static User user(String id, String email, Long phone) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        return user;
    }

    @Configuration
    @EnableMethodSecurity
    static class Config {
        @Bean
        UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        UserRepository userRepository() {
            return mock(UserRepository.class);
        }

        @Bean("authz")
        AuthorizationChecks authorizationChecks(UserRepository userRepository) {
            return new AuthorizationChecks(userRepository);
        }

        @Bean
        UserController userController(UserService userService) {
            return new UserController(userService, new UserMapperImpl());
        }
    }
}
