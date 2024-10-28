import { Component } from '@angular/core';
import { RouterOutlet , RouterLink, RouterModule, Router } from '@angular/router';

@Component({
  selector: 'an-auth',
  standalone: true,
  imports: [RouterOutlet, RouterModule],
  templateUrl: './auth.component.html',
  styleUrl: './auth.component.css'
})
export class AuthComponent {
  constructor(private router: Router){
  }

  // Get a cookie
  getCookie(cookieName: string): string {
    const name = cookieName + "=";
    const decodedCookie = decodeURIComponent(document.cookie);
    const ca = decodedCookie.split(';');
    for (const element of ca) {
        let c = element;
        while (c.charAt(0) === ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) === 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
  }

  // Set a cookie
  setCookie(cookieName: string, cookieValue: string): void {
    // First delete the cookie by setting an expired date
    document.cookie = `${cookieName}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/; SameSite=Strict`;
    // Set the new cookie value
    document.cookie = `${cookieName}=${cookieValue}; SameSite=Strict; path=/`;
  }

  disableGoBackFunction(): void {
    const token = this.getCookie('token');
    if (!token) {
      alert('Please log in first again!');
      this.router.navigate(['/content/vor-home']);
    }
  }

}
