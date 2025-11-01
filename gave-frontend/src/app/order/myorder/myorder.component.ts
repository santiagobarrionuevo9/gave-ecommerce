import { Component, OnInit } from '@angular/core';
import { OrderService } from '../../service/order.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-myorder',
  standalone: true,
  imports: [CommonModule,FormsModule],
  templateUrl: './myorder.component.html',
  styleUrl: './myorder.component.css'
})
export class MyorderComponent implements OnInit {
  email = '';
  orders: any[] = [];
  constructor(private svc: OrderService) {}
  ngOnInit(){}

  load(){
    this.svc.myOrders(this.email, 0, 20).subscribe(p => this.orders = p.content);
  }
}
