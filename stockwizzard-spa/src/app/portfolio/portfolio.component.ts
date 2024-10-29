import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthComponent } from '../auth/auth.component';
import { Apollo, QueryRef, gql } from 'apollo-angular';
import { tap } from 'rxjs/operators';
import { HttpClientModule, HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-portfolio',
  standalone: true,
  imports: [CommonModule, RouterModule, HttpClientModule],
  templateUrl: './portfolio.component.html',
  styleUrls: ['./portfolio.component.css']
})


export class PortfolioComponent implements OnInit {
  budget: string = '';
  portfolioValue: number | null = null;
  transactions: any[] = [];
  portfolioStocks: any[] = [];
  loading: boolean = true;
  error: any;
  private authComponent: AuthComponent;
  constructor(public router: Router, private apollo: Apollo, private http: HttpClient) {
    this.authComponent = new AuthComponent(router);
  }

  ngOnInit(): void {
    this.authComponent.disableGoBackFunction();
    this.displayAllDatabaseData();
    this.getAllPortfolioStocks();
  }
  test(): void{
    console.log("test");
  }

  displayAllDatabaseData(){
    this.getUserBudget();
    this.getPortfolioValue();
    this.loadTransactions();
  }

  getCurrentDateTime(): string {
    const now: Date = new Date();
    const year: number = now.getFullYear(); 
    const month: string = String(now.getMonth() + 1).padStart(2, '0'); 
    const day: string = String(now.getDate()).padStart(2, '0'); 
    const hours: string = String(now.getHours()).padStart(2, '0'); 
    const minutes: string = String(now.getMinutes()).padStart(2, '0'); 
    const seconds: string = String(now.getSeconds()).padStart(2, '0'); 

    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
}


  toggleLabel(): void {
    const label = document.getElementById('quantity-label') as HTMLLabelElement;
    const button = document.getElementById('toggle-label') as HTMLButtonElement;

    if (label.textContent === 'Quantity in $:') {
        label.textContent = 'Quantity in Stocks:';
        button.textContent = 'Switch to $';
    } else {
        label.textContent = 'Quantity in $:';
        button.textContent = 'Switch to Stocks';
    }
  }

// Check if all necessary fields for creating an order are filled
  checkFields(): void {
    const stockName = (document.getElementById('stock-name') as HTMLInputElement).value.trim();
    const quantity = (document.getElementById('quantity') as HTMLInputElement).value.trim();

    const buyButton = document.getElementById('buy-stock') as HTMLButtonElement;
    const sellButton = document.getElementById('sell-stock') as HTMLButtonElement;

    if (stockName !== "" && quantity !== "") {
        buyButton.disabled = false; 
        sellButton.disabled = false; 
    } else {
        buyButton.disabled = true;
        sellButton.disabled = true;
    }
  }


  loadTransactions(): void {
    const email = this.authComponent.getCookie("email"); // Hole die E-Mail aus dem Cookie
    const token = this.authComponent.getCookie("token"); // Hole das Token aus dem Cookie
    const sortby = "date"; // Beispielwert, kann angepasst werden

    this.apollo
      .watchQuery({
        query: GET_ALL_TRANSACTIONS,
        variables: { email, token, sortby },
      })
      .valueChanges
      .pipe(
        tap(({ data } : any) => 
          {
          this.transactions = data.getAllTransactions || [];
          this.displayTransactionHistory(this.transactions);
        }
      )
      )
      .subscribe({
        next: () => {
        },
        error: (error) => {
          this.error = error;
          this.loading = false;
          alert('An unexpected error occurred: ' + error.message);
        },
      });
  }

  // loadTransactions(): void {
  //   const email = this.authComponent.getCookie("email"); // Hole die E-Mail aus dem Cookie
  //   const token = this.authComponent.getCookie("token"); // Hole das Token aus dem Cookie
  //   const sortby = "date"; // Beispielwert für die Sortierung

  //   const url = `https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/transactions?email=${email}&token=${token}&sortby=${sortby}`;

  //   this.http.get<any[]>(url).subscribe({
  //     next: (transactions) => {
  //       this.transactions = transactions || [];
  //       this.loading = false;
  //       console.log(transactions);
  //       this.displayTransactionHistory(this.transactions); // Aufruf der Funktion zur Anzeige
  //     },
  //     error: (error) => {
  //       this.error = error;
  //       this.loading = false;
  //       alert('An unexpected error occurred: ' + error.message);
  //     },
  //   });
  // }

