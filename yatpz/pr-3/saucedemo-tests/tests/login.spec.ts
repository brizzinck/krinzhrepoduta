import { test, expect } from '@playwright/test';
import { LoginPage } from '../pages/LoginPage';
import { InventoryPage } from '../pages/InventoryPage';

const PASSWORD = 'secret_sauce';

test.describe('Вхід', () => {
  test('1. Успішний вхід', async ({ page }) => {
    const loginPage = new LoginPage(page);
    const inventoryPage = new InventoryPage(page);

    await loginPage.open();
    await loginPage.login('standard_user', PASSWORD);

    await expect(inventoryPage.title()).toHaveText('Products');
  });

  test('2. Заблокований користувач', async ({ page }) => {
    const loginPage = new LoginPage(page);

    await loginPage.open();
    await loginPage.login('locked_out_user', PASSWORD);

    await expect(loginPage.errorMessage()).toContainText('locked out');
  });

  test('3. Порожні поля', async ({ page }) => {
    const loginPage = new LoginPage(page);

    await loginPage.open();
    await loginPage.login('', '');

    await expect(loginPage.errorMessage()).toContainText('Username is required');
  });
});
