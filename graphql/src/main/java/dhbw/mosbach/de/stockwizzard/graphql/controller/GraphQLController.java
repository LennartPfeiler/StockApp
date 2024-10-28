package dhbw.mosbach.de.stockwizzard.graphql.controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


import java.util.logging.Level;
import java.util.logging.Logger;


import dhbw.mosbach.de.stockwizzard.model.*;
import dhbw.mosbach.de.stockwizzard.service.dataManager.*;
import dhbw.mosbach.de.stockwizzard.service.dataManagerImplementation.*;
import dhbw.mosbach.de.stockwizzard.model.alexa.*;

@CrossOrigin(origins = "http://StockWizzardSingleFrontend-grumpy-oribi-ia.apps.01.cf.eu01.stackit.cloud", allowedHeaders = "*")
@Controller
public class GraphQLController {

    private IUserManager userService = UserManagerImplementation.getUserManager();
    private ITransactionManager  transactionService = TransactionManagerImplementation.getTransactionManager();
    private IStockManager stockService = StockManagerImplementation.getStockManager();
    private ISessionManager sessionService = SessionManagerImplementation.getSessionManager();
    private IPortfolioStockManager portfolioStockService = PortfolioStockManagerImplementation.getPortfolioStockManager();
    private IPortfolioManager portfolioService = PortfolioManagerImplementation.getPortfolioManager();
    private IAuthManager authService = AuthManagerImplementation.getAuthManager();

    private void editPortfolioValue(String email) {
        Double portfolioValue = 0.0;
        List<PortfolioStock> portfolioStocks = portfolioStockService.getAllPortfolioStocks(email, "symbol");
        for (PortfolioStock portfolioStock : portfolioStocks) {
            portfolioValue += portfolioStock.getCurrentValue();
        }
        User user = userService.getUserProfile(email);     
        portfolioService.editPortfolioValue(email,portfolioValue+user.getBudget());
    }

