import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarComponent } from './navbar/navbar.component'; // Importiere die Navbar
import { VorHomeNavbarComponent } from './navbar-vorhome/navbar-vorhome.component'; // Importiere die VorHomeNavbar
// import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    NavbarComponent, // Binde die Navbar hier ein
    VorHomeNavbarComponent, // Binde die VorHomeNavbar hier ein
    // HttpClientModule
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  constructor(private router: Router) {}

  isVorHomePage(): boolean {
    return this.router.url.includes('/vor-home');
  }

  isAuth(): boolean {
    return this.router.url.includes('/auth');
  }
}

