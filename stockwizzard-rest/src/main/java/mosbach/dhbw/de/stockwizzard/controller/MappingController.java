package mosbach.dhbw.de.stockwizzard.controller;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.*;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.AuthManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.PasswordManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.PortfolioManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.PortfolioStockManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.UserManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.SessionManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.StockManagerImplementation;
import mosbach.dhbw.de.stockwizzard.dataManagerImplementation.TransactionManagerImplementation;
import mosbach.dhbw.de.stockwizzard.model.LoginRequest;
import mosbach.dhbw.de.stockwizzard.model.StringAnswer;
import mosbach.dhbw.de.stockwizzard.model.TokenUser;
import mosbach.dhbw.de.stockwizzard.model.User;
import mosbach.dhbw.de.stockwizzard.model.Portfolio;
import mosbach.dhbw.de.stockwizzard.model.PortfolioStock;
import mosbach.dhbw.de.stockwizzard.model.PortfolioStockValue;
import mosbach.dhbw.de.stockwizzard.model.Stock;
import mosbach.dhbw.de.stockwizzard.model.Session;
import mosbach.dhbw.de.stockwizzard.model.AddStockRequest;
import mosbach.dhbw.de.stockwizzard.model.EditRequest;
import mosbach.dhbw.de.stockwizzard.model.TokenTransactionContent;
import mosbach.dhbw.de.stockwizzard.model.Transaction;
import mosbach.dhbw.de.stockwizzard.model.TransactionContent;
import mosbach.dhbw.de.stockwizzard.model.EditCurrentValueRequest;
import mosbach.dhbw.de.stockwizzard.model.alexa.*;

