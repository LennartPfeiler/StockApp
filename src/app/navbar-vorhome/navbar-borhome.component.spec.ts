import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NavbarBorhomeComponent } from './navbar-borhome.component';

describe('NavbarBorhomeComponent', () => {
  let component: NavbarBorhomeComponent;
  let fixture: ComponentFixture<NavbarBorhomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NavbarBorhomeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NavbarBorhomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
