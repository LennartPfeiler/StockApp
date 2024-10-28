import { Component, OnInit } from '@angular/core';
import { AuthComponent } from '../auth/auth.component';
import { RouterModule, Router } from '@angular/router';
import { HttpClientModule, HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [HttpClientModule, RouterModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  private authComponent: AuthComponent;
  constructor(public router: Router, private http: HttpClient) {
    this.authComponent = new AuthComponent(router);
  }

  ngOnInit(): void {
    this.authComponent.disableGoBackFunction();
    this.setProfileValues();
  }

  setProfileValues(): void {
    const firstNameInput = document.getElementById('first-name') as HTMLInputElement;
    const lastNameInput = document.getElementById('last-name') as HTMLInputElement;
    const emailInput = document.getElementById('email') as HTMLInputElement;

    if (firstNameInput) {
      firstNameInput.value = this.authComponent.getCookie('firstname');
    }
    if (lastNameInput) {
      lastNameInput.value = this.authComponent.getCookie('lastname');
    }
    if (emailInput) {
      emailInput.value = this.authComponent.getCookie('email');
    }
  }

  editUser(): void {
    const firstNameInput = document.getElementById('first-name') as HTMLInputElement;
    const lastNameInput = document.getElementById('last-name') as HTMLInputElement;
    const emailInput = document.getElementById('email') as HTMLInputElement;
    const budgetSelect = document.getElementById('amount-selection') as HTMLSelectElement;
    const firstname = firstNameInput.value.trim();
    const lastname = lastNameInput.value.trim();
    const email = emailInput.value.trim();
    const budgetValue = budgetSelect.value.trim();
    const budget = (budgetValue !== '' && !isNaN(parseFloat(budgetValue))) ? parseFloat(budgetValue) : 0;

    // Überprüfen, ob der Wert numerisch ist und nicht leer, sonst auf 0 setzen
    const editRegister = {
      url: "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/user",
      method: "PUT",
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        token: this.authComponent.getCookie("token"),
            currentmail: this.authComponent.getCookie("email"),
            user: {
                firstname: firstname,
                lastname: lastname,
                email: email,
                budget: budget
            }
      })
    };
    
    this.http.put(editRegister.url, editRegister.body, { headers: editRegister.headers })
      .subscribe(
        (data: any) => {
          // Setze die Cookies
          console.log(data);
          this.authComponent.setCookie("firstname", data.firstname);
          this.authComponent.setCookie("lastname", data.lastname);
          this.authComponent.setCookie("email", data.email);
          alert("User edited successfully");
        },
        (error) => {
          if (error.status === 401 || error.status === 409 || error.status === 500) {
            alert(error.error.answer);
          } else {
            alert("An unexpected error occurred. Status: " + error.status);
          }
        }
      );
  }
}
