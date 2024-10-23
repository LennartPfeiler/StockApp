package dhbw.mosbach.de.stockwizzard.graphql.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import java.util.List;


import dhbw.mosbach.de.stockwizzard.model.*;
import dhbw.mosbach.de.stockwizzard.service.dataManager.*;
import dhbw.mosbach.de.stockwizzard.service.dataManagerImplementation.*;


@Controller
public class GraphQLController {

    private IUserManager userService = UserManagerImplementation.getUserManager();
    private ITransactionManager  transactionService = TransactionManagerImplementation.getTransactionManager();
    private IStockManager stockService = StockManagerImplementation.getStockManager();
    private ISessionManager sessionService = SessionManagerImplementation.getSessionManager();
    private IPortfolioStockManager portfolioStockService = PortfolioStockManagerImplementation.getPortfolioStockManager();
    private IPortfolioManager portfolioService = PortfolioManagerImplementation.getPortfolioManager();
    private IAuthManager authService = AuthManagerImplementation.getAuthManager();

    @QueryMapping
    public User getUserProfile(@Argument String email, @Argument String token) {
        try {
            Boolean isValid = sessionService.validToken(token, email);
            if (isValid) {
                User user = userService.getUserProfile(email);
                if (user == null) {
                    throw new RuntimeException("Please register first!");
                } else {
                    return user;
                }
            } else {
                throw new RuntimeException("Unauthorized for this transaction!");
            }
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred while fetching user data.");
        }
    }

