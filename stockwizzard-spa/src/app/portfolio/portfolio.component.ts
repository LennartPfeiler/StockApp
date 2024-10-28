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
  transactions: any[] = [];
  loading: boolean = true;
  error: any;
  private authComponent: AuthComponent;
  constructor(public router: Router, private apollo: Apollo, private http: HttpClient) {
    this.authComponent = new AuthComponent(router);
  }

  ngOnInit(): void {
    this.authComponent.disableGoBackFunction();
    this.loadTransactions();
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
          this.transactions = data.transactions || [];
          this.displayTransactionHistory(this.transactions)
        }
      )
      )
      .subscribe({
        next: () => {
          // console.log("AHHHHHHH");
          // console.log(transactions);
          // this.transactions = transactions || [];
          // this.loading = false;
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
    const transactionHistoryContainer = document.querySelector('#transaction-history') as HTMLElement;
    if (!transactionHistoryContainer) console.log("KLLLLLLLLLLLW");

    transactionHistoryContainer.innerHTML = ''; 
    const heading = document.createElement('h2');
    heading.textContent = 'Transaction History';
    transactionHistoryContainer.appendChild(heading);

    transactions.forEach(transaction => {
        const transactionDiv = document.createElement('div');
        const type = transaction.transactionType === 1 ? 'Bought' : 'Sold'; // Ternäre Bedingung
        transactionDiv.textContent = `${type} ${transaction.stockAmount} ${transaction.symbol} at price of ${transaction.pricePerStock}$ for ${transaction.totalPrice}$`;
        transactionHistoryContainer.appendChild(transactionDiv);
    });
  }
<<<<<<< HEAD
=======
    // getAllTransactions(): void {
    //   const settingsGetAllTransactions = {
    //     url: "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/transactions?email=" + this.authComponent.getCookie("email") + "&token=" + this.authComponent.getCookie("token") + "&sortby=date",
    //     method: "GET",
    //     headers: {
    //       'Accept': 'application/json',
    //       'Content-Type': 'application/json'
    //     }
    //   };
      
    //   this.http.post(settingsLogin.url, settingsLogin.body, { headers: settingsLogin.headers })
    //     .subscribe(
    //       (data: any) => {
    //         // Setze die Cookies
    //         this.authComponent.setCookie("token", data.token);
    //         this.authComponent.setCookie("firstname", data.user.firstname);
    //         this.authComponent.setCookie("lastname", data.user.lastname);
    //         this.authComponent.setCookie("email", data.user.email);
    //         alert("Login successfully");
    //         this.router.navigate(['/content/home']); // Navigiere zur Home-Seite
    //       },
    //       (error) => {
    //         if (error.status === 400 || error.status === 401 || error.status === 500) {
    //           alert(error.error.answer);
    //         } else {
    //           alert("An unexpected error occurred. Status: " + error.status);
    //         }
    //       }
    //     );
    // }
>>>>>>> cc6f00765beb22d4954a5d511a79cac822f6e9af
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