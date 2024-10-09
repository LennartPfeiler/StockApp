package mosbach.dhbw.de.stockwizzard.dataManager;
import mosbach.dhbw.de.stockwizzard.model.Portfolio;

public interface IPortfolioManager{

    public void createPortfolioTable();

    public void addPortfolio(Portfolio portfolioData);

    public Portfolio getUserPortfolio(String email);
}