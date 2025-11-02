import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export type Category = {
  id: string;
  name: string;
};


export interface Event {
  eventId: string;
  userId: string;
  eventType: string;
  timestamp: string;
  data: any;
}

export interface EventData {
  [key: string]: any;
  sessionId?: string;
  userAgent?: string;
  ipAddress?: string;
  pageUrl?: string;
  pageTitle?: string;
  itemId?: string;
  itemType?: string;
  price?: number;
  quantity?: number;
  orderId?: string;
}

@Injectable({
  providedIn: 'root',
})
export class EventService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  getEvents(): Observable<any> {
    return this.http.get(`${this.apiUrl}/events`);
  }

  getEventsByUser(userId: string, eventType?: string): Observable<any> {
    let params = new HttpParams().set('userId', userId);
    if (eventType) {
      params = params.set('eventType', eventType);
    }
    return this.http.get(`${this.apiUrl}/events`, { params });
  }

  clearEventLog(): Observable<any> {
    return this.http.get(`${this.apiUrl}/events/clear`);
  }

  trackEvent(userId: string, eventType: string, eventData: any) {
    const requestBody = {
      userId,
      eventType,
      ...eventData
    };

    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }),
      withCredentials: true
    };

    if (eventData.item) {
      const { item, ...rest } = eventData;
      return this.http.post<any>(
        `${this.apiUrl}/events`,
        {
          ...rest,
          userId,
          eventType,
          ...item
        },
        httpOptions
      ).pipe(
        catchError(error => {
          console.error('Error tracking event:', error);
          return throwError(() => error);
        })
      );
    }

    return this.http.post<any>(
      `${this.apiUrl}/events`,
      requestBody,
      httpOptions
    ).pipe(
      catchError(error => {
        console.error('Error tracking event:', error);
        return throwError(() => error);
      })
    );
  }
}
