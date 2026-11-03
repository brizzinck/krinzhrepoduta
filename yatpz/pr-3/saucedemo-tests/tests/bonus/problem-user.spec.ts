import { test, expect } from '../../fixtures';

test.describe('Бонус: problem_user', () => {
  // test.fail() означає: тест падає через відомий баг і набір лишається зеленим.
  // Коли сайт виправлять, тест пройде, Playwright покаже це як помилку, і позначку треба зняти.
  test('Відомий баг: поле Last Name не приймає текст на Checkout', async ({
    loginPage, inventoryPage, cartPage, checkoutPage,
  }) => {
    test.info().annotations.push({
      type: 'known bug',
      description: 'problem_user: текст із Last Name потрапляє в First Name, оформлення не завершити',
    });
    await loginPage.open();
    await loginPage.login('problem_user', 'secret_sauce');
    await inventoryPage.addToCart('Sauce Labs Backpack');
    await inventoryPage.openCart();
    await cartPage.checkout();
    await checkoutPage.fillInfoAndContinue('Test', 'User', '12345');

    // очікуємо перехід на наступний крок, тобто відсутність помилки
    test.fail();
    await expect(checkoutPage.errorMessage()).toBeHidden();
  });
});
