import { TestBed } from "@angular/core/testing";
import { HttpClientTestingModule, HttpTestingController } from "@angular/common/http/testing";
import { AuthService } from "./auth.service";

describe("AuthService", () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it("stores token after login", () => {
    service.login("john@example.com", "password123").subscribe((res) => {
      expect(res.token).toBe("jwt-token");
      expect(service.getToken()).toBe("jwt-token");
      expect(service.isAuthenticated()).toBeTrue();
    });

    const req = httpMock.expectOne("/api/auth/login");
    expect(req.request.method).toBe("POST");
    req.flush({ token: "jwt-token" });
  });

  it("stores token after register", () => {
    service.register("new@example.com", "password123").subscribe((res) => {
      expect(res.token).toBe("reg-token");
      expect(service.getToken()).toBe("reg-token");
    });

    const req = httpMock.expectOne("/api/auth/register");
    expect(req.request.method).toBe("POST");
    req.flush({ token: "reg-token" });
  });

  it("calls backend logout and clears token", () => {
    service.saveToken("jwt-token");

    service.logout().subscribe(() => {
      expect(service.getToken()).toBeNull();
    });

    const req = httpMock.expectOne("/api/auth/logout");
    expect(req.request.method).toBe("POST");
    req.flush({ message: "Logged out" });
  });

  it("clears token on logoutLocal", () => {
    service.saveToken("token");
    service.logoutLocal();
    expect(service.getToken()).toBeNull();
  });
});