    @QueryMapping
    public Object getStock(@Argument String email, @Argument String token, @Argument String symbol) {
        try {
            Boolean isValid = sessionService.validToken(token, email);
            if (isValid) {
                Stock stock = stockService.getStock(symbol);
                if (stock != null) {
                    return stock;
                } else {
                    return new StringAnswer("Stock is not in the database.");
                }
            } else {
                return new StringAnswer("Unauthorized for this transaction!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new StringAnswer("An unexpected error occurred during getting stock data.");
        }
    }

    @QueryMapping
    public Object getAllPortfolioStocks(@Argument String email, @Argument String token, @Argument String sortby) {
        try {
            Boolean isValid = sessionService.validToken(token, email);
            if (isValid) {
                List<PortfolioStock> portfolioStocks = portfolioStockService.getAllPortfolioStocks(email, sortby);
                return portfolioStocks;
            } else {
                throw new RuntimeException("Unauthorized for this transaction!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An unexpected error occurred during getting portfolioStocks.");
        }
    }




    @MutationMapping
    public StringAnswer createUser(@Argument String firstname, @Argument String lastname, @Argument String email, @Argument String password, @Argument Float budget) {
        try {
            User user = new User();
            user.setFirstName(firstname);
            user.setLastName(lastname);
            user.setEmail(email);
            user.setPassword(password);
            user.setBudget(budget.doubleValue());
            Boolean isRegistered = userService.isEmailAlreadyRegistered(user.getEmail());
            if (isRegistered) {
                return new StringAnswer("You are already registered!");
            } else {
                userService.addUser(user);
            
                Portfolio portfolio = new Portfolio(null, user.getBudget(), user.getBudget(), user.getEmail());
                portfolioService.addPortfolio(portfolio);

                return new StringAnswer("User successfully registered!");
            }
        } catch (Exception e) {
            return new StringAnswer("An unexpected error occurred while creating the profile.");
        }
    }

    @MutationMapping
    public Object editProfile(@Argument String currentMail, @Argument String currentToken, @Argument String firstname, @Argument String lastname, @Argument String email, @Argument String password, @Argument Float budget) {
        try{
            EditRequest editRequest = new EditRequest();
            User user = new User();
            user.setFirstName(firstname);
            user.setLastName(lastname);
            user.setEmail(email);
            user.setPassword(password);
            user.setBudget(budget.doubleValue());

            editRequest.setCurrentMail(currentMail);
            editRequest.setToken(currentToken);
            editRequest.setUser(user);

            String token = editRequest.getToken();
            User currentUser = userService.getUserProfile(editRequest.getCurrentMail());
            User newUserData = editRequest.getUser();

            Boolean isValid = sessionService.validToken(token, currentUser.getEmail());
            if (isValid) {
                Boolean emailChanged = !newUserData.getEmail().equals(currentUser.getEmail());
                Portfolio userPortfolio = portfolioService.getUserPortfolio(currentUser.getEmail());

                if (!emailChanged) {
                    userService.editProfile(currentUser, newUserData);
                    portfolioService.editAllPortfolioValues(currentUser.getEmail(), newUserData.getEmail(),
                            (userPortfolio.getStartValue() + newUserData.getBudget()),
                            (userPortfolio.getValue() + newUserData.getBudget()));
                    return userService.getUserProfile(newUserData.getEmail());
                } else {
                    Boolean isRegistered = userService.isEmailAlreadyRegistered(newUserData.getEmail());
                    if (!isRegistered) {
                        userService.addUser(new User(newUserData.getFirstName(), newUserData.getLastName(),
                                newUserData.getEmail(), currentUser.getPassword(),
                                (newUserData.getBudget() + currentUser.getBudget())));
                        sessionService.editSession(currentUser.getEmail(), newUserData.getEmail());
                        transactionService.editTransactionEmail(currentUser.getEmail(), newUserData.getEmail());
                        portfolioService.editAllPortfolioValues(currentUser.getEmail(), newUserData.getEmail(),
                                (userPortfolio.getStartValue() + newUserData.getBudget()),
                                (userPortfolio.getValue() + newUserData.getBudget()));
                        userService.deleteUser(currentUser.getEmail());
                        return userService.getUserProfile(newUserData.getEmail());
                    } else {
                        return new StringAnswer("This email is already registered");
                    }
                }
            } else {
                return new StringAnswer("Unauthorized for this transaction");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new StringAnswer("An unexpected error occurred during editing user.");
        }
    }

    @MutationMapping
    public StringAnswer resetProfile(@Argument String token, @Argument String tokenEmail) {
        try {
            Boolean isValid = sessionService.validToken(token, tokenEmail);
            if (isValid) {
                portfolioStockService.deleteAllPortfolioStocks(tokenEmail);
                transactionService.deleteAllTransactions(tokenEmail);
                portfolioService.resetPortfolio(tokenEmail);
                userService.resetProfile(tokenEmail);
                return new StringAnswer("User successfully resetted!");
            } else {
                return new StringAnswer("Unauthorized for this transaction!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new StringAnswer("An unexpected error occurred during resetting.");
        }
    }

    @MutationMapping
    public StringAnswer deleteProfile(@Argument String token, @Argument String tokenEmail) {
        try {
            boolean isValid = sessionService.validToken(token, tokenEmail);
            if (isValid) {
                userService.deleteUser(tokenEmail);
                return new StringAnswer("Profile successfully deleted");
            } else {
                return new StringAnswer("Unauthorized for this transaction!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new StringAnswer("An unexpected error occurred during resetting the profile.");
        }
    }

    @MutationMapping
    public StringAnswer createStock(@Argument String token, @Argument String email, @Argument String symbol, @Argument Float stockPrice, @Argument String name) {
        try {
            Boolean isValid = sessionService.validToken(token, email);
            if (isValid) {
                Stock stock = new Stock();
                stock.setSymbol(symbol);
                stock.setStockPrice(stockPrice.doubleValue());
                stock.setName(name);
                stockService.addStock(stock);
                return new StringAnswer("Stock got added to Database");
            } else {
                return new StringAnswer("Unauthorized for this transaction!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new StringAnswer("An unexpected error occurred during adding Stock to Database.");
        }
    }








}   
    // @MutationMapping
    // public StringAnswer createUser(@Argument User user) {
    //     return userService.createUser(user);
    // }

    // @MutationMapping
    // public StringAnswer editProfile(@Argument EditRequest editRequest) {
    //     return userService.editProfile(editRequest);
    // }

    // @MutationMapping
    // public StringAnswer resetProfile(@Argument TokenEmail tokenEmail) {
    //     return userService.resetProfile(tokenEmail);
    // }

    // @MutationMapping
    // public StringAnswer deleteProfile(@Argument TokenEmail tokenEmail) {
    //     return userService.deleteProfile(tokenEmail);
    // }

    // @QueryMapping
    // public Stock getStock(@Argument String email, @Argument String token, @Argument String symbol) {
    //     return StockService.getStock(email, token, symbol);
    // }

    // @MutationMapping
    // public StringAnswer createStock(@Argument AddStockRequest addStockRequest) {
    //     return StockService.createStock(addStockRequest);
    // }

    // @QueryMapping
    // public List<PortfolioStock> getAllPortfolioStocks(@Argument String email, @Argument String token, @Argument String sortby) {
    //     return PortfolioService.getAllPortfolioStocks(email, token, sortby);
    // }

    // @QueryMapping
    // public List<Transaction> getAllTransactions(@Argument String email, @Argument String token, @Argument String sortby) {
    //     return TransactionService.getAllTransactions(email, token, sortby);
    // }

    // @QueryMapping
    // public Portfolio getUserPortfolio(@Argument String email, @Argument String token) {
    //     return PortfolioService.getUserPortfolio(email, token);
    // }

    // @MutationMapping
    // public StringAnswer createBuyOrder(@Argument TokenTransactionContent tokenTransactionContent) {
    //     return OrderService.createBuyOrder(tokenTransactionContent);
    // }

    // @MutationMapping
    // public StringAnswer createSellOrder(@Argument TokenTransactionContent tokenTransactionContent) {
    //     return OrderService.createSellOrder(tokenTransactionContent);
    // }

