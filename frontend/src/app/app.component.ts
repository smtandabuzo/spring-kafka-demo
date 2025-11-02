import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { EventFormComponent } from './components/event-form/event-form';
import { EventListComponent } from './components/event-list/event-list';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
  standalone: true,
  imports: [CommonModule, RouterOutlet, EventFormComponent, EventListComponent]
})
export class AppComponent {
  title = 'Kafka Event Tracker';

  onEventSubmitted(): void {
    // This will be called when a new event is submitted
    // The event list will automatically refresh due to the event emitter
  }
}
