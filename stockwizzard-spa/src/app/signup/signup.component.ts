import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { HttpClientModule, HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AuthComponent } from '../auth/auth.component';

@Component({
  selector: 'an-signup',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, HttpClientModule],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {
  showNavbar = false;
  private authComponent: AuthComponent;
  constructor(public router: Router, private http: HttpClient) {
    this.authComponent = new AuthComponent();
  }

  onRegister(form: any): void {
    const firstname = form.value.firstname;
    const lastname = form.value.lastname;
    const email = form.value.email;
    const password = form.value.password;
    const budget = form.value.budget;

    const settingsRegister = {
      url: "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/user",
      method: "POST",
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        firstname: firstname, 
        lastname: lastname,
        email: email,
        password: password,
        budget: budget
      })
    };
    
    this.http.post(settingsRegister.url, settingsRegister.body, { headers: settingsRegister.headers })
      .subscribe(
        (data: any) => {
          // Setze die Cookies
          alert(data.answer);
        },
        (error) => {
          if (error.status === 409 || error.status === 500) {
            alert(error.error.answer);
          } else {
            alert("An unexpected error occurred. Status: " + error.status);
          }
        }
      );
  }
}
