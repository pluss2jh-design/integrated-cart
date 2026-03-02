---
name: playwright
description: >
  Use this skill when asked to automate browser interactions, write E2E tests,
  scrape web pages, or take screenshots. Triggered by phrases like "automate browser",
  "E2E test", "scrape this page", "playwright test", or "take a screenshot of".
---

# Playwright Browser Automation Skill

oh-my-opencode 환경에서 Playwright를 활용한 브라우저 자동화 스킬입니다.

## 에이전트 역할 분담

- **Oracle**: 테스트 아키텍처 설계 및 전략 수립
- **Hephaestus**: 실제 Playwright 스크립트 작성 및 실행
- **Momus**: 테스트 코드 품질 리뷰

## 설치 및 초기 설정

```bash
# Playwright 설치
npm init playwright@latest

# 또는 기존 프로젝트에 추가
npm install --save-dev @playwright/test
npx playwright install
```

## 기본 설정 (`playwright.config.ts`)

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './tests/e2e',
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:3000',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
    { name: 'mobile', use: { ...devices['iPhone 13'] } },
  ],
  webServer: {
    command: 'npm run dev',
    url: 'http://localhost:3000',
    reuseExistingServer: !process.env.CI,
  },
});
```

## Page Object Model 패턴

```typescript
// tests/pages/LoginPage.ts
import { Page, Locator } from '@playwright/test';

export class LoginPage {
  readonly emailInput: Locator;
  readonly passwordInput: Locator;
  readonly submitButton: Locator;

  constructor(private page: Page) {
    this.emailInput = page.getByRole('textbox', { name: 'Email' });
    this.passwordInput = page.getByRole('textbox', { name: 'Password' });
    this.submitButton = page.getByRole('button', { name: 'Login' });
  }

  async goto() {
    await this.page.goto('/login');
  }

  async login(email: string, password: string) {
    await this.emailInput.fill(email);
    await this.passwordInput.fill(password);
    await this.submitButton.click();
  }
}
```

## 웹 스크래핑 패턴

```typescript
import { chromium, Browser, Page } from 'playwright';

interface ScrapedItem {
  name: string;
  price: string;
  url: string;
}

async function scrapeProducts(targetUrl: string): Promise<ScrapedItem[]> {
  const browser: Browser = await chromium.launch({ headless: true });
  const page: Page = await browser.newPage();

  // 봇 감지 우회를 위한 설정
  await page.setExtraHTTPHeaders({
    'Accept-Language': 'ko-KR,ko;q=0.9,en-US;q=0.8',
  });

  await page.goto(targetUrl, { waitUntil: 'networkidle' });
  
  const items = await page.$$eval('.product-card', (elements) =>
    elements.map((el) => ({
      name: el.querySelector('.product-name')?.textContent?.trim() ?? '',
      price: el.querySelector('.price')?.textContent?.trim() ?? '',
      url: (el.querySelector('a') as HTMLAnchorElement)?.href ?? '',
    }))
  );

  await browser.close();
  return items;
}
```

## 모범 사례

1. **Locator 우선순위**: `role` > `label` > `test-id` > `css` > `xpath`
2. **data-testid 사용**: `data-testid` 속성으로 안정적인 선택자 사용
3. **환경 변수**: 테스트 URL, 계정 정보는 `.env` 파일로 관리
4. **병렬 실행**: 독립적인 테스트는 병렬로 실행하여 속도 향상
