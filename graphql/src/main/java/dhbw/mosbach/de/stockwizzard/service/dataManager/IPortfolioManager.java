package dhbw.mosbach.de.stockwizzard.service.dataManager;

import dhbw.mosbach.de.stockwizzard.model.Portfolio;

public interface IPortfolioManager {

    public void createPortfolioTable();

    public void addPortfolio(Portfolio portfolioData);

    public Portfolio getUserPortfolio(String email);

    public void editPortfolioValue(Integer portfolioId, Double oldValue, Double addition);

    public void editAllPortfolioValues(String email, String newEmail, Double newStartValue, Double newValue);

    public void resetPortfolio(String email);
}