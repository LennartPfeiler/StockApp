import { Component } from '@angular/core';
import { RouterOutlet , RouterLink } from '@angular/router';

@Component({
  selector: 'an-auth',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './auth.component.html',
  styleUrl: './auth.component.css'
})
export class AuthComponent {

}
