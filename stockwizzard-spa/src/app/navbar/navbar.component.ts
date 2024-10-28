import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule, HttpClient, HttpHeaders } from '@angular/common/http';
import { RouterModule, Router } from '@angular/router';
import { AuthComponent } from '../auth/auth.component';

@Component({
  selector: 'app-navbar',  // Der Selector sollte 'app-navbar' sein
  standalone: true,
  imports: [CommonModule, RouterModule, HttpClientModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  private authComponent: AuthComponent;
  constructor(public router: Router, private http: HttpClient) {
    this.authComponent = new AuthComponent();
  }

  logout(): void {
    const token = this.authComponent.getCookie('token');
    const email = this.authComponent.getCookie('email');
  
    const settingsLogout = {
      url: "https://StockWizzardBackend-grateful-platypus-pd.apps.01.cf.eu01.stackit.cloud/api/auth",
      method: "DELETE",
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        token: token,
        email: email
      })
    };
  
    const options = {
      headers: settingsLogout.headers,
      body: settingsLogout.body
    };
  
    this.http.delete(settingsLogout.url, options).subscribe(
      (data: any) => {
        alert(data.answer);
        document.cookie = 'email=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        document.cookie = 'token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        document.cookie = 'firstname=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        document.cookie = 'lastname=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
        this.router.navigate(['/content/vor-home']);
      },
      (error) => {
        if (error.status === 401 || error.status === 500) {
          alert(error.error.answer);
        } else {
          alert('An unexpected error occurred. Status: ' + error.status);
        }
      }
    );
  }
}