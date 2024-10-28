import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
// import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { AuthComponent } from '../auth/auth.component';

@Component({
  selector: 'an-login',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private authComponent: AuthComponent;
  constructor(public router: Router) {
    this.authComponent = new AuthComponent();
  }

  onSubmit() {
    console.log('Form submitted!');
    this.router.navigate(['/content/home']);
    // Hier kannst du deine Logik hinzufügen
  }

  // onLogin(form: any): void {
  //   const email = form.email.value;
  //   const password = form.password.value;

  //   const settingsLogin = {
  //     url: "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/auth",
  //     method: "POST",
  //     headers: {
  //       'Accept': 'application/json',
  //       'Content-Type': 'application/json'
  //     },
  //     body: JSON.stringify({
  //       email: email,
  //       password: password
  //     })
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
  //         this.router.navigate(['home']); // Navigiere zur Home-Seite
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
}
