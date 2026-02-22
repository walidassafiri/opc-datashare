import { TestBed } from "@angular/core/testing";
import { HttpClientTestingModule, HttpTestingController } from "@angular/common/http/testing";
import { FileService } from "./file.service";

describe("FileService", () => {
  let service: FileService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(FileService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it("calls upload endpoint with multipart", () => {
    const file = new File(["hello"], "hello.txt", { type: "text/plain" });

    service.upload(file, 3, "secret123", ["demo", "qa"]).subscribe((res) => {
      expect(res.token).toBe("tok123");
    });

    const req = httpMock.expectOne("/api/files/upload");
    expect(req.request.method).toBe("POST");
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush({ token: "tok123" });
  });

  it("calls download endpoint with password query param", () => {
    service.downloadFile("abc123", "secret123").subscribe();

    const req = httpMock.expectOne("/api/files/download/abc123?password=secret123");
    expect(req.request.method).toBe("GET");
    expect(req.request.responseType).toBe("blob");
    req.flush(new Blob(["content"], { type: "text/plain" }));
  });

  it("calls history endpoint", () => {
    service.getHistory().subscribe((res) => {
      expect(res.length).toBe(1);
      expect(res[0].token).toBe("tok1");
    });

    const req = httpMock.expectOne("/api/files/history");
    expect(req.request.method).toBe("GET");
    req.flush([{ token: "tok1" }]);
  });

  it("calls file info endpoint", () => {
    service.getFileInfo("abc123").subscribe((res) => {
      expect(res.filename).toBe("demo.txt");
    });

    const req = httpMock.expectOne("/api/files/info/abc123");
    expect(req.request.method).toBe("GET");
    req.flush({ filename: "demo.txt" });
  });

  it("calls delete endpoint", () => {
    service.deleteFile("abc123").subscribe((res) => {
      expect(res).toEqual({});
    });

    const req = httpMock.expectOne("/api/files/abc123");
    expect(req.request.method).toBe("DELETE");
    req.flush({});
  });
});
