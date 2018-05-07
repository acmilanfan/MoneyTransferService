package com.revolut.test.repository.impl;

import com.revolut.test.model.Account;
import com.revolut.test.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;

/**
 * Simple in memory implementation of AccountRepository interface.
 */
public class InMemoryAccountRepository implements AccountRepository {

    private Map<Long, Account> accountsDatabase = new HashMap<>();

    public InMemoryAccountRepository() {
        createTestAccounts();
    }

    private void createTestAccounts() {
        Account firstAccount = Account.of(1L, BigDecimal.valueOf(1000));
        accountsDatabase.put(firstAccount.getId(), firstAccount);

        Account secondAccount = Account.of(2L, BigDecimal.valueOf(100));
        accountsDatabase.put(secondAccount.getId(), secondAccount);
    }

    @Override
    public Account findOneById(Long accountId) {
        return accountsDatabase.get(accountId);
    }

    @Override
    public void save(Account account) {
        Long accountId = account.getId();

        if (isNull(accountsDatabase.get(accountId))) {
            accountsDatabase.put(accountId, account);
        }
    }
}
