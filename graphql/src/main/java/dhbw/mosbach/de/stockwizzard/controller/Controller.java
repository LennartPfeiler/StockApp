package dhbw.mosbach.de.stockwizzard.graphql;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

@Controller
public class Controller {

private IUserManager userService = new UserManagerImplementation();
private ITransactionManager  transactionService = new TransactionManagerImplementation();
private IStockManager stockService = new StockManagerImplementation();
private ISessionManager sessionService = new SessionManagerImplementation();

    @QueryMapping
    public User getUserProfile(@Argument String email) {
        return userService.getUserProfile(email);
    }

    @MutationMapping
    public StringAnswer createUser(@Argument User user) {
        return UserService.createUser(user);
    }

    @MutationMapping
    public StringAnswer editProfile(@Argument EditRequest editRequest) {
        return UserService.editProfile(editRequest);
    }

    @MutationMapping
    public StringAnswer resetProfile(@Argument TokenEmail tokenEmail) {
        return UserService.resetProfile(tokenEmail);
    }

    @MutationMapping
    public StringAnswer deleteProfile(@Argument TokenEmail tokenEmail) {
        return UserService.deleteProfile(tokenEmail);
    }

    @QueryMapping
    public Stock getStock(@Argument String email, @Argument String token, @Argument String symbol) {
        return StockService.getStock(email, token, symbol);
    }

    @MutationMapping
    public StringAnswer createStock(@Argument AddStockRequest addStockRequest) {
        return StockService.createStock(addStockRequest);
    }

    @QueryMapping
    public List<PortfolioStock> getAllPortfolioStocks(@Argument String email, @Argument String token, @Argument String sortby) {
        return PortfolioService.getAllPortfolioStocks(email, token, sortby);
    }

    @QueryMapping
    public List<Transaction> getAllTransactions(@Argument String email, @Argument String token, @Argument String sortby) {
        return TransactionService.getAllTransactions(email, token, sortby);
    }

    @QueryMapping
    public Portfolio getUserPortfolio(@Argument String email, @Argument String token) {
        return PortfolioService.getUserPortfolio(email, token);
    }

    @MutationMapping
    public StringAnswer createBuyOrder(@Argument TokenTransactionContent tokenTransactionContent) {
        return OrderService.createBuyOrder(tokenTransactionContent);
    }

    @MutationMapping
    public StringAnswer createSellOrder(@Argument TokenTransactionContent tokenTransactionContent) {
        return OrderService.createSellOrder(tokenTransactionContent);
    }
}
