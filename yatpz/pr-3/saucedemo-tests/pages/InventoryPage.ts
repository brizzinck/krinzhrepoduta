import { Locator, Page } from '@playwright/test';

export class InventoryPage {
  constructor(private readonly page: Page) {}

  title(): Locator {
    return this.page.getByTestId('title');
  }

  // картку товару шукаємо за назвою, а кнопку вже всередині неї
  async addToCart(productName: string): Promise<void> {
    await this.page
      .getByTestId('inventory-item')
      .filter({ hasText: productName })
      .getByRole('button', { name: 'Add to cart' })
      .click();
  }

  cartBadge(): Locator {
    return this.page.getByTestId('shopping-cart-badge');
  }

  async openCart(): Promise<void> {
    await this.page.getByTestId('shopping-cart-link').click();
  }
}
