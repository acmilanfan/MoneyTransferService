package com.revolut.test.exception.handler;

import com.google.gson.JsonSyntaxException;
import com.revolut.test.exception.MoneyTransferException;
import com.revolut.test.exception.NotEnoughMoneyException;
import spark.ExceptionHandler;

import static com.revolut.test.constants.ErrorDescriptions.*;
import static com.revolut.test.web.AccountController.gson;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

/**
 * Exception handler functions for handle different exception classes.
 */
public class ExceptionHandlers {

    private static final String CONTENT_TYPE_JSON = "application/json";

    public static ExceptionHandler<JsonSyntaxException> jsonSyntaxExceptionHandler =
            (exception, request, response) -> {
                response.status(SC_BAD_REQUEST);
                response.type(CONTENT_TYPE_JSON);
                response.body(gson.toJson(RETRIEVING_DATA_ERROR_DESCRIPTION));
            };
    public static ExceptionHandler<MoneyTransferException> moneyTransferExceptionHandler =
            (exception, request, response) -> {
                response.status(SC_BAD_REQUEST);
                response.type(CONTENT_TYPE_JSON);
                response.body(gson.toJson(TRANSFERRING_MONEY_ERROR_DESCRIPTION));
            };

    public static ExceptionHandler<NotEnoughMoneyException> notEnoughMoneyExceptionHandler =
            (exception, request, response) -> {
                response.status(SC_BAD_REQUEST);
                response.type(CONTENT_TYPE_JSON);
                response.body(gson.toJson(NOT_ENOUGH_MONEY_ERROR_DESCRIPTION));
            };
}
