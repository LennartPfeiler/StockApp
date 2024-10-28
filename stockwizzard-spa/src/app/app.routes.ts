import { RouterModule, Routes } from '@angular/router'; // Hinzufügen des korrekten Imports
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { PortfolioComponent } from './portfolio/portfolio.component';
import { ProfileComponent } from './profile/profile.component';
import { SignupComponent } from './signup/signup.component';
import { TrackerComponent } from './tracker/tracker.component';
import { VorHomeComponent } from './vor-home/vor-home.component';
import { AuthComponent } from './auth/auth.component';
import { ContentComponent } from './content/content.component';

export const routes: Routes = [
  // { path: '', redirectTo: '/content/vor-home', pathMatch: 'full' },
  {
    path: 'auth', component: AuthComponent,
    children: [
      { path: 'login', component: LoginComponent },
      { path: 'sign-up', component: SignupComponent }
    ]
  },
  {
    path: 'content', component: ContentComponent,
    children: [
      { path: 'home', component: HomeComponent },
      { path: 'portfolio', component: PortfolioComponent },
      { path: 'profile', component: ProfileComponent },
      { path: 'tracker', component: TrackerComponent },
      { path: 'vor-home', component: VorHomeComponent }
    ]
  },
  {path:'**', redirectTo: '/content/vor-home'}
  
];