    private AlexaRO prepareResponse(String outText, boolean shouldEndSession, Map<String, Object> sessionAttributes) {
        AlexaRO responseRO = new AlexaRO();
        responseRO.setVersion("1.0");

        // Session-Attribute setzen
        SessionRO sessionRO = new SessionRO();
        sessionRO.setAttributes(sessionAttributes);
        responseRO.setSession(sessionRO);

        OutputSpeechRO outputSpeechRO = new OutputSpeechRO();
        outputSpeechRO.setType("PlainText");
        outputSpeechRO.setText(outText);

        // Reprompt hinzufügen, um die Sitzung aktiv zu halten
        OutputSpeechRO repromptSpeech = new OutputSpeechRO();
        repromptSpeech.setType("PlainText");
        repromptSpeech.setText("Kannst du das bitte wiederholen?");

        ResponseRO response = new ResponseRO();
        response.setOutputSpeech(outputSpeechRO);
        response.setShouldEndSession(shouldEndSession);

        if (!shouldEndSession) {
            // Reprompt hinzufügen
            RepromptRO reprompt = new RepromptRO();
            reprompt.setOutputSpeech(repromptSpeech);
            response.setReprompt(reprompt);
        }

        responseRO.setResponse(response);

        return responseRO;
    }



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
                editPortfolioValue(email);
                return portfolioStocks;
            } else {
                throw new RuntimeException("Unauthorized for this transaction!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("An unexpected error occurred during getting portfolioStocks.");
        }
    }

    @QueryMapping
        public PortfolioStock getPortfolioStock(@Argument String email, @Argument String token, @Argument String symbol) {

            try {
                Boolean isValid = sessionService.validToken(token, email);
                if (isValid) {
                    PortfolioStock portfolioStock = portfolioStockService.getPortfolioStock(email, symbol);
                    if (portfolioStock != null) {
                        return portfolioStock;
                    } else {
                        throw new RuntimeException("PortfolioStock is not in the database.");
                    }
                } else {
                    throw new RuntimeException("Unauthorized for this transaction!");
                }
            } catch (Exception e) {
                throw new RuntimeException("An unexpected error occurred during getting portfolioStocks: " + e.getMessage());
            }
        }

        @QueryMapping
            public List<Transaction> getAllTransactions(@Argument String email, @Argument String token, @Argument String sortby) {
                try {
                    Boolean isValid = sessionService.validToken(token, email);
                    if (isValid) {
                        return transactionService.getAllTransactions(email, sortby);
                    } else {
                        throw new RuntimeException("Unauthorized for this transaction!");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("An unexpected error occurred during getting transactions: " + e.getMessage());
                }
            }

        @QueryMapping
            public Portfolio getUserPortfolio(@Argument String email, @Argument String token) {
                try {
                    Boolean isValid = sessionService.validToken(token, email);
                    if (isValid) {
                        return portfolioService.getUserPortfolio(email);
                    } else {
                        throw new RuntimeException("Unauthorized for this transaction!");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("An unexpected error occurred while getting the user portfolio: " + e.getMessage());
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
    public StringAnswer resetProfile(@Argument String token, @Argument String SessionEmail) {
        try {
            Boolean isValid = sessionService.validToken(token, SessionEmail);
            if (isValid) {
                portfolioStockService.deleteAllPortfolioStocks(SessionEmail);
                transactionService.deleteAllTransactions(SessionEmail);
                portfolioService.resetPortfolio(SessionEmail);
                userService.resetProfile(SessionEmail);
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
    public StringAnswer deleteProfile(@Argument String token, @Argument String SessionEmail) {
        try {
            boolean isValid = sessionService.validToken(token, SessionEmail);
            if (isValid) {
                userService.deleteUser(SessionEmail);
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

    @MutationMapping
        public String editCurrentValue(@Argument String token, @Argument String email, @Argument String symbol, @Argument Float newValue) {
            try {
                EditCurrentValueRequest editCurrentValueRequest = new EditCurrentValueRequest();
                editCurrentValueRequest.setToken(token);
                editCurrentValueRequest.setEmail(email);
                editCurrentValueRequest.setSymbol(symbol);
                editCurrentValueRequest.setNewValue(newValue.doubleValue());
                Boolean isValid = sessionService.validToken(editCurrentValueRequest.getToken(),
                        editCurrentValueRequest.getEmail());
                if (isValid) {
                    portfolioStockService.editCurrentValue(editCurrentValueRequest.getEmail(),
                            editCurrentValueRequest.getSymbol(), editCurrentValueRequest.getNewValue());
                    return "Current Value successfully updated";
                } else {
                    return "Unauthorized for this transaction!";
                }
            } catch (Exception e) {
                return "An unexpected error occurred during updating current value of stock.";
            }
        }

        @MutationMapping
            public StringAnswer addPortfolioStockOrder(@Argument Integer transactionType, @Argument Float stockAmount, @Argument String date, @Argument Float pricePerStock, @Argument Float totalPrice, @Argument String email, @Argument String symbol, @Argument String token) {
                try {
                    TransactionContent transactionContent = new TransactionContent();
                    transactionContent.setTransactionType(transactionType);
                    transactionContent.setStockAmount(stockAmount.doubleValue());
                    transactionContent.setDate(date);
                    transactionContent.setPricePerStock(pricePerStock.doubleValue());
                    transactionContent.setTotalPrice(totalPrice.doubleValue());
                    transactionContent.setEmail(email);
                    transactionContent.setSymbol(symbol);

                    Boolean isValid = sessionService.validToken(token, transactionContent.getEmail());
                    if (isValid) {
                        User currentUser = userService.getUserProfile(transactionContent.getEmail());
                        Boolean enoughBudget = userService.checkIfEnoughBudgetLeft(transactionContent.getTotalPrice(), currentUser);
                        if (enoughBudget) {
                            // Transaktion hinzufügen
                            transactionService.addTransaction(transactionContent);
                            
                            // Benutzerportfolio holen und aktualisieren
                            Portfolio userPortfolio = portfolioService.getUserPortfolio(transactionContent.getEmail());
                            portfolioStockService.addPortfolioStock(userPortfolio.getPortfolioID(),
                                    transactionContent.getSymbol(), transactionContent.getStockAmount(),
                                    transactionContent.getTotalPrice());

                            // Benutzerbudget aktualisieren
                            userService.editUserBudget(currentUser.getEmail(), currentUser.getBudget(),
                                    transactionContent.getTotalPrice(), transactionContent.getTransactionType());

                            return new StringAnswer("Transaction was successfully completed");
                        } else {
                            return new StringAnswer("Not enough budget for this transaction!");
                        }
                    } else {
                        return new StringAnswer("Unauthorized for this transaction!");
                    }
                } catch (Exception e) {
                    return new StringAnswer("An unexpected error occurred while processing the transaction: " + e.getMessage());
                }
            }

            @MutationMapping
                public StringAnswer createBuyOrder(@Argument Integer transactionType, @Argument Float stockAmount, @Argument String date, @Argument Float pricePerStock, @Argument Float totalPrice, @Argument String email, @Argument String symbol, @Argument String token) {
                    try {
                        TransactionContent transactionContent = new TransactionContent();
                        transactionContent.setTransactionType(transactionType);
                        transactionContent.setStockAmount(stockAmount.doubleValue());
                        transactionContent.setDate(date);
                        transactionContent.setPricePerStock(pricePerStock.doubleValue());
                        transactionContent.setTotalPrice(totalPrice.doubleValue());
                        transactionContent.setEmail(email);
                        transactionContent.setSymbol(symbol);

                        Boolean isValid = sessionService.validToken(token, transactionContent.getEmail());
                        
                        if (isValid) {
                            User currentUser = userService.getUserProfile(transactionContent.getEmail());
                            Boolean enoughBudget = userService.checkIfEnoughBudgetLeft(transactionContent.getTotalPrice(), currentUser);
                            
                            if (enoughBudget) {
                                transactionService.addTransaction(transactionContent);
                                Portfolio userPortfolio = portfolioService.getUserPortfolio(transactionContent.getEmail());
                                
                                portfolioStockService.addPortfolioStock(
                                    userPortfolio.getPortfolioID(),
                                    transactionContent.getSymbol(),
                                    transactionContent.getStockAmount(),
                                    transactionContent.getTotalPrice()
                                );
                                
                                userService.editUserBudget(
                                    currentUser.getEmail(),
                                    currentUser.getBudget(),
                                    transactionContent.getTotalPrice(),
                                    transactionContent.getTransactionType()
                                );
                                
                                editPortfolioValue(currentUser.getEmail());
                                
                                return new StringAnswer("Transaction was successfully completed");
                            } else {
                                return new StringAnswer("Not enough budget for this transaction!");
                            }
                        } else {
                            return new StringAnswer("Unauthorized for this transaction!");
                        }
                    } catch (Exception e) {
                        return new StringAnswer("An unexpected error occurred while processing the transaction.");
                    }
                }

                @MutationMapping
                    public StringAnswer increasePortfolioStockOrder(@Argument Integer transactionType, @Argument Float stockAmount, @Argument String date, @Argument Float pricePerStock, @Argument Float totalPrice, @Argument String email, @Argument String symbol, @Argument String token) {
                        try {
                            TransactionContent transactionContent = new TransactionContent();
                            transactionContent.setTransactionType(transactionType);
                            transactionContent.setStockAmount(stockAmount.doubleValue());
                            transactionContent.setDate(date);
                            transactionContent.setPricePerStock(pricePerStock.doubleValue());
                            transactionContent.setTotalPrice(totalPrice.doubleValue());
                            transactionContent.setEmail(email);
                            transactionContent.setSymbol(symbol);

                            Boolean isValid = sessionService.validToken(token, transactionContent.getEmail());
                            
                            if (isValid) {
                                User currentUser = userService.getUserProfile(transactionContent.getEmail());
                                Boolean enoughBudget = userService.checkIfEnoughBudgetLeft(transactionContent.getTotalPrice(), currentUser);
                                
                                if (enoughBudget) {
                                    transactionService.addTransaction(transactionContent);
                                    Portfolio userPortfolio = portfolioService.getUserPortfolio(transactionContent.getEmail());
                                    
                                    portfolioStockService.increasePortfolioStock(
                                        userPortfolio.getPortfolioID(),
                                        transactionContent.getSymbol(),
                                        transactionContent.getStockAmount(),
                                        transactionContent.getTotalPrice(),
                                        currentUser.getEmail()
                                    );
                                    
                                    userService.editUserBudget(
                                        currentUser.getEmail(),
                                        currentUser.getBudget(),
                                        transactionContent.getTotalPrice(),
                                        transactionContent.getTransactionType()
                                    );
                                    
                                    editPortfolioValue(currentUser.getEmail());
                                    
                                    return new StringAnswer("Transaction was successfully completed");
                                } else {
                                    return new StringAnswer("Not enough budget for this transaction!");
                                }
                            } else {
                                return new StringAnswer("Unauthorized for this transaction!");
                            }
                        } catch (Exception e) {
                            return new StringAnswer("An unexpected error occurred while processing the transaction.");
                        }
                    }

            @MutationMapping
            public StringAnswer decreasePortfolioStockOrder(@Argument Integer transactionType, @Argument Float stockAmount, @Argument String date, @Argument Float pricePerStock, @Argument Float totalPrice, @Argument String email, @Argument String symbol, @Argument String token) {
                try {
                    TransactionContent transactionContent = new TransactionContent();
                    transactionContent.setTransactionType(transactionType);
                    transactionContent.setStockAmount(stockAmount.doubleValue());
                    transactionContent.setDate(date);
                    transactionContent.setPricePerStock(pricePerStock.doubleValue());
                    transactionContent.setTotalPrice(totalPrice.doubleValue());
                    transactionContent.setEmail(email);
                    transactionContent.setSymbol(symbol);

                    Boolean isValid = sessionService.validToken(token, transactionContent.getEmail());
                    
                    if (isValid) {
                        // GET USER
                        User currentUser = userService.getUserProfile(transactionContent.getEmail());
                        // GET BOUGHT AND CURRENT VALUE OF PORTFOLIO STOCK
                        PortfolioStockValue portfolioStockValues = portfolioStockService.getPortfolioStockValues(
                            transactionContent.getTotalPrice(), 
                            currentUser.getEmail(), 
                            transactionContent.getSymbol()
                        );
                        
                        if (portfolioStockValues == null) {
                            return new StringAnswer("You don't own a position with the selected stock!");
                        } else {
                            if (portfolioStockValues.getCurrentValue() == -1) {
                                return new StringAnswer("Your stock position is not that high!");
                            } else {
                                // ADD SELL TRANSACTION
                                transactionService.addTransaction(transactionContent);
                                // GET USER PORTFOLIO
                                Portfolio userPortfolio = portfolioService.getUserPortfolio(transactionContent.getEmail());
                                // GET ALL TRANSACTIONS OF A USER
                                List<Transaction> transactionsInPortfolio = transactionService.getAllTransactionsInPortfolioStock(
                                    transactionContent.getEmail()
                                );

                                Double remainingAmount = transactionContent.getTotalPrice();
                                Double totalBoughtValueReduction = 0.0;

                                for (Transaction transaction : transactionsInPortfolio) {
                                    if (remainingAmount <= 0) break;

                                    Double leftInTransaction = transaction.getLeftInPortfolio();
                                    Double transactionBoughtValue = transaction.getTotalPrice();
                                    Integer transactionId = transaction.getTransactionID();

                                    if (remainingAmount >= leftInTransaction) {
                                        remainingAmount -= leftInTransaction;
                                        totalBoughtValueReduction += transactionBoughtValue;
                                        transactionService.editLeftinPortfolio(transactionId, 0.0);
                                    } else {
                                        Double proportion = remainingAmount / transactionBoughtValue;
                                        Double reductionInBoughtValue = transactionBoughtValue * proportion;
                                        totalBoughtValueReduction += reductionInBoughtValue;
                                        Double newLeftInTransaction = leftInTransaction - remainingAmount;
                                        remainingAmount = 0.0;
                                        transactionService.editLeftinPortfolio(transactionId, newLeftInTransaction);
                                    }
                                }

                                Double newCurrentValue = portfolioStockValues.getCurrentValue() - transactionContent.getTotalPrice();
                                Double newBoughtValue = portfolioStockValues.getBoughtValue() - totalBoughtValueReduction;

                                portfolioStockService.decreasePortfolioStock(
                                    newCurrentValue, 
                                    newBoughtValue, 
                                    transactionContent.getStockAmount(), 
                                    userPortfolio.getPortfolioID(), 
                                    transactionContent.getSymbol()
                                );
                                
                                userService.editUserBudget(
                                    currentUser.getEmail(), 
                                    currentUser.getBudget(),
                                    transactionContent.getTotalPrice(), 
                                    transactionContent.getTransactionType()
                                );
                                
                                editPortfolioValue(currentUser.getEmail());
                                
                                return new StringAnswer("Transaction was successfully completed");
                            }
                        }
                    } else {
                        return new StringAnswer("Unauthorized for this transaction!");
                    }
                } catch (Exception e) {
                    return new StringAnswer("An unexpected error occurred while processing the transaction.");
                }
            }

        @MutationMapping
        public StringAnswer deletePortfolioStockOrder(@Argument Integer transactionType, @Argument Float stockAmount, @Argument String date, @Argument Float pricePerStock, @Argument Float totalPrice, @Argument String email, @Argument String symbol, @Argument String token) {
            try {
                TransactionContent transactionContent = new TransactionContent();
                transactionContent.setTransactionType(transactionType);
                transactionContent.setStockAmount(stockAmount.doubleValue());
                transactionContent.setDate(date);
                transactionContent.setPricePerStock(pricePerStock.doubleValue());
                transactionContent.setTotalPrice(totalPrice.doubleValue());
                transactionContent.setEmail(email);
                transactionContent.setSymbol(symbol);

                Boolean isValid = sessionService.validToken(token, transactionContent.getEmail());

                if (isValid) {
                    User currentUser = userService.getUserProfile(transactionContent.getEmail());
                    PortfolioStockValue portfolioStockValues = portfolioStockService.getPortfolioStockValues(
                        transactionContent.getTotalPrice(), 
                        currentUser.getEmail(), 
                        transactionContent.getSymbol()
                    );

                    if (portfolioStockValues == null) {
                        return new StringAnswer("You don't own a position with the selected stock!");
                    } else if (portfolioStockValues.getCurrentValue() == -1) {
                        return new StringAnswer("Your stock position is not that high!");
                    } else {
                        transactionService.addTransaction(transactionContent);
                        Portfolio userPortfolio = portfolioService.getUserPortfolio(transactionContent.getEmail());
                        List<Transaction> transactionsInPortfolio = transactionService.getAllTransactionsInPortfolioStock(
                            transactionContent.getEmail()
                        );

                        // Delete portfolio stock
                        portfolioStockService.deletePortfolioStock(transactionContent.getSymbol(), userPortfolio.getPortfolioID());

                        // Set all remaining transaction portfolio values to zero
                        for (Transaction transaction : transactionsInPortfolio) {
                            transactionService.editLeftinPortfolio(transaction.getTransactionID(), 0.0);
                        }

                        userService.editUserBudget(
                            currentUser.getEmail(),
                            currentUser.getBudget(),
                            transactionContent.getTotalPrice(),
                            transactionContent.getTransactionType()
                        );
                        
                        editPortfolioValue(currentUser.getEmail());

                        return new StringAnswer("Transaction was successfully completed");
                    }
                } else {
                    return new StringAnswer("Unauthorized for this transaction!");
                }
            } catch (Exception e) {
                return new StringAnswer("An unexpected error occurred while processing the transaction.");
            }
        }

        @MutationMapping
        public AlexaRO handleAlexaRequest(@Argument AlexaROInput alexaRO) {
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

                if (requestType.equalsIgnoreCase("LaunchRequest")) {
                    outText = "Willkommen zu The Wallstreet Wizzard. Wie kann ich dir helfen?";
                    Logger.getLogger("AlexaLogger").log(Level.INFO, "Handling LaunchRequest");
                } else if (requestType.equalsIgnoreCase("IntentRequest")) {
                    IntentRO intent = alexaRO.getRequest().getIntent();
                    String intentName = intent.getName();
                    Logger.getLogger("AlexaLogger").log(Level.INFO, "Handling IntentRequest: " + intentName);

                    if (intentName.equalsIgnoreCase("GetUserCountIntent")) {
                        // Holen der Benutzeranzahl
                        int userCount = userService.getUserCount();
                        outText = "Die Gesamtzahl der Benutzer beträgt " + userCount + ".";
                        shouldEndSession = true;
                    } else {
                        outText = "Dieser Befehl wird nicht unterstützt.";
                        shouldEndSession = true;
                    }
                } else if (requestType.equalsIgnoreCase("SessionEndedRequest")) {
                    Logger.getLogger("AlexaLogger").log(Level.INFO, "Session ended with reason: " + alexaRO.getRequest().getReason());
                    // Keine Antwort erforderlich
                    return null;
                } else {
                    outText = "Entschuldigung, ich konnte deine Anfrage nicht verarbeiten.";
                    shouldEndSession = true;
                }
            } catch (Exception e) {
                Logger.getLogger("AlexaLogger").log(Level.SEVERE, "Exception occurred: ", e);
                outText = "Es gab einen Fehler bei der Verarbeitung deiner Anfrage.";
                shouldEndSession = true;
            }

            return prepareResponse(outText, shouldEndSession, sessionAttributes);
        }



    }