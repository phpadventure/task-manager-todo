import { test, expect } from '@playwright/test';

test('should redirect to login when not authenticated', async ({ page }) => {
  await page.goto('/');
  await expect(page).toHaveURL(/.*login/);
});

test('should display login page with OAuth buttons', async ({ page }) => {
  await page.goto('/login');
  await expect(page.locator('button:has-text("Sign in with Google")')).toBeVisible();
  await expect(page.locator('button:has-text("Sign in with GitHub")')).toBeVisible();
});
