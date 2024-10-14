import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VorHomeComponent } from './vor-home.component';

describe('VorHomeComponent', () => {
  let component: VorHomeComponent;
  let fixture: ComponentFixture<VorHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VorHomeComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VorHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
