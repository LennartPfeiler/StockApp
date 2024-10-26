package mosbach.dhbw.de.stockwizzard.dataManager;

import java.util.List;

import mosbach.dhbw.de.stockwizzard.model.PortfolioStock;
import mosbach.dhbw.de.stockwizzard.model.PortfolioStockValue;
import mosbach.dhbw.de.stockwizzard.model.Transaction;

public interface IPortfolioStockManager {

    public void createPortfolioStockTable();

    public void addPortfolioStock(Integer portfolioId, String symbol, Double stockAmount, Double totalPrice);

    public void increasePortfolioStock(Integer portfolioId, String symbol, Double stockAmount, Double totalPrice, String email);

    public List<PortfolioStock> getAllPortfolioStocks(String email, String sortby);

    public PortfolioStock getPortfolioStock(String email, String symbol);

    public void decreasePortfolioStock(Double newCurrentValue, Double newBoughtValue, Double stockAmount, Integer portfolioId, String symbol);

    public PortfolioStockValue getPortfolioStockValues(Double sellRequestAmount, String email, String symbol);

    public void deletePortfolioStock(String symbol, Integer portfolioId);

    public void deleteAllPortfolioStocks(String email);

    public void editCurrentValue(String email, String symbol, Double newCurrentValue);
}
