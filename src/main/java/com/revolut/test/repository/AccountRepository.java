package com.revolut.test.repository;

import com.revolut.test.model.Account;

/**
 * Interface for account repositories.
 */
public interface AccountRepository {

    /**
     * Retrieve account by id.
     *
     * @param accountId id of account
     * @return account entity
     */
    Account findOneById(Long accountId);

    /**
     * Save new account in database.
     *
     * @param account account entity
     */
    void save(Account account);
}
