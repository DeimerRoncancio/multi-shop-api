package com.multi.shop.api.multi_shop_api.users.repositories;

import com.multi.shop.api.multi_shop_api.users.entities.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserRepositoryIdentityTest {
    private final UserRepository repository = mock(UserRepository.class);

    UserRepositoryIdentityTest() {
        doCallRealMethod().when(repository).findByIdentity(any());
    }

    @Test
    void findsByEmailWhenTheIdentityIsAnEmail() {
        User user = new User();
        when(repository.findByEmail("juan@mail.com")).thenReturn(Optional.of(user));

        assertThat(repository.findByIdentity("juan@mail.com")).containsSame(user);
        verify(repository, never()).findByPhoneNumber(any());
    }

    @Test
    void findsByPhoneWhenTheIdentityIsOnlyDigits() {
        User user = new User();
        when(repository.findByPhoneNumber(3001112233L)).thenReturn(Optional.of(user));

        assertThat(repository.findByIdentity("3001112233")).containsSame(user);
        verify(repository, never()).findByEmail(any());
    }

    @Test
    void findsNobodyWithoutAnIdentity() {
        assertThat(repository.findByIdentity(null)).isEmpty();
        assertThat(repository.findByIdentity(" ")).isEmpty();
    }
}
