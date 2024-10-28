import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthComponent } from '../auth/auth.component';

@Component({
  selector: 'app-portfolio',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './portfolio.component.html',
  styleUrls: ['./portfolio.component.css']
})
export class PortfolioComponent implements OnInit {
  private authComponent: AuthComponent;
  constructor(public router: Router) {
    this.authComponent = new AuthComponent(router);
  }

  ngOnInit(): void {
    this.authComponent.disableGoBackFunction();
  }
}

