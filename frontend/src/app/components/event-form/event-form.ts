import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { Category } from '../../services/event';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { EventService } from '../../services/event';

interface EventType {
  value: string;
  viewValue: string;
  icon: string;
}

interface Currency {
  code: string;
  symbol: string;
}

interface EventFormType {
  userId: FormControl<string>;
  eventType: FormControl<EventType['value'] | null>;
  pageUrl: FormControl<string | null>;
  pageTitle: FormControl<string | null>;
  itemId: FormControl<string | null>;
  itemType: FormControl<string | null>;
  category: FormControl<string | null>;
  price: FormControl<number | null>;
  currency: FormControl<string>;
  quantity: FormControl<number>;
  orderId: FormControl<string | null>;
}
//}

@Component({
  selector: 'app-event-form',
  templateUrl: './event-form.html',
  styleUrls: ['./event-form.scss'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatIconModule,
    MatDividerModule
  ]
})
export class EventFormComponent implements OnInit {
  // Form controls
  eventForm!: FormGroup<EventFormType>;
  loading = false;

  // Form controls
  pageUrl: FormControl<string | null> = new FormControl('');
  pageTitle: FormControl<string | null> = new FormControl('');
  itemId: FormControl<string | null> = new FormControl('');

  // Constants and configuration
  public itemTypes = ['product', 'service', 'subscription', 'digital', 'physical'] as const;

  @Output() eventSubmitted = new EventEmitter<void>();

  eventTypes: EventType[] = [
    {value: 'page_view', viewValue: 'Page View', icon: 'visibility'},
    {value: 'add_to_cart', viewValue: 'Add to Cart', icon: 'add_shopping_cart'},
    {value: 'purchase', viewValue: 'Purchase', icon: 'shopping_bag'}
  ];

  currencies: Currency[] = [
    {code: 'USD', symbol: '$'},
    {code: 'EUR', symbol: '€'},
    {code: 'GBP', symbol: '£'},
    {code: 'JPY', symbol: '¥'},
    {code: 'CAD', symbol: 'CA$'},
    {code: 'AUD', symbol: 'A$'}
  ];

  categories: Category[] = [
    {id: 'electronics', name: 'Electronics'},
    {id: 'clothing', name: 'Clothing'},
    {id: 'books', name: 'Books'},
    {id: 'home', name: 'Home & Garden'},
    {id: 'sports', name: 'Sports & Outdoors'}
  ];

  defaultCurrency = this.currencies[0];

  constructor(
    private fb: FormBuilder,
    private eventService: EventService,
    private snackBar: MatSnackBar
  ) {
  }

  getCurrencySymbol(): string {
    const currencyCode = this.eventForm?.get('currency')?.value || this.defaultCurrency.code;
    const currency = this.currencies.find(c => c.code === currencyCode);
    return currency ? currency.symbol : this.defaultCurrency.symbol;
  }

  getCurrencyPlaceholder(): string {
    return `0.00 ${this.getCurrencySymbol()}`;
  }

  onCurrencyChange(): void {
    // This will trigger the price field to update its display
    this.eventForm.get('price')?.updateValueAndValidity();
  }

  // Helper method to get the event type value
  private getEventTypeValue(eventType: string | null): EventType | null {
    if (!eventType) return null;
    return this.eventTypes.find(et => et.value === eventType) || null;
  }

  ngOnInit(): void {
    this.eventForm = new FormGroup<EventFormType>({
      userId: new FormControl('', {nonNullable: true, validators: [Validators.required]}),
      eventType: new FormControl<string | null>(null, {nonNullable: false, validators: [Validators.required]}),
      pageUrl: new FormControl(''),
      pageTitle: new FormControl(''),
      itemId: new FormControl(''),
      itemType: new FormControl(''),
      category: new FormControl(''),
      price: new FormControl<number | null>(null),
      currency: new FormControl(this.defaultCurrency.code, {nonNullable: true}),
      quantity: new FormControl(1, {nonNullable: true, validators: [Validators.required, Validators.min(1)]}),
      orderId: new FormControl('')
    });

    // Update price placeholder when currency changes
    this.eventForm.get('currency')?.valueChanges.subscribe(() => {
      this.eventForm.get('price')?.updateValueAndValidity();
    });

    this.eventForm.get('eventType')?.valueChanges.subscribe((eventTypeValue: string | null) => {
      if (eventTypeValue) {
        this.updateFormForEventType(eventTypeValue);
      }
    });
  }

