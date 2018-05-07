package com.revolut.test.web.init;

import com.revolut.test.repository.impl.InMemoryAccountRepository;
import com.revolut.test.service.AccountService;
import com.revolut.test.web.AccountController;

/**
 * Class for creating account controller with all dependencies.
 */
public class ControllerInitializer {

    public static AccountController initController() {
        return new AccountController(new AccountService(new InMemoryAccountRepository()));
    }
}
