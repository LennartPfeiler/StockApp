import { Component } from '@angular/core';
import { NavbarComponent } from "../navbar/navbar.component";
import { RouterOutlet , RouterLink } from '@angular/router';
import { HomeComponent } from "../home/home.component";
import { VorHomeComponent } from "../vor-home/vor-home.component";
import { PortfolioComponent } from "../portfolio/portfolio.component";
import { ProfileComponent } from "../profile/profile.component";
import { TrackerComponent } from "../tracker/tracker.component";

@Component({
  selector: 'an-content',
  standalone: true,
  imports: [NavbarComponent, RouterOutlet, HomeComponent, VorHomeComponent, PortfolioComponent, ProfileComponent, TrackerComponent],
  templateUrl: './content.component.html',
  styleUrl: './content.component.css'
})
export class ContentComponent {

}
