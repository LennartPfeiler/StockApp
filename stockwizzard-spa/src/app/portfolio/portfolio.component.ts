import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthComponent } from '../auth/auth.component';
import { Apollo, QueryRef, gql } from 'apollo-angular';
import { tap } from 'rxjs/operators';
import { HttpClientModule, HttpClient } from '@angular/common/http';

const APIKEY = 'Vf080TfqbqvnJHcpt2aP9Ec1XL21Xb0D';

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
    this.showStockPriceViaEvent();
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

  displayStockPrice(value: string): void {
    const priceDisplay = document.getElementById('price-display') as HTMLElement;
    if (priceDisplay) {
      priceDisplay.textContent = value.toString();
    }
  }

  getOrderInformations(): { amount: number; price: number; totalPrice: number } | null {
    let stockAmount: number;
    const priceDisplay: string = (document.getElementById('price-display') as HTMLElement).textContent?.trim() || '';
    const price: number = parseFloat(priceDisplay.replace('$', '').trim());

    if (isNaN(price) || price <= 0) {
      alert("Enter a valid stock price!");
      return null;
    }

    const quantityLabel = (document.getElementById('quantity-label') as HTMLLabelElement).textContent;

    if (quantityLabel === "Quantity in $:") {
      const quantity: number = this.roundToTwoDecimalPlaces(parseFloat((document.getElementById('quantity') as HTMLInputElement).value.trim()));
      if (isNaN(quantity) || quantity <= 0) {
        alert("Enter a valid quantity in $!");
        return null;
      }
      stockAmount = quantity / price;
    } else {
      const quantity: number = parseFloat((document.getElementById('quantity') as HTMLInputElement).value.trim());
      if (isNaN(quantity) || quantity <= 0) {
        alert("Enter a valid stock amount!");
        return null;
      }
      stockAmount = quantity;
    }

    const totalPrice: number = this.roundToTwoDecimalPlaces(price * stockAmount);

    return {
      amount: stockAmount,
      price: price,
      totalPrice: totalPrice
    };
  }

  checkBuyStock(): void {
    const orderData = this.getOrderInformations(); // Assuming this method exists in the same class

    if (orderData != null) {
        const stockName = (document.getElementById('stock-name') as HTMLInputElement).value;

        this.getPortfolioStockData(stockName, (data: number) => {
            if (data === -1) {
                this.addPortfolioStockOrder(orderData.amount, orderData.totalPrice, orderData.price);
            } else {
                if (data === 0) {
                    alert("An error occurred when trying to create order. Please try later again.");
                } else {
                    const remainingBudget = parseFloat((document.querySelector('.remaining-budget') as HTMLElement).innerText.trim());

                    if (orderData.totalPrice > remainingBudget) {
                        alert("You don't have enough budget for this transaction!");
                    } else {
                        this.increasePortfolioStockOrder(orderData.amount, orderData.totalPrice, orderData.price);
                    }
                }
            }
        });
    }
  }

  checkSellStock(): void {
    const orderData = this.getOrderInformations(); 

    if (orderData != null) {
        const stockName = (document.getElementById('stock-name') as HTMLInputElement).value;

        this.getPortfolioStockData(stockName, (data: { currentvalue: number } | number) => {
            if (data === -1) {
                alert("You don't have this stock in your portfolio.");
            } else if (data === 0) {
                alert("An error occurred when trying to create order. Please try later again.");
            } else {
                // Hier sicherstellen, dass data ein Objekt ist
                if (typeof data !== 'number' && data.currentvalue !== undefined) {
                    if (orderData.totalPrice > data.currentvalue) {
                        alert("Your stock position is not that high!");
                    } else {
                        if (orderData.totalPrice === data.currentvalue) {
                            this.deletePortfolioStockOrder(orderData.amount, orderData.totalPrice, orderData.price);
                        } else {
                            this.decreasePortfolioStockOrder(orderData.amount, orderData.totalPrice, orderData.price);
                        }
                    }
                } else {
                    alert("Invalid data received."); // Fallback für unerwartete Daten
                }
            }
        });
    }
  }

  getPortfolioStockData(symbol: string, callback: (data: any) => void): void {
    const email = this.authComponent.getCookie("email");
    const token = this.authComponent.getCookie("token");
    const url = `https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/portfolioStock?email=${email}&token=${token}&symbol=${symbol}`;

    this.http.get(url, { observe: 'response' }).subscribe({
        next: (response) => {
            if (response.status === 200) {
                callback(response.body); // Daten zurückgeben
            } else {
                callback(0); // Fehlerbehandlung, wenn der Status nicht 200 ist
            }
        },
        error: (error) => {
            if (error.status === 404) {
                callback(-1); // Symbol nicht gefunden
            } else {
                console.error("Error fetching bought value:", error);
                callback(0); // Allgemeiner Fehler
            }
        }
    });
  }

  addPortfolioStockOrder(stockAmount: number, totalPrice: number, pricePerStock: number): void {
    const stockSymbol = (document.getElementById('stock-name') as HTMLInputElement).value;
    const orderData = {
        token: this.authComponent.getCookie("token"),
        transactioncontent: {
            transactiontype: 1,
            stockamount: stockAmount,
            date: this.getCurrentDateTime(), // Methode, um das aktuelle Datum zu holen
            priceperstock: pricePerStock,
            totalprice: totalPrice,
            email: this.authComponent.getCookie("email"),
            symbol: stockSymbol
        }
    };

    this.http.post<any>("https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/order", orderData, { observe: 'response' })
        .subscribe({
            next: (response) => {
                // Überprüfen, ob die Antwort erfolgreich war
                if (response.status === 200) {
                    alert("Order successfully added!"); // Erfolgsmeldung anzeigen
                } else {
                    alert("Unexpected response from the server."); // Allgemeine Antwortnachricht
                }
                (document.getElementById('quantity') as HTMLInputElement).value = ""; // Input-Feld leeren
                this.displayPortfolioStock(stockSymbol); // Portfolio-Aktie anzeigen

                this.getPortfolioStockData(stockSymbol, (data) => {
                    this.updateStockDisplay(stockSymbol, data.currentvalue, data.boughtvalue); // Aktienanzeige aktualisieren
                });

                this.displayAllDatabaseData(); // Alle Daten anzeigen
            },
            error: (error) => {
                if (error.status === 400 || error.status === 401 || error.status === 500) {
                    alert("An error occurred: " + error.message); // Fehlernachricht anzeigen
                    (document.getElementById('quantity') as HTMLInputElement).value = ""; // Input-Feld leeren
                } else {
                    alert("An unexpected error occurred. Status: " + error.status); // Allgemeiner Fehler
                    (document.getElementById('quantity') as HTMLInputElement).value = ""; // Input-Feld leeren
                }
            }
        });
  }

  increasePortfolioStockOrder(stockAmount: number, totalPrice: number, pricePerStock: number): void {
    const stockSymbol = (document.getElementById('stock-name') as HTMLInputElement).value;

    const orderData = {
        token: this.authComponent.getCookie("token"),
        transactioncontent: {
            transactiontype: 1,
            stockamount: stockAmount,
            date: this.getCurrentDateTime(), // Methode, um das aktuelle Datum zu holen
            priceperstock: pricePerStock,
            totalprice: totalPrice,
            email: this.authComponent.getCookie("email"),
            symbol: stockSymbol
        }
    };

    this.http.put<any>("https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/order/buy", orderData, { observe: 'response' })
        .subscribe({
            next: (response) => {
                // Überprüfen, ob die Antwort erfolgreich war
                if (response.status === 200) {
                    alert("Stock order successfully increased!"); // Erfolgsmeldung anzeigen
                } else {
                    alert("Unexpected response from the server."); // Allgemeine Antwortnachricht
                }
                (document.getElementById('quantity') as HTMLInputElement).value = ""; // Input-Feld leeren

                this.getPortfolioStockData(stockSymbol, (data) => {
                    this.updateStockDisplay(stockSymbol, data.currentvalue, data.boughtvalue); // Aktienanzeige aktualisieren
                });
                this.displayAllDatabaseData(); // Alle Daten anzeigen
            },
            error: (error) => {
                if (error.status === 400 || error.status === 401 || error.status === 500) {
                    alert("An error occurred: " + error.message); // Fehlernachricht anzeigen
                    (document.getElementById('quantity') as HTMLInputElement).value = ""; // Input-Feld leeren
                } else {
                    alert("An unexpected error occurred. Status: " + error.status); // Allgemeiner Fehler
                    (document.getElementById('quantity') as HTMLInputElement).value = ""; // Input-Feld leeren
                }
            }
        });
  }

  decreasePortfolioStockOrder(stockAmount: number, totalPrice: number, pricePerStock: number): void {
    const stockSymbol = (document.getElementById('stock-name') as HTMLInputElement).value;

    const orderData = {
        token: this.authComponent.getCookie("token"),
        transactioncontent: {
            transactiontype: 0, // 0 für Verkaufsauftrag
            stockamount: stockAmount,
            date: this.getCurrentDateTime(), // Methode, um das aktuelle Datum zu holen
            priceperstock: pricePerStock,
            totalprice: totalPrice,
            email: this.authComponent.getCookie("email"),
            symbol: stockSymbol
        }
    };

    this.http.put<any>("https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/order/sell", orderData, { observe: 'response' })
        .subscribe({
            next: (response) => {
                // Überprüfen, ob die Antwort erfolgreich war
                if (response.status === 200) {
                    alert("Stock order successfully decreased!"); // Erfolgsmeldung anzeigen
                } else {
                    alert("Unexpected response from the server."); // Allgemeine Antwortnachricht
                }
                (document.getElementById('quantity') as HTMLInputElement).value = ""; // Input-Feld leeren

                this.getPortfolioStockData(stockSymbol, (data) => {
                    this.updateStockDisplay(stockSymbol, data.currentvalue, data.boughtvalue); // Aktienanzeige aktualisieren
                });
                this.displayAllDatabaseData(); // Alle Daten anzeigen
            },
            error: (error) => {
                if (error.status === 400 || error.status === 401 || error.status === 500) {
                    alert("An error occurred: " + error.message); // Fehlernachricht anzeigen
                    (document.getElementById('quantity') as HTMLInputElement).value = ""; // Input-Feld leeren
                } else {
                    alert("An unexpected error occurred. Status: " + error.status); // Allgemeiner Fehler
                    (document.getElementById('quantity') as HTMLInputElement).value = ""; // Input-Feld leeren
                }
            }
        });
  }

  deletePortfolioStockOrder(stockAmount: number, totalPrice: number, pricePerStock: number): void {
    const stockSymbol = (document.getElementById('stock-name') as HTMLInputElement).value;

    const orderData = {
        token: this.authComponent.getCookie("token"),
        transactioncontent: {
            transactiontype: 0, // 0 für Verkaufsauftrag
            stockamount: stockAmount,
            date: this.getCurrentDateTime(), // Methode, um das aktuelle Datum zu holen
            priceperstock: pricePerStock,
            totalprice: totalPrice,
            email: this.authComponent.getCookie("email"),
            symbol: stockSymbol
        }
    };

    this.http.request('DELETE', "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/order", {
        body: orderData,
        observe: 'response'
    }).subscribe({
        next: (response) => {
            // Überprüfen, ob die Antwort erfolgreich war
            if (response.status === 200) {
                (document.getElementById('quantity') as HTMLInputElement).value = ""; // Input-Feld leeren
                // Element mit dem Aktien-Symbol aus dem DOM entfernen
                const stockElement = document.getElementById(stockSymbol.trim());
                if (stockElement) {
                    stockElement.remove();
                }
                this.displayAllDatabaseData(); // Alle Daten anzeigen
            } else {
                alert("Unexpected response from the server."); // Allgemeine Antwortnachricht
            }
        },
        error: (error) => {
            if (error.status === 400 || error.status === 401 || error.status === 500) {
                alert("An error occurred: " + error.message); // Fehlernachricht anzeigen
                (document.getElementById('quantity') as HTMLInputElement).value = ""; // Input-Feld leeren
            } else {
                alert("An unexpected error occurred. Status: " + error.status); // Allgemeiner Fehler
                (document.getElementById('quantity') as HTMLInputElement).value = ""; // Input-Feld leeren
            }
        }
    });
  }

  checkStockInDB(stockName: string, callback: (exists: boolean) => void): void {
    const email = this.authComponent.getCookie("email");
    const token = this.authComponent.getCookie("token");
    const url = `https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/stock?email=${email}&token=${token}&symbol=${stockName}`;

    this.http.get(url, { observe: 'response' }).subscribe({
        next: (response) => {
            if (response.status === 200) {
                callback(true);
            }
        },
        error: (error) => {
            if (error.status === 404) {
                callback(false);
            } else {
                callback(true);
            }
        }
    });
  }

  getCompanyValueFromAPI(stockName: string, callback: (companyName: string) => void): void {
    const url = `https://api.polygon.io/v3/reference/tickers/${stockName}?apiKey=${APIKEY}`;

    this.http.get<any>(url).subscribe({
        next: (data) => {
            if (data.status === 'OK' && data.results) {
                const name = data.results.name;
                callback(name);
            } else {
                callback(""); 
            }
        },
        error: (error) => {
            console.error('Error fetching company name:', error);
            callback(""); 
        }
    });
  }

  addNewStockIfNotExists(stockName: string, stockPrice: number): void {
    this.checkStockInDB(stockName, (stockExists: boolean) => {
        if (!stockExists) {
            this.getCompanyValueFromAPI(stockName, (companyName: string) => {
                const url = "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/stock";
                const payload = {
                    session: {
                        token: this.authComponent.getCookie("token"),
                        email: this.authComponent.getCookie("email"),
                    },
                    stock: {
                        symbol: stockName,
                        stockprice: stockPrice,
                        name: companyName,
                    },
                };

                this.http.post(url, payload, {
                    headers: {
                        'Accept': 'application/json',
                        'Content-Type': 'application/json',
                    },
                }).subscribe({
                    next: (response) => {
                        console.log("Stock inserted successfully:", response);
                    },
                    error: (error) => {
                        if (error.status === 401 || error.status === 500) {
                            console.log(JSON.parse(error.error).answer);
                        } else {
                            console.log("An unexpected error occurred. Status: " + error.status);
                        }
                    },
                });
            });
        } else {
            console.log("Stock already exists in the database.");
        }
    });
  }

  getStockPriceFromAPI(stockName: string): void {
    const url = `https://api.polygon.io/v2/aggs/ticker/${stockName}/prev?adjusted=true&apiKey=${APIKEY}`;

    this.http.get<any>(url).subscribe({
        next: (data) => {
            // Temporarily, as the price in the database is not retrieved and updated in the db anyway. 
            // However, the database column exists if the call is changed to the commented-out code in the future 
            if (data.status === 'OK' && data.results && data.results.length > 0) {
                this.addNewStockIfNotExists(stockName, 0.0);
                const closeValue = parseFloat(data.results[0].c);
                const roundedCloseValue = this.roundToTwoDecimalPlaces(closeValue);
                this.displayStockPrice(roundedCloseValue.toString());
            } else if (!data.results) {
                this.displayStockPrice('Stock not found or no data available.');
            } else if (data.results.length === 0) {
                this.displayStockPrice('No closing price data available.');
            } else {
                this.displayStockPrice('Unknown error retrieving data.');
            }
        },
        error: (error) => {
            if (error.status === 429) {
                this.displayStockPrice('Too many requests. Please try again later.');
            } else {
                console.error('Error:', error);
                this.displayStockPrice('Error retrieving data. Please try again later.');
            }
        }
    });
  }

  getStockName(): string {
    const stockNameLabel = document.getElementById("stock-name") as HTMLInputElement; // Typ angeben
    return stockNameLabel ? stockNameLabel.value : ''; // Rückgabe des Wertes oder leerer String, wenn nicht gefunden
  }

  handleInputKeypress(e: KeyboardEvent): void {
    if (e.key === 'Enter') { 
        this.getStockPriceFromAPI(this.getStockName());
    }
  }

  showStockPriceViaEvent(): void {
    const inputField = document.getElementById('stock-name') as HTMLInputElement; // Typen festlegen
    if (inputField) {
        inputField.addEventListener('keypress', this.handleInputKeypress.bind(this)); // Sicherstellen, dass der Kontext beibehalten wird
        inputField.addEventListener('blur', () => this.getStockPriceFromAPI(this.getStockName())); // Verwendung von 'this'
    }
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

