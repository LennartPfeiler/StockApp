import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthComponent } from '../auth/auth.component';

@Component({
  selector: 'app-tracker',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './tracker.component.html',
  styleUrls: ['./tracker.component.css']
})
export class TrackerComponent implements OnInit {
  private authComponent: AuthComponent;
  constructor(public router: Router) {
    this.authComponent = new AuthComponent(router);
  }

  ngOnInit(): void {
    this.loadTradingViewWidget();
    this.authComponent.disableGoBackFunction();
  }

  loadTradingViewWidget(): void {
    const script = document.createElement('script');
    script.src = 'https://s3.tradingview.com/tv.js';
    script.async = true;
    script.onload = () => {
      new (window as any).TradingView.widget({
        autosize: true,
        symbol: "NASDAQ:AAPL",
        timezone: "Etc/UTC",
        theme: "dark",
        style: "1",
        locale: "en",
        toolbar_bg: "#f1f3f6",
        enable_publishing: true,
        withdateranges: true,
        range: "YTD",
        hide_side_toolbar: false,
        allow_symbol_change: true,
        details: true,
        hotlist: true,
        calendar: true,
        show_popup_button: true,
        popup_width: "1000",
        popup_height: "24650",
        container_id: "tradingview_1dcca"
      });
    };
    document.body.appendChild(script);
  }
}