  displayTransactionHistory(transactions: any[]): void {
    const transactionHistoryContainer = document.querySelector('.transaction-history') as HTMLElement;

    transactionHistoryContainer.innerHTML = ''; 
    const heading = document.createElement('h2');
    heading.textContent = 'Transaction History';
    transactionHistoryContainer.appendChild(heading);

    transactions.forEach(transaction => {
        const transactionDiv = document.createElement('div');
        const type = transaction.transactionType === 1 ? 'Bought' : 'Sold';
        transactionDiv.textContent = `${type} ${transaction.stockAmount} ${transaction.symbol} at price of ${transaction.pricePerStock}$ for ${transaction.totalPrice}$`;
        transactionHistoryContainer.appendChild(transactionDiv);
    });
  }

    getAllPortfolioStocks(): void {
      const email = this.authComponent.getCookie("email"); // Hole die E-Mail aus dem Cookie
      const token = this.authComponent.getCookie("token"); // Hole das Token aus dem Cookie
      const sortby = "symbol"; // Beispielwert, kann angepasst werden

      this.apollo
        .watchQuery({
          query: GET_ALL_PORTFOLIOSTOCKS,
          variables: { email, token, sortby },
        })
        .valueChanges
        .pipe(
          tap(({ data } : any) => 
            {
              this.portfolioStocks = data.getAllPortfolioStocks || [];
              this.displayPortfolioStocks(this.portfolioStocks);
              this.portfolioStocks.forEach(portfolioStock => {
                var positionAmount = portfolioStock.stockAmount;
                this.getNewCurrentValue(portfolioStock.symbol, (price) => { // Arrow-Funktion
                    let totalValue = this.roundToTwoDecimalPlaces(price * positionAmount);
                    this.setNewCurrentValue(totalValue, portfolioStock.symbol);
                    this.updateStockDisplay(portfolioStock.symbol, totalValue, portfolioStock.boughtValue);
                });
              })
          }
        )
        )
        .subscribe({
          next: () => {
          },
          error: (error) => {
            this.error = error;
            this.loading = false;
            alert('An unexpected error occurred: ' + error.message);
          },
      });
  }

  getNewCurrentValue(stockName: string, callback: (value: number) => void): void {
    const editCurrentValueRegister = {
        url: `https://api.polygon.io/v2/aggs/ticker/${stockName}/prev?adjusted=true&apiKey=Vf080TfqbqvnJHcpt2aP9Ec1XL21Xb0D`,
        method: "GET",
        dataType: 'json',
    };

    this.http.get<any[]>(editCurrentValueRegister.url).subscribe({
        next: (data: any) => {
            callback(this.roundToTwoDecimalPlaces(data.results[0].c));
        },
        error: () => { // Arrow function here
            console.log("Error fetching stock price, retrying...");
            setTimeout(() => {
                this.getNewCurrentValue(stockName, callback); // Erneuter Versuch
            }, 2000);
        }
    });
  }

  setNewCurrentValue(currentValue: number, symbol: string): void {
    const email = this.authComponent.getCookie("email"); 
    const token = this.authComponent.getCookie("token"); 
    this.apollo.mutate({
      mutation: EDIT_CURRENT_VALUE,
      variables: {
        token,
        email,
        symbol,
        newValue: currentValue,
      },
    }).subscribe({
      next: (response) => {
        console.log("Antwort vom Server:", response.data);
      },
      error: (error) => {
        console.error("Fehler bei der Anfrage:", error);
        alert("Ein Fehler ist aufgetreten: " + (error.message || "Unbekannter Fehler."));
      }
    });
  }

  // Zeigt alle Portfolio-Aktien an
  displayPortfolioStocks(portfolioStocks: { symbol: string }[]): void {
    const stockListContainer = document.querySelector('.portfolio .stock-list') as HTMLElement;

    stockListContainer.innerHTML = ''; 

    portfolioStocks.forEach(stock => {
        // Ruft die displayPortfolioStock-Funktion auf, um jede Aktie anzuzeigen
        this.displayPortfolioStock(stock.symbol);
    });
  }

  // Zeigt eine einzelne Portfolio-Aktie an
  displayPortfolioStock(symbol: string): void {
    const stockListContainer = document.querySelector('.portfolio .stock-list') as HTMLElement;

    const stockDiv = document.createElement('div');
    stockDiv.id = symbol; // Füge eine ID für die spätere Aktualisierung hinzu
    stockDiv.innerHTML = `${symbol}: Calculating Portfolio data... <span class="change"></span>`;
    
    stockListContainer.appendChild(stockDiv);
  }

