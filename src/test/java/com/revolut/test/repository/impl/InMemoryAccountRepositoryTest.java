package com.revolut.test.repository.impl;

import com.revolut.test.model.Account;
import com.revolut.test.repository.AccountRepository;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class InMemoryAccountRepositoryTest {

    private AccountRepository accountRepository = new InMemoryAccountRepository();

    @Test
    public void shouldReturnAccountById() {
        // given
        long accountId = 3L;
        Account account = Account.of(accountId, BigDecimal.ONE);
        accountRepository.save(account);

        // when
        Account foundAccount = accountRepository.findOneById(accountId);

        // then
        assertThat(foundAccount, notNullValue());
        assertThat(foundAccount.getId(), is(account.getId()));
        assertThat(foundAccount.getBalance(), is(account.getBalance()));
    }

    @Test
    public void shouldSaveAccount() {
        // given
        Account account = Account.of(4L, BigDecimal.TEN);

        // when
        accountRepository.save(account);

        // then
        Long accountId = account.getId();
        Account savedAccount = accountRepository.findOneById(accountId);

        assertThat(savedAccount, notNullValue());
        assertThat(savedAccount.getId(), is(accountId));
        assertThat(savedAccount.getBalance(), is(account.getBalance()));
    }
}