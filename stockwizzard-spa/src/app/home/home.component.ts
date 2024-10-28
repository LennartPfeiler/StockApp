import { Component, OnInit } from '@angular/core';
import { AuthComponent } from '../auth/auth.component';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'an-home',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent implements OnInit{
  private authComponent: AuthComponent;
  constructor(public router: Router) {
    this.authComponent = new AuthComponent(router);
  }

  ngOnInit(): void {
    this.authComponent.disableGoBackFunction();
  }
}