  private updateFormForEventType(eventType: string): void {
    // Clear all validators first
    const controls = {
      pageUrl: this.eventForm.get('pageUrl'),
      pageTitle: this.eventForm.get('pageTitle'),
      itemId: this.eventForm.get('itemId'),
      itemType: this.eventForm.get('itemType'),
      category: this.eventForm.get('category'),
      price: this.eventForm.get('price'),
      currency: this.eventForm.get('currency'),
      quantity: this.eventForm.get('quantity'),
      orderId: this.eventForm.get('orderId')
    };

    // Clear validators and reset values
    Object.values(controls).forEach(control => {
      if (control) {
        control.clearValidators();
        control.updateValueAndValidity();
      }
    });

    // Set default values
    this.eventForm.patchValue({
      pageUrl: '',
      pageTitle: '',
      itemId: '',
      itemType: '',
      category: null,
      price: null,
      orderId: ''
    });

    // Set validators based on event type
    switch (eventType) {
      case 'page_view':
        controls.pageUrl?.setValidators([
          Validators.required,
          Validators.pattern('^https?://.+')
        ]);
        controls.pageTitle?.setValidators([
          Validators.required,
          Validators.minLength(3)
        ]);
        break;
      case 'add_to_cart':
        controls.itemId?.setValidators([
          Validators.required,
          Validators.minLength(2)
        ]);
        controls.itemType?.setValidators([Validators.required]);
        controls.category?.setValidators([Validators.required]);
        controls.price?.setValidators([
          Validators.required,
          Validators.min(0.01),
          Validators.pattern(/^\d+(\.\d{1,2})?$/)
        ]);
        controls.quantity?.setValidators([
          Validators.required,
          Validators.min(1),
          Validators.pattern('^[1-9]\\d*$')
        ]);
        controls.currency?.setValidators([Validators.required]);
        break;
      case 'purchase':
        controls.orderId?.setValidators([
          Validators.required,
          Validators.minLength(5)
        ]);
        break;
    }

    // Update validation
    Object.values(controls).forEach(control => {
      control?.updateValueAndValidity();
    });
  }

  onSubmit(): void {
    if (this.eventForm.valid) {
      const formValue = this.eventForm.getRawValue();
      const eventData: any = {
        timestamp: new Date().toISOString()
      };

      if (formValue.eventType === 'page_view') {
        eventData.pageUrl = formValue.pageUrl || '';
        eventData.pageTitle = formValue.pageTitle || '';
      } else if (formValue.eventType === 'add_to_cart') {
        eventData.itemId = formValue.itemId || '';
        eventData.itemType = formValue.itemType || '';

        // Add category information if selected
        if (formValue.category) {
          const selectedCategory = this.categories.find(cat => cat.id === formValue.category);
          eventData.category = {
            id: formValue.category,
            name: selectedCategory ? selectedCategory.name : 'Unknown'
          };
        }

        eventData.price = formValue.price || 0;
        eventData.currency = formValue.currency || 'USD';
        eventData.quantity = formValue.quantity || 1;
      } else if (formValue.eventType === 'purchase') {
        eventData.orderId = formValue.orderId || '';
      }

      this.loading = true;
      this.eventService.trackEvent(formValue.userId || '', formValue.eventType || '', eventData)
        .subscribe({
          next: () => {
            this.snackBar.open('Event tracked successfully!', 'Close', {duration: 3000});
            this.eventForm.reset({
              eventType: formValue.eventType,
              quantity: 1,
              currency: this.defaultCurrency.code
            });
            this.eventSubmitted.emit();
          },
          error: (error) => {
            console.error('Error tracking event:', error);
            this.snackBar.open(
              `Error tracking event: ${error.error?.message || error.message || 'Unknown error'}`,
              'Close',
              {duration: 5000}
            );
          },
          complete: () => {
            this.loading = false;
          }
        });
    }
  }
}
