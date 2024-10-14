import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-vorhome-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar-vorhome.component.html',  // Korrektur: Verweise auf die HTML-Datei
  styleUrls: ['./navbar-vorhome.component.css']    // CSS-Datei bleibt hier
})
export class VorHomeNavbarComponent {}
