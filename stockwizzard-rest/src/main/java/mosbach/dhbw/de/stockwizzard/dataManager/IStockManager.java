package mosbach.dhbw.de.stockwizzard.dataManager;

import mosbach.dhbw.de.stockwizzard.model.Stock;

public interface IStockManager{

    public void createStockTable();

    public Stock getStock(String symbol);

    public void addStock(Stock stock);
}