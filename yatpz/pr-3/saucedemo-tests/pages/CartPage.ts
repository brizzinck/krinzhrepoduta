import { Locator, Page } from '@playwright/test';

export class CartPage {
  constructor(private readonly page: Page) {}

  items(): Locator {
    return this.page.getByTestId('inventory-item');
  }

  async checkout(): Promise<void> {
    await this.page.getByTestId('checkout').click();
  }
}
