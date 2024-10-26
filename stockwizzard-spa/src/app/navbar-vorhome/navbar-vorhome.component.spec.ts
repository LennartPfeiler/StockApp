import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VorHomeNavbarComponent } from './navbar-vorhome.component';

describe('NavbarVorhomeComponent', () => {
  let component: VorHomeNavbarComponent;
  let fixture: ComponentFixture<VorHomeNavbarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VorHomeNavbarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VorHomeNavbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
