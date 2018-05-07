package com.revolut.test.service;

import com.revolut.test.exception.MoneyTransferException;
import com.revolut.test.exception.NotEnoughMoneyException;
import com.revolut.test.model.Account;
import com.revolut.test.repository.AccountRepository;
import com.revolut.test.repository.impl.InMemoryAccountRepository;
import com.revolut.test.web.json.TransferRequest;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class AccountServiceTest {

    private AccountService accountService;
    private AccountRepository accountRepository;

    @Before
    public void setUp() throws Exception {
        accountRepository = new InMemoryAccountRepository();
        accountService = mock(AccountService.class,
                withSettings()
                        .useConstructor(accountRepository)
                        .defaultAnswer(CALLS_REAL_METHODS));
    }

    @Test
    public void shouldTransferMoney() {
        // given
        Account accountFrom = Account.of(3L, BigDecimal.valueOf(1000.66));
        accountRepository.save(accountFrom);
        Account accountTo = Account.of(4L, BigDecimal.valueOf(100.55));
        accountRepository.save(accountTo);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(accountFrom.getId());
        transferRequest.setToAccountId(accountTo.getId());
        transferRequest.setAmount(BigDecimal.TEN);

        BigDecimal transferAmount = transferRequest.getAmount();
        BigDecimal expectedFromBalance = accountFrom.getBalance().subtract(transferAmount);
        BigDecimal expectedToBalance = accountTo.getBalance().add(transferAmount);

        // when
        accountService.moneyTransfer(transferRequest);

        // then
        Account accountFromAfterTransfer = accountRepository.findOneById(accountFrom.getId());
        assertThat(accountFromAfterTransfer.getBalance(), is(expectedFromBalance));

        Account accountToAfterTransfer = accountRepository.findOneById(accountTo.getId());
        assertThat(accountToAfterTransfer.getBalance(), is(expectedToBalance));
    }

    @Test(expected = NotEnoughMoneyException.class)
    public void shouldThrowNotEnoughMoneyException() {
        // given
        Account accountFrom = Account.of(5L, BigDecimal.valueOf(5));
        accountRepository.save(accountFrom);
        Account accountTo = Account.of(6L, BigDecimal.valueOf(100));
        accountRepository.save(accountTo);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(accountFrom.getId());
        transferRequest.setToAccountId(accountTo.getId());
        transferRequest.setAmount(BigDecimal.TEN);

        // when
        accountService.moneyTransfer(transferRequest);
    }

    @Test(expected = MoneyTransferException.class)
    public void shouldRollbackChangesAndThrowMoneyTransferException() {
        // given
        Account accountFrom = Account.of(7L, BigDecimal.valueOf(100));
        accountRepository.save(accountFrom);
        Account accountTo = Account.of(8L, BigDecimal.valueOf(100));
        accountRepository.save(accountTo);

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(accountFrom.getId());
        transferRequest.setToAccountId(accountTo.getId());
        transferRequest.setAmount(BigDecimal.TEN);

        doThrow(RuntimeException.class)
                .when(accountService).changeBalance(accountFrom, accountTo, transferRequest.getAmount());

        // when
        accountService.moneyTransfer(transferRequest);
    }
}