import java.util.logging.Level;
import java.util.logging.Logger;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class MappingController {

    TransactionManagerImplementation transactionManager = TransactionManagerImplementation.getTransactionManager();
    UserManagerImplementation userManager = UserManagerImplementation.getUserManager();
    AuthManagerImplementation authManager = AuthManagerImplementation.getAuthManager();
    PortfolioManagerImplementation portfolioManager = PortfolioManagerImplementation.getPortfolioManager();
    PasswordManagerImplementation passwordManager = PasswordManagerImplementation.getPasswordManager();
    SessionManagerImplementation sessionManager = SessionManagerImplementation.getSessionManager();
    StockManagerImplementation stockManager = StockManagerImplementation.getStockManager();
    PortfolioStockManagerImplementation portfolioStockManager = PortfolioStockManagerImplementation
            .getPortfolioStockManager();
    private final double EPSILON = 0.000001;

    ////////////////////////////////////////////////////////////// Auth
    ////////////////////////////////////////////////////////////// Endpoints////////////////////////////////////////////////////////////////////

    @PostMapping(path = "/auth", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String email = loginRequest.getEmail();
            String password = loginRequest.getPassword();
            User user = userManager.getUserProfile(email);

            if (user == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new StringAnswer("Please register first!"));
            }

            if (!passwordManager.checkPassword(password, user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Incorrect email or password!"));
            }

            String token = authManager.generateToken();
            sessionManager.createSession(user.getEmail(), token);

            return ResponseEntity.ok(new TokenUser(token, user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred during login."));
        }
    }

    @DeleteMapping(path = "/auth", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> logout(@RequestBody Session session) {
        try {
            String email = session.getEmail();
            String token = session.getToken();
            Boolean isValid = sessionManager.validToken(token, email);
            if (isValid) {
                sessionManager.deleteSession(email, token);
                return ResponseEntity.ok(new StringAnswer("Logout successfully!"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Unauthorized for this logout!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred during registration."));
        }
    }

    ////////////////////////////////////////////////////////////// User
    ////////////////////////////////////////////////////////////// Endpoints////////////////////////////////////////////////////////////////////

    @GetMapping("/user")
    public ResponseEntity<?> getUserProfile(
            @RequestParam(value = "email", defaultValue = "") String email,
            @RequestParam(value = "token", defaultValue = "") String token) {

        try {
            Boolean isValid = sessionManager.validToken(token, email);
            if (isValid) {
                User user = userManager.getUserProfile(email);
                if (user == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new StringAnswer("Please register first!"));
                } else {
                    return ResponseEntity.ok(user);
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Unauthorized for this transaction!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred while fetching user data."));
        }
    }

    @PostMapping(path = "/user", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            Boolean isRegistered = userManager.isEmailAlreadyRegistered(user.getEmail());
            if (isRegistered) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new StringAnswer("You are already registered!"));
            } else {
                userManager.addUser(user);
                portfolioManager.addPortfolio(new Portfolio(null, user.getBudget(), user.getBudget(), user.getEmail()));
                return ResponseEntity.ok(new StringAnswer("User successfully registered!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred while creating the profile."));
        }
    }

    @PutMapping(path = "/user", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> editProfile(@RequestBody EditRequest editRequest) {
        try {
            String token = editRequest.getToken();
            User currentUser = userManager.getUserProfile(editRequest.getCurrentMail());
            User new_user_data = editRequest.getUser();

            Boolean isValid = sessionManager.validToken(token, currentUser.getEmail());
            if (isValid) {
                Boolean emailChanged = !new_user_data.getEmail().equals(currentUser.getEmail());
                Portfolio userPortfolio = portfolioManager.getUserPortfolio(currentUser.getEmail());
                if (emailChanged == false) {
                    userManager.editProfile(currentUser, new_user_data);
                    portfolioManager.editAllPortfolioValues(currentUser.getEmail(), new_user_data.getEmail(),
                            (userPortfolio.getStartValue() + new_user_data.getBudget()),
                            (userPortfolio.getValue() + new_user_data.getBudget()));
                    return ResponseEntity.ok(userManager.getUserProfile(new_user_data.getEmail()));
                } else {
                    Boolean isRegistered = userManager.isEmailAlreadyRegistered(new_user_data.getEmail());
                    if (isRegistered == false) {
                        userManager.addUser(new User(new_user_data.getFirstName(), new_user_data.getLastName(),
                                new_user_data.getEmail(), currentUser.getPassword(),
                                (new_user_data.getBudget() + currentUser.getBudget())));
                        sessionManager.editSession(currentUser.getEmail(), new_user_data.getEmail());
                        transactionManager.editTransactionEmail(currentUser.getEmail(), new_user_data.getEmail());
                        portfolioManager.editAllPortfolioValues(currentUser.getEmail(), new_user_data.getEmail(),
                                (userPortfolio.getStartValue() + new_user_data.getBudget()),
                                (userPortfolio.getValue() + new_user_data.getBudget()));
                        userManager.deleteUser(currentUser.getEmail());
                        return ResponseEntity.ok(userManager.getUserProfile(new_user_data.getEmail()));
                    } else {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(new StringAnswer("This email is already registered"));
                    }
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Unauthorized for this transaction"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred during editing user."));
        }
    }

    @PutMapping(path = "/user/reset", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> resetProfile(@RequestBody Session session) {
        try {
            String token = session.getToken();
            String email = session.getEmail();
            Boolean isValid = sessionManager.validToken(token, email);
            if (isValid) {
                portfolioStockManager.deleteAllPortfolioStocks(email);
                transactionManager.deleteAllTransactions(email);
                portfolioManager.resetPortfolio(email);
                userManager.resetProfile(email);
                return ResponseEntity.ok(new StringAnswer("Profile successfully resetted!"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Unauthorized for this transaction!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred during resetting."));
        }
    }

    @DeleteMapping(path = "/user", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> deleteProfile(@RequestBody Session session) {
        try {
            String token = session.getToken();
            String email = session.getEmail();
            boolean isValid = sessionManager.validToken(token, email);
            if (isValid) {
                userManager.deleteUser(email);
                return ResponseEntity.ok(new StringAnswer("Profile successfully deleted"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Unauthorized for this transaction!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred during deleting the profile."));
        }
    }

    ////////////////////////////////////////////////////////////// Stock
    ////////////////////////////////////////////////////////////// Endpoints////////////////////////////////////////////////////////////////////

    // VLT IN Buy/SELL stock
    @GetMapping("/stock")
    public ResponseEntity<?> getStock(
            @RequestParam(value = "email", defaultValue = "") String email,
            @RequestParam(value = "token", defaultValue = "") String token,
            @RequestParam(value = "symbol", defaultValue = "") String symbol) {

        try {
            Boolean isValid = sessionManager.validToken(token, email);
            if (isValid) {
                Stock stock = stockManager.getStock(symbol);
                if (stock != null) {
                    return ResponseEntity.ok(stock);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new StringAnswer("Stock is not in the database."));
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Unauthorized for this transaction!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred during getting stock data."));
        }
    }

    @PostMapping(path = "/stock", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> createStock(@RequestBody AddStockRequest addStockRequest) {
        try {
            Boolean isValid = sessionManager.validToken(addStockRequest.getSession().getToken(),
                    addStockRequest.getSession().getEmail());
            if (isValid) {
                stockManager.addStock(addStockRequest.getStock());
                return ResponseEntity.ok(new StringAnswer("Stock got added to Database"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Unauthorized for this transaction!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred while adding Stock to Database."));
        }
    }

    ////////////////////////////////////////////////////////////// PortfolioStock
    ////////////////////////////////////////////////////////////// Endpoints////////////////////////////////////////////////////////////////////

    @GetMapping("/portfolioStocks")
    public ResponseEntity<?> getAllPortfolioStocks(
            @RequestParam(value = "email", defaultValue = "") String email,
            @RequestParam(value = "token", defaultValue = "") String token,
            @RequestParam(value = "sortby", defaultValue = "") String sortby) {

        try {
            Boolean isValid = sessionManager.validToken(token, email);
            if (isValid) {
                List<PortfolioStock> portfolioStocks = portfolioStockManager.getAllPortfolioStocks(email, sortby);
                editPortfolioValue(email);
                return ResponseEntity.ok(portfolioStocks);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Unauthorized for this transaction!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred during getting all user portfolioStocks."));
        }
    }

    @GetMapping("/portfolioStock")
    public ResponseEntity<?> getPortfolioStock(
            @RequestParam(value = "email", defaultValue = "") String email,
            @RequestParam(value = "token", defaultValue = "") String token,
            @RequestParam(value = "symbol", defaultValue = "") String symbol) {

        try {
            Boolean isValid = sessionManager.validToken(token, email);
            if (isValid) {
                PortfolioStock portfolioStock = portfolioStockManager.getPortfolioStock(email, symbol);
                if (portfolioStock != null) {
                    return ResponseEntity.ok(portfolioStock);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new StringAnswer("You don't have this stock in your portfolio."));
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Unauthorized for this transaction!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred during getting portfolioStocks."));
        }
    }

    // VLT IN BUY ORDER
    @PutMapping(path = "/portfolioStocks/currentValue", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> editCurrentValue(@RequestBody EditCurrentValueRequest editCurrentValueRequest) {
        try {
            Boolean isValid = sessionManager.validToken(editCurrentValueRequest.getToken(),
                    editCurrentValueRequest.getEmail());
            if (isValid) {
                Logger.getLogger("UpdateCurrentValueLogger").log(Level.INFO, "{0}",
                        editCurrentValueRequest.getNewValue());
                portfolioStockManager.editCurrentValue(editCurrentValueRequest.getEmail(),
                        editCurrentValueRequest.getSymbol(), editCurrentValueRequest.getNewValue());
                return ResponseEntity.ok(new StringAnswer("Current Value successfully updated"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Unauthorized for this transaction!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred during updating current value of stock."));
        }
    }
    ////////////////////////////////////////////////////////////// Transaction
    ////////////////////////////////////////////////////////////// Endpoints////////////////////////////////////////////////////////////////////

    @GetMapping("/transactions")
    public ResponseEntity<?> getAllTransactions(
            @RequestParam(value = "email", defaultValue = "") String email,
            @RequestParam(value = "token", defaultValue = "") String token,
            @RequestParam(value = "sortby", defaultValue = "") String sortby) {

        try {
            Boolean isValid = sessionManager.validToken(token, email);
            if (isValid) {
                List<Transaction> transactions = transactionManager.getAllTransactions(email, sortby);
                return ResponseEntity.ok(transactions);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Unauthorized for this transaction!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred during getting transactions."));
        }
    }

    // Just for testing purpose
    // @GetMapping("/Portfoliotransactions")
    // public ResponseEntity<?> getAllPortfolioTransactions(
    // @RequestParam(value = "email", defaultValue = "") String email,
    // @RequestParam(value = "token", defaultValue = "") String token) {

    // try {
    // Boolean isValid = sessionManager.validToken(token, email);
    // if (isValid) {
    // List<Transaction> transactions =
    // transactionManager.getAllTransactionsInPortfolioStock(email);
    // return ResponseEntity.ok(transactions);
    // } else {
    // return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    // .body(new StringAnswer("Unauthorized for this transaction!"));
    // }
    // } catch (Exception e) {
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body(new StringAnswer("An unexpected error occurred during getting
    // transactions."));
    // }
    // }

    // @GetMapping("/transaction")
    // public void getTransaction(@RequestParam(value = "transactionID",
    // defaultValue = "") Integer transactionID) {
    // // userManager.createUserTable();
    // // sessionManager.createSessionTable();
    // // portfolioManager.createPortfolioTable();
    // // stockManager.createStockTable();
    // // portfolioStockManager.createPortfolioStockTable();
    // // portfolioManager.createPortfolioTable();
    // transactionManager.createTransactionTable();
    // // stockManager.createStockTable();
    // //return transactionManager.getTransaction(transactionID);
    // // return new Transaction(null, null, null, null, null, null, null, null,
    // null);
    // }

    ////////////////////////////////////////////////////////////// Portfolio
    ////////////////////////////////////////////////////////////// Endpoints////////////////////////////////////////////////////////////////////

    @GetMapping("/portfolio")
    public ResponseEntity<?> getUserPortfolio(
            @RequestParam(value = "email", defaultValue = "") String email,
            @RequestParam(value = "token", defaultValue = "") String token) {

        try {
            Boolean isValid = sessionManager.validToken(token, email);
            if (isValid) {
                Portfolio portfolio = portfolioManager.getUserPortfolio(email);
                return ResponseEntity.ok(portfolio);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Unauthorized for this transaction!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred while getting the user portfolio."));
        }
    }

    private void editPortfolioValue(String email) {
        Double portfolioValue = 0.0;
        List<PortfolioStock> portfolioStocks = portfolioStockManager.getAllPortfolioStocks(email, "symbol");
        for (PortfolioStock portfolioStock : portfolioStocks) {
            portfolioValue += portfolioStock.getCurrentValue();
        }
        User user = userManager.getUserProfile(email);
        portfolioManager.editPortfolioValue(email, portfolioValue + user.getBudget());
    }

    ////////////////////////////////////////////////////////////// Order
    ////////////////////////////////////////////////////////////// Endpoints////////////////////////////////////////////////////////////////////

    @PostMapping(path = "/order", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> createBuyOrder(@RequestBody TokenTransactionContent tokenTransactionContent) {
        try {
            String token = tokenTransactionContent.getToken();
            TransactionContent transactionContent = tokenTransactionContent.getTransactionContent();
            Boolean isValid = sessionManager.validToken(token, transactionContent.getEmail());
            if (isValid) {
                User currentUser = userManager.getUserProfile(transactionContent.getEmail());
                Boolean enoughBudget = userManager.checkIfEnoughBudgetLeft(transactionContent.getTotalPrice(),
                        currentUser);
                if (enoughBudget == true) {
                    transactionManager.addTransaction(transactionContent);
                    Portfolio userPortfolio = portfolioManager.getUserPortfolio(transactionContent.getEmail());
                    portfolioStockManager.addPortfolioStock(userPortfolio.getPortfolioID(),
                            transactionContent.getSymbol(), transactionContent.getStockAmount(),
                            transactionContent.getTotalPrice());
                    userManager.editUserBudget(currentUser.getEmail(), currentUser.getBudget(),
                            transactionContent.getTotalPrice(), transactionContent.getTransactionType());
                    editPortfolioValue(currentUser.getEmail());
                    return ResponseEntity.ok(new StringAnswer("Transaction was successfully completed!"));
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new StringAnswer("Not enough budget for this transaction!"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Unauthorized for this transaction!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred while adding a new stock to the portfolio."));
        }
    }

    @PutMapping(path = "/order/buy", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> increasePortfolioStockOrder(@RequestBody TokenTransactionContent tokenTransactionContent) {
        try {
            String token = tokenTransactionContent.getToken();
            TransactionContent transactionContent = tokenTransactionContent.getTransactionContent();
            Boolean isValid = sessionManager.validToken(token, transactionContent.getEmail());
            if (isValid) {
                User currentUser = userManager.getUserProfile(transactionContent.getEmail());
                Boolean enoughBudget = userManager.checkIfEnoughBudgetLeft(transactionContent.getTotalPrice(),
                        currentUser);
                PortfolioStock portfolioStock = portfolioStockManager.getPortfolioStock(currentUser.getEmail(),
                        transactionContent.getSymbol());
                if (portfolioStock == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new StringAnswer("You don't own a position with the selected stock!"));
                } else {
                    if (enoughBudget == true) {
                        transactionManager.addTransaction(transactionContent);
                        Portfolio userPortfolio = portfolioManager.getUserPortfolio(transactionContent.getEmail());
                        portfolioStockManager.increasePortfolioStock(userPortfolio.getPortfolioID(),
                                transactionContent.getSymbol(), transactionContent.getStockAmount(),
                                transactionContent.getTotalPrice(), currentUser.getEmail());
                        userManager.editUserBudget(currentUser.getEmail(), currentUser.getBudget(),
                                transactionContent.getTotalPrice(), transactionContent.getTransactionType());
                        editPortfolioValue(currentUser.getEmail());
                        return ResponseEntity.ok(new StringAnswer("Transaction was successfully completed!"));
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new StringAnswer("Not enough budget for this transaction!"));
                    }
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Unauthorized for this transaction!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred while increasing the portfolio stock."));
        }
    }

    @PutMapping(path = "/order/sell", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> decreasePortfolioStockOrder(@RequestBody TokenTransactionContent tokenTransactionContent) {
        try {
            String token = tokenTransactionContent.getToken();
            TransactionContent transactionContent = tokenTransactionContent.getTransactionContent();
            Boolean isValid = sessionManager.validToken(token, transactionContent.getEmail());
            if (isValid) {
                User currentUser = userManager.getUserProfile(transactionContent.getEmail());
                PortfolioStockValue portfolioStockValues = portfolioStockManager.getPortfolioStockValues(
                        transactionContent.getTotalPrice(), currentUser.getEmail(), transactionContent.getSymbol());
                if (portfolioStockValues == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new StringAnswer("You don't own a position with the selected stock!"));
                } else {
                    if (portfolioStockValues.getCurrentValue() == -1) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new StringAnswer("Your stock position is not that high!"));
                    } else {
                        transactionManager.addTransaction(transactionContent);
                        Portfolio userPortfolio = portfolioManager.getUserPortfolio(transactionContent.getEmail());
                        List<Transaction> transactionsInPortfolio = transactionManager
                                .getAllTransactionsInPortfolioStock(transactionContent.getEmail());
                        Double remainingAmount = transactionContent.getTotalPrice();
                        Double totalBoughtValueReduction = 0.0;
                        for (Transaction transaction : transactionsInPortfolio) {
                            if (remainingAmount <= 0) {
                                break;
                            }
                            Double leftInTransaction = transaction.getLeftInPortfolio();
                            Double transactionBoughtValue = transaction.getTotalPrice();
                            Integer transactionId = transaction.getTransactionID();
                            if (remainingAmount >= leftInTransaction) {
                                remainingAmount -= leftInTransaction;
                                totalBoughtValueReduction += transactionBoughtValue;
                                transactionManager.editLeftinPortfolio(transactionId, 0.0);
                            } else {
                                Double proportion = remainingAmount / transactionBoughtValue;
                                Double reductionInBoughtValue = transactionBoughtValue * proportion;
                                totalBoughtValueReduction += reductionInBoughtValue;

                                Double newLeftInTransaction = leftInTransaction - remainingAmount;
                                remainingAmount = 0.0;
                                transactionManager.editLeftinPortfolio(transactionId, newLeftInTransaction);
                            }
                        }
                        Double newCurrentValue = portfolioStockValues.getCurrentValue()
                                - transactionContent.getTotalPrice();
                        Double newBoughtValue = portfolioStockValues.getBoughtValue() - totalBoughtValueReduction;
                        portfolioStockManager.decreasePortfolioStock(newCurrentValue, newBoughtValue,
                                transactionContent.getStockAmount(), userPortfolio.getPortfolioID(),
                                transactionContent.getSymbol());
                        userManager.editUserBudget(currentUser.getEmail(), currentUser.getBudget(),
                                transactionContent.getTotalPrice(), transactionContent.getTransactionType());
                        editPortfolioValue(currentUser.getEmail());
                        return ResponseEntity.ok(new StringAnswer("Transaction was successfully completed!"));
                    }
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Unauthorized for this transaction!"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred while decreasing the portfolio stock."));
        }
    }

    @DeleteMapping(path = "/order", consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> deletePortfolioStockOrder(@RequestBody TokenTransactionContent tokenTransactionContent) {
        try {
            String token = tokenTransactionContent.getToken();
            TransactionContent transactionContent = tokenTransactionContent.getTransactionContent();
            Boolean isValid = sessionManager.validToken(token, transactionContent.getEmail());
            if (isValid) {
                User currentUser = userManager.getUserProfile(transactionContent.getEmail());
                Logger.getLogger("GetPortfolioStockValuesLogger").log(Level.INFO, "Start sellStock{0}",
                        transactionContent.getTotalPrice());
                PortfolioStockValue portfolioStockValues = portfolioStockManager.getPortfolioStockValues(
                        transactionContent.getTotalPrice(), currentUser.getEmail(), transactionContent.getSymbol());
                if (portfolioStockValues == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new StringAnswer("You don't own a position with the selected stock!"));
                } else {
                    if (portfolioStockValues.getCurrentValue() == -1) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new StringAnswer("Your stock position is not that high!"));
                    } else {
                        transactionManager.addTransaction(transactionContent);
                        Portfolio userPortfolio = portfolioManager.getUserPortfolio(transactionContent.getEmail());
                        List<Transaction> transactionsInPortfolio = transactionManager
                                .getAllTransactionsInPortfolioStock(transactionContent.getEmail());
                        portfolioStockManager.deletePortfolioStock(transactionContent.getSymbol(),
                                userPortfolio.getPortfolioID());
                        for (Transaction transaction : transactionsInPortfolio) {
                            transactionManager.editLeftinPortfolio(transaction.getTransactionID(), 0.0);
                        }
                        userManager.editUserBudget(currentUser.getEmail(), currentUser.getBudget(),
                                transactionContent.getTotalPrice(), transactionContent.getTransactionType());
                        editPortfolioValue(currentUser.getEmail());
                        return ResponseEntity.ok(new StringAnswer("Transaction was successfully completed!"));
                    }
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new StringAnswer("Unauthorized for this transaction!"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new StringAnswer("An unexpected error occurred while deleting the portfolio stock."));
        }
    }

    ////////////////////////////////////////////////////// Alexa

    ////////////////////////////////////////////////////////////// ALEXA
    
    @PostMapping(path = "/alexa", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public AlexaRO handleAlexaRequest(@RequestBody AlexaRO alexaRO) {
        String requestType = alexaRO.getRequest().getType();
        String outText = "";
        boolean shouldEndSession = false;
        Map<String, Object> sessionAttributes = null;
    
        try {
            // Initialisiere Session-Attribute
            if (alexaRO.getSession() != null) {
                sessionAttributes = alexaRO.getSession().getAttributes();
            }
            if (sessionAttributes == null) {
                sessionAttributes = new HashMap<>();
            }
            Logger.getLogger("AlexaLogger").log(Level.INFO, "Session attributes at start: " + sessionAttributes);
    
            if (requestType.equalsIgnoreCase("LaunchRequest")) {
                outText = "Willkommen zu The Wallstreet Wizzard. Wie kann ich dir helfen?";
                Logger.getLogger("AlexaLogger").log(Level.INFO, "Handling LaunchRequest");
            } else if (requestType.equalsIgnoreCase("IntentRequest")) {
                IntentRO intent = alexaRO.getRequest().getIntent();
                String intentName = intent.getName();
                Logger.getLogger("AlexaLogger").log(Level.INFO, "Handling IntentRequest: " + intentName);
    
                if (intentName.equalsIgnoreCase("Login")) {
                    // Überprüfen, ob Slots für Benutzername und Passwort gesetzt sind
                    String email = intent.getSlots().get("email") != null ? intent.getSlots().get("email").getValue() : null;
                    String password = intent.getSlots().get("password") != null ? intent.getSlots().get("password").getValue() : null;
                
                    if (email != null) {
                        // Benutzername wurde erkannt und gespeichert
                        sessionAttributes.put("partialUsername", email);
                        Logger.getLogger("AlexaLogger").log(Level.INFO, "Username received and stored: " + email);
                
                        if (password == null) {
                            outText = "Benutzername erkannt. Bitte gib nun dein Passwort an.";
                        } else {
                            // Passwort ist ebenfalls vorhanden, Login kann abgeschlossen werden
                            LoginRequest loginRequest = new LoginRequest();
                            loginRequest.setEmail(email);
                            loginRequest.setPassword(password);
                            login(loginRequest);
                            outText = "Test";
                            // if (user != null && user.getPassword().equals(password)) {
                            //     sessionAttributes.put("userID", userId);
                            //     sessionAttributes.put("loggedIn", true);
                            //     outText = "Die Anmeldung war erfolgreich. Du bist nun angemeldet.";
                            //     Logger.getLogger("AlexaLogger").log(Level.INFO, "Login successful for user: " + userId);
                            // } else {
                            //     outText = "Anmeldung fehlgeschlagen. Überprüfe deine Benutzer-ID und dein Passwort.";
                            // }
                        }
                    } else if (password != null) {
                        // Passwort wurde angegeben, Benutzername jedoch nicht
                        if (sessionAttributes.get("partialUsername") != null) {
                            email = (String) sessionAttributes.get("partialUsername");
                            User user = UserManagerImplementation.getUserManager().getUserProfile(email);
                            if (user != null && user.getPassword().equals(password)) {
                                sessionAttributes.put("userID", email);
                                sessionAttributes.put("loggedIn", true);
                                outText = "Die Anmeldung war erfolgreich. Du bist nun angemeldet.";
                                Logger.getLogger("AlexaLogger").log(Level.INFO, "Login successful for user: " + email);
                                sessionAttributes.remove("partialUsername");
                            } else {
                                outText = "Anmeldung fehlgeschlagen. Überprüfe deine Benutzer-ID und dein Passwort.";
                            }
                        } else {
                            outText = "Bitte gib zuerst deinen Benutzernamen an.";
                        }
                    } else {
                        // Weder Benutzername noch Passwort wurden angegeben
                        outText = "Benutzername und Passwort wurden nicht erkannt. Bitte gib deinen Benutzernamen an.";
                    }
                    Logger.getLogger("AlexaLogger").log(Level.INFO, "Session attributes after login: " + sessionAttributes);
                    
                // } else if (intentName.equalsIgnoreCase("GetPortfolioValueIntent")) {
                //     // Überprüfen, ob der Benutzer angemeldet ist
                //     Logger.getLogger("AlexaLogger").log(Level.INFO, "Checking login status for GetPortfolioValueIntent");
                //     if (sessionAttributes.get("loggedIn") != null && (boolean) sessionAttributes.get("loggedIn")) {
                //         String userId = (String) sessionAttributes.get("userID");
                //         PortfolioManagerImplementation portfolioManager = PortfolioManagerImplementation.getPortfolioManager();
                //         Portfolio userPortfolio = portfolioManager.getUserPortfolio(userId);
    
                //         if (userPortfolio != null) {
                //             double portfolioValue = userPortfolio.getValue();
                //             outText = "Dein aktueller Portfoliowert beträgt " + portfolioValue + " Euro.";
                //         } else {
                //             outText = "Es konnte kein Portfolio für dich gefunden werden.";
                //         }
                //     } else {
                //         outText = "Bitte melde dich zuerst an, um deinen Portfoliowert zu erfahren.";
                //         Logger.getLogger("AlexaLogger").log(Level.INFO, "User is not logged in.");
                //     }
                //     shouldEndSession = false;
                } else if (intentName.equalsIgnoreCase("GetUserCountIntent")) {
                    // Benutzeranzahl abrufen
                    UserManagerImplementation userManager = UserManagerImplementation.getUserManager();
                    int userCount = userManager.getUserCount();
                    outText = "Die Gesamtzahl der Benutzer beträgt " + userCount + ".";
                    shouldEndSession = false;
                } else {
                    outText = "Dieser Befehl wird nicht unterstützt.";
                    shouldEndSession = false;
                }
            } else if (requestType.equalsIgnoreCase("SessionEndedRequest")) {
                Logger.getLogger("AlexaLogger").log(Level.INFO, "Session ended with reason: " + alexaRO.getRequest().getReason());
                // Keine Antwort erforderlich
                return null;
            } else {
                outText = "Entschuldigung, ich konnte deine Anfrage nicht verarbeiten.";
                shouldEndSession = false;
            }
        } catch (Exception e) {
            Logger.getLogger("AlexaLogger").log(Level.SEVERE, "Exception occurred: ", e);
            outText = "Es gab einen Fehler bei der Verarbeitung deiner Anfrage.";
            shouldEndSession = false;
        }
    
        return prepareResponse(outText, shouldEndSession, sessionAttributes);
    }


    
     
    private AlexaRO prepareResponse(String outText, boolean shouldEndSession, Map<String, Object> sessionAttributes) {
        AlexaRO responseRO = new AlexaRO();
        responseRO.setVersion("1.0");
    
        // Setzen der Session-Attribute, falls vorhanden
        if (sessionAttributes != null) {
            SessionRO sessionRO = new SessionRO();
            sessionRO.setAttributes(sessionAttributes);
            responseRO.setSession(sessionRO);
        }
    
        // Hauptausgabe der Sprachausgabe festlegen
        OutputSpeechRO outputSpeechRO = new OutputSpeechRO();
        outputSpeechRO.setType("PlainText");
        outputSpeechRO.setText(outText);
    
        // Antwort-Objekt konfigurieren und mit der Sprachausgabe und Sitzungsstatus befüllen
        ResponseRO response = new ResponseRO();
        response.setOutputSpeech(outputSpeechRO);
        response.setShouldEndSession(shouldEndSession);
    
        // Falls Sitzung offen bleiben soll, Reprompt hinzufügen
        if (!shouldEndSession) {
            OutputSpeechRO repromptSpeech = new OutputSpeechRO();
            repromptSpeech.setType("PlainText");
            repromptSpeech.setText("Kannst du das bitte wiederholen?");
            
            RepromptRO reprompt = new RepromptRO();
            reprompt.setOutputSpeech(repromptSpeech);
            response.setReprompt(reprompt);
        }
    
        // Antwort-Objekt in die Hauptantwort einfügen
        responseRO.setResponse(response);
    
        return responseRO;
    }
}
