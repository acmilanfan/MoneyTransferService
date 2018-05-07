package com.revolut.test.web;

import com.despegar.http.client.HttpClientException;
import com.despegar.http.client.HttpResponse;
import com.despegar.http.client.PutMethod;
import com.despegar.sparkjava.test.SparkServer;
import com.revolut.test.repository.impl.InMemoryAccountRepository;
import com.revolut.test.service.AccountService;
import com.revolut.test.web.json.TransferRequest;
import org.junit.ClassRule;
import org.junit.Test;
import spark.servlet.SparkApplication;

import java.math.BigDecimal;

import static com.revolut.test.constants.ErrorDescriptions.*;
import static com.revolut.test.constants.Paths.MONEY_TRANSFER_PATH;
import static com.revolut.test.service.AccountService.OK_RESPONSE;
import static com.revolut.test.web.AccountController.gson;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class AccountControllerTest {

    private static AccountService accountService;

    public static class TestApplication implements SparkApplication {
        @Override
        public void init() {
            InMemoryAccountRepository accountRepository = new InMemoryAccountRepository();

            accountService = mock(AccountService.class,
                    withSettings()
                            .useConstructor(accountRepository)
                            .defaultAnswer(CALLS_REAL_METHODS));

            new AccountController(accountService);
        }
    }

    @ClassRule
    public static SparkServer<TestApplication> testServer =
            new SparkServer<>(AccountControllerTest.TestApplication.class, 4567);

    @Test
    public void shouldTransferMoney() throws HttpClientException {
        // given
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(1L);
        transferRequest.setToAccountId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(100));

        PutMethod put = testServer.put(MONEY_TRANSFER_PATH, gson.toJson(transferRequest), false);

        // when
        HttpResponse response = testServer.execute(put);

        // then
        assertThat(response.code(), is(SC_OK));
        assertThat(asString(response.body()), containsString(OK_RESPONSE));
    }

    @Test
    public void shouldBeErrorWhenNotEnoughMoneyToTransfer() throws HttpClientException {
        // given
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(2L);
        transferRequest.setToAccountId(1L);
        transferRequest.setAmount(BigDecimal.valueOf(1000));

        PutMethod put = testServer.put(MONEY_TRANSFER_PATH, gson.toJson(transferRequest), false);

        // when
        HttpResponse response = testServer.execute(put);

        // then
        assertThat(response.code(), is(SC_BAD_REQUEST));
        assertThat(asString(response.body()), containsString(NOT_ENOUGH_MONEY_ERROR_DESCRIPTION));
    }

    @Test
    public void shouldBeErrorWhenGotEmptyRequest() throws HttpClientException {
        // given
        PutMethod put = testServer.put(MONEY_TRANSFER_PATH, "", false);

        // when
        HttpResponse response = testServer.execute(put);

        // then
        assertThat(response.code(), is(SC_BAD_REQUEST));
        assertThat(asString(response.body()), containsString(RETRIEVING_DATA_ERROR_DESCRIPTION));
    }

    @Test
    public void shouldBeErrorWhenOccurExceptionWhileTransferringMoney() throws HttpClientException {
        // given
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromAccountId(1L);
        transferRequest.setToAccountId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(1000));

        doThrow(RuntimeException.class)
                .when(accountService).changeBalance(any(), any(), eq(transferRequest.getAmount()));

        PutMethod put = testServer.put(MONEY_TRANSFER_PATH, gson.toJson(transferRequest), false);

        // when
        HttpResponse response = testServer.execute(put);

        // then
        assertThat(response.code(), is(SC_BAD_REQUEST));
        assertThat(asString(response.body()), containsString(TRANSFERRING_MONEY_ERROR_DESCRIPTION));
    }

    private String asString(byte[] body) {
        return new String(body);
    }
}