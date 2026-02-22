import "zone.js/testing";
import { getTestBed } from "@angular/core/testing";
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from "@angular/platform-browser-dynamic/testing";

getTestBed().initTestEnvironment(
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting()
);

// Explicit imports keep the setup compatible when require.context is unavailable.
import "./app/auth/auth.guard.spec";
import "./app/auth/auth.service.spec";
import "./app/services/file.service.spec";
