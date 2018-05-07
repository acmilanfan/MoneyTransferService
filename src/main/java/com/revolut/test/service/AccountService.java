package com.revolut.test.service;

import com.revolut.test.exception.IncorrectRequestDataException;
import com.revolut.test.exception.MoneyTransferException;
import com.revolut.test.exception.NotEnoughMoneyException;
import com.revolut.test.model.Account;
import com.revolut.test.repository.AccountRepository;
import com.revolut.test.web.json.TransferRequest;

import java.math.BigDecimal;

import static java.util.Objects.isNull;

/**
 * Service for working with accounts.
 */
public class AccountService {

    public static final String OK_RESPONSE = "OK";

    private AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Method for transferring money.
     *
     * @param transferRequest transfer request with required information
     * @return response message
     */
    public String moneyTransfer(TransferRequest transferRequest) {
        Account from = accountRepository.findOneById(transferRequest.getFromAccountId());
        Account to = accountRepository.findOneById(transferRequest.getToAccountId());
        BigDecimal amount = transferRequest.getAmount();

        checkRequestData(from, to, amount);
        checkAvailableMoney(from, amount);

        BigDecimal fromBalance = from.getBalance();
        BigDecimal toBalance = to.getBalance();

        try {
            changeBalance(from, to, amount);
        } catch (RuntimeException e) {
            rollbackAnyChanges(from, to, fromBalance, toBalance);
            throw new MoneyTransferException();
        }

        return OK_RESPONSE;
    }

    private void checkRequestData(Account from, Account to, BigDecimal amount) {
        if (isDataNotCorrect(from, to, amount)) {
            throw new IncorrectRequestDataException();
        }
    }

    private boolean isDataNotCorrect(Account from, Account to, BigDecimal amount) {
        return isNullOrHasNullBalance(from) || isNullOrHasNullBalance(to) || isNull(amount);
    }

    private boolean isNullOrHasNullBalance(Account account) {
        return isNull(account) || isNull(account.getBalance());
    }

    private void checkAvailableMoney(Account from, BigDecimal amount) {
        if (from.getBalance().compareTo(amount) < 0) {
            throw new NotEnoughMoneyException();
        }
    }

    /**
     * Synchronized (thread safe) method for transfer money from one account to another.
     *
     * @param from   the source account of money for transfer
     * @param to     the target account of transferring
     * @param amount amount of money for transfer
     */
    public synchronized void changeBalance(Account from, Account to, BigDecimal amount) {
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));
    }

    private void rollbackAnyChanges(Account from, Account to, BigDecimal fromBalance, BigDecimal toBalance) {
        from.setBalance(fromBalance);
        to.setBalance(toBalance);
    }
}
