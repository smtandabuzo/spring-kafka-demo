import { Component, OnInit } from '@angular/core';
import { Event } from '../../services/event';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { EventService } from '../../services/event';

@Component({
  selector: 'app-event-list',
  templateUrl: './event-list.html',
  styleUrls: ['./event-list.scss'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatListModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatSelectModule
  ]
})
export class EventListComponent implements OnInit {
  events: any[] = [];
  loading = false; // Start with loading false
  initialLoad = true; // Track if it's the initial load
  error: string | null = null;
  userIdFilter = '';
  eventTypeFilter = '';
  eventTypes = [
    'PAGE_VIEW', 'SEARCH', 'CLICK',
    'ADD_TO_CART', 'REMOVE_FROM_CART', 'VIEW_ITEM',
    'VIEW_CART', 'INITIATE_CHECKOUT', 'ADD_PAYMENT_INFO',
    'PURCHASE', 'ADD_TO_WISHLIST', 'SIGN_UP'
  ];

  constructor(
    private eventService: EventService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    // Don't load events on init
    // User needs to enter a user ID first
  }

  loadEvents(): void {
    if (!this.userIdFilter) {
      this.showError('Please enter a User ID');
      return;
    }

    this.initialLoad = false; // Mark that we've started loading at least once

    this.loading = true;
    this.error = null;
    this.events = []; // Clear previous events

    // Add a small delay to prevent flickering on fast responses
    const startTime = Date.now();
    const MIN_LOADING_TIME = 300; // milliseconds

    const subscription = this.eventService.getEventsByUser(this.userIdFilter, this.eventTypeFilter)
      .subscribe({
        next: (events) => {
          const elapsed = Date.now() - startTime;
          const remainingTime = Math.max(0, MIN_LOADING_TIME - elapsed);

          setTimeout(() => {
            try {
              this.events = Array.isArray(events) ? events : [];
              this.error = null;
            } catch (e) {
              console.error('Error processing events:', e);
              this.error = 'Error processing events';
              this.events = [];
            }
            this.loading = false;
            subscription.unsubscribe();
          }, remainingTime);
        },
        error: (err) => {
          const elapsed = Date.now() - startTime;
          const remainingTime = Math.max(0, MIN_LOADING_TIME - elapsed);

          setTimeout(() => {
            this.error = 'Failed to load events. Please try again.';
            this.events = [];
            this.loading = false;
            subscription.unsubscribe();
          }, remainingTime);
        }
      });
  }

  clearEventLog(): void {
    this.loading = true;
    this.eventService.clearEventLog().subscribe({
      next: () => {
        this.events = [];
        this.loading = false;
        this.snackBar.open('Event log cleared successfully', 'Close', {
          duration: 3000
        });
      },
      error: (err) => {
        this.error = 'Failed to clear event log';
        this.loading = false;
        console.error('Error clearing event log:', err);
        this.showError(this.error);
      }
    });
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });
  }

  trackByEventId(index: number, event: Event): string {
    return event.eventId;
  }

  clearFilters(): void {
    this.userIdFilter = '';
    this.eventTypeFilter = '';
    this.events = [];
  }

  onFilterChange(): void {
    this.loadEvents();
  }

  clearFilter(): void {
    this.userIdFilter = '';
    this.loadEvents();
  }
}
