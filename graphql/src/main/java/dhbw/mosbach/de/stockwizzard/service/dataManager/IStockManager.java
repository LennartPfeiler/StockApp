package dhbw.mosbach.de.stockwizzard.service.dataManager;

import dhbw.mosbach.de.stockwizzard.model.Stock;

public interface IStockManager{

    public void createStockTable();

    public Stock getStock(String symbol);

    public void addStock(Stock stock);
}