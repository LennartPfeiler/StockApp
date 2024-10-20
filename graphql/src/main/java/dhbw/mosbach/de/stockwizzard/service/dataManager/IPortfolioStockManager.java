package dhbw.mosbach.de.stockwizzard.service.dataManager;
import java.util.List;

import dhbw.mosbach.de.stockwizzard.model.PortfolioStock;
import dhbw.mosbach.de.stockwizzard.model.PortfolioStockValue;
import dhbw.mosbach.de.stockwizzard.model.Transaction;

public interface IPortfolioStockManager{

    public void createPortfolioStockTable();

    public void increasePortfolioStock(Integer portfolioId, String symbol, Double stockAmount, Double totalPrice);

    public List<PortfolioStock> getAllPortfolioStocks(String email, String sortby);

    public void decreasePortfolioStock(Integer portfolioId, String symbol, Double stockAmount, Double totalPrice, PortfolioStockValue portfolioStockValues, List<Transaction> transactionsInPortfolio);

    public PortfolioStockValue getPortfolioStockValues(Double sellRequestAmount, String email, String symbol);

    public void deleteAllPortfolioStocks(String email);
}