package com.revolut.test.web;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.revolut.test.exception.IncorrectRequestDataException;
import com.revolut.test.exception.MoneyTransferException;
import com.revolut.test.exception.NotEnoughMoneyException;
import com.revolut.test.service.AccountService;
import com.revolut.test.web.json.TransferRequest;
import spark.Request;
import spark.Route;

import static com.revolut.test.constants.Paths.MONEY_TRANSFER_PATH;
import static com.revolut.test.exception.handler.ExceptionHandlers.*;
import static java.util.Objects.isNull;
import static spark.Spark.exception;
import static spark.Spark.put;

/**
 * Controller for handle account requests.
 */
public class AccountController {

    public static final Gson gson = new Gson();

    private AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;

        addRoutes();
        addExceptionHandlers();
    }

    private void addRoutes() {
        put(MONEY_TRANSFER_PATH, transferRoute, gson::toJson);
    }

    private void addExceptionHandlers() {
        exception(JsonSyntaxException.class, jsonSyntaxExceptionHandler);
        exception(MoneyTransferException.class, moneyTransferExceptionHandler);
        exception(NotEnoughMoneyException.class, notEnoughMoneyExceptionHandler);
        exception(IncorrectRequestDataException.class, incorrectRequestDataExceptionHandler);
    }

    private Route transferRoute = (request, response)
            -> accountService.moneyTransfer(getTransferRequest(request));

    private TransferRequest getTransferRequest(Request request) {
        TransferRequest transferRequest = gson.fromJson(request.body(), TransferRequest.class);

        if (isNull(transferRequest)) {
            throw new JsonSyntaxException("Empty request body!");
        }

        return transferRequest;
    }
}
