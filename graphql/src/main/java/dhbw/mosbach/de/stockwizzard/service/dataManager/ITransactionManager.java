package dhbw.mosbach.de.stockwizzard.service.dataManager;

import java.util.List;

import dhbw.mosbach.de.stockwizzard.model.Transaction;
import dhbw.mosbach.de.stockwizzard.model.TransactionContent;

public interface ITransactionManager {

    public void createTransactionTable();

    public void addTransaction(TransactionContent transactionContent);

    public List<Transaction> getAllTransactions(String email, String sortby);

    public List<Transaction> getAllTransactionsInPortfolioStock(String email);

    public void editLeftinPortfolio(Integer transactionId, Double newLeftInPortfolio);

    public void editTransactionEmail(String email, String newEmail);

    public void deleteAllTransactions(String email);
}