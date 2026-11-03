import { Locator, Page } from '@playwright/test';

export class CheckoutPage {
  constructor(private readonly page: Page) {}

  async fillInfoAndContinue(firstName: string, lastName: string, postalCode: string): Promise<void> {
    await this.page.getByTestId('firstName').fill(firstName);
    await this.page.getByTestId('lastName').fill(lastName);
    await this.page.getByTestId('postalCode').fill(postalCode);
    await this.page.getByTestId('continue').click();
  }

  async finish(): Promise<void> {
    await this.page.getByTestId('finish').click();
  }

  successTitle(): Locator {
    return this.page.getByTestId('complete-header');
  }

  errorMessage(): Locator {
    return this.page.getByTestId('error');
  }

  lastNameInput(): Locator {
    return this.page.getByTestId('lastName');
  }
}