  getUserBudget(): void {
    const email = this.authComponent.getCookie("email"); // Hole die E-Mail aus dem Cookie
    const token = this.authComponent.getCookie("token"); // Hole das Token aus dem Cookie

    this.apollo
      .watchQuery({
        query: GET_USER_BUDGET,
        variables: { email, token },
      })
      .valueChanges
      .pipe(
        tap(({ data } : any) => 
          {
            this.budget = data.getUserProfile.budget || [];
        }
      )
      )
      .subscribe({
        next: () => {
          var budgetElement = document.querySelector('.remaining-budget') as HTMLElement;
          budgetElement.textContent = this.budget + " $";
        },
        error: (error) => {
          var budgetElement = document.querySelector('.remaining-budget') as HTMLElement;
          budgetElement.textContent = JSON.parse(error.message).answer;
        },
    });
  }
  
  getPortfolioValue(): void {
    const email = this.authComponent.getCookie("email"); // Hole die E-Mail aus dem Cookie
    const token = this.authComponent.getCookie("token"); // Hole das Token aus dem Cookie

    this.apollo
      .watchQuery({
        query: GET_TOTAL_PORTFOLIO_VALUE,
        variables: { email, token },
      })
      .valueChanges
      .pipe(
        tap(({ data } : any) => 
          {
            const portfolioData = data.getUserPortfolio;
            const stockValue = this.roundToTwoDecimalPlaces(portfolioData.value);
            this.portfolioValue = stockValue || null;
            const { percentageChange, changeClass } = this.calculatePercentage(portfolioData.startValue, portfolioData.value);
            const stockElement = document.querySelector('.portfolio-value') as HTMLElement;
            stockElement.innerHTML = `${stockValue}$ <span class="percentage.${changeClass}">${percentageChange}</span>`;
        }
      )
      )
      .subscribe({
        next: () => {
        },
        error: (error) => {
          var budgetElement = document.querySelector('.portfolio-value') as HTMLElement;
          budgetElement.textContent = JSON.parse(error.message).answer;
        },
    });
  }

  roundToTwoDecimalPlaces(value: number): number {
    return Math.round(value * 100) / 100;
  }

  calculatePercentage(boughtValue: number, currentValue: number): { percentageChange: string; changeClass: string } {
    const percentageChange = ((currentValue - boughtValue) / boughtValue * 100).toFixed(2);
    const changeClass = parseFloat(percentageChange) >= 0 ? 'positive' : 'negative';
    const sign = parseFloat(percentageChange) >= 0 ? '+' : '';

    return {
        percentageChange: `${sign}${percentageChange}%`,
        changeClass: changeClass
    };
  }

  updateStockDisplay(symbol: string, currentValue: number, boughtValue: number): void {
    const stockElement = document.getElementById(symbol) as HTMLElement;

    if (!stockElement) {
        console.error(`Element mit dem Symbol "${symbol}" wurde nicht gefunden.`);
        return;
    }

    const stockValue = this.roundToTwoDecimalPlaces(currentValue);

    const { percentageChange, changeClass } = this.calculatePercentage(boughtValue, currentValue);
    stockElement.innerHTML = `${symbol}: ${stockValue}$ <span class="change ${changeClass}">${percentageChange}</span>`;
  }

}

const GET_ALL_TRANSACTIONS = gql`
  query getAllTransactions($email: String!, $token: String!, $sortby: String!) {
    getAllTransactions(email: $email, token: $token, sortby: $sortby) {
      transactionID
      transactionType
      stockAmount
      date
      pricePerStock
      totalPrice
      email
      symbol
      leftInPortfolio
    }
  }
`;

const GET_ALL_PORTFOLIOSTOCKS = gql`
  query getAllPortfolioStocks($email: ID!, $token: String!, $sortby: String) {
    getAllPortfolioStocks(email: $email, token: $token, sortby: $sortby) {
      portfolioID
      symbol 
      stockAmount 
      boughtValue 
      currentValue 
    }
  }
`;

const GET_USER_BUDGET = gql`
  query getUserProfile($email: ID!, $token: String!) {
    getUserProfile(email: $email, token: $token) {
      firstname
      lastname
      email
      password
      budget
    }
  }
`;

const GET_TOTAL_PORTFOLIO_VALUE = gql`
  query getUserPortfolio($email: ID!, $token: String!) {
    getUserPortfolio(email: $email, token: $token) {
      portfolioID
      value
      startValue
      email
    }
  }
`;

const EDIT_CURRENT_VALUE = gql`
  mutation editCurrentValue($token: String!, $email: String!, $symbol: String!, $newValue: Float!) {
    editCurrentValue(token: $token, email: $email, symbol: $symbol, newValue: $newValue)
}
`;