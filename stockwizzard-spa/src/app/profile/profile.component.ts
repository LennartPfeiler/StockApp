import { Component, OnInit } from '@angular/core';
import { AuthComponent } from '../auth/auth.component';
import { RouterModule, Router } from '@angular/router';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  private authComponent: AuthComponent;
  constructor(public router: Router) {
    this.authComponent = new AuthComponent(router);
  }

  ngOnInit(): void {
    this.authComponent.disableGoBackFunction();
  }
}
