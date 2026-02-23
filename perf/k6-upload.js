import http from "k6/http";
import { check, sleep } from "k6";

const filePath = __ENV.K6_FILE_PATH || "perf/sample-upload.txt";
const fileContent = open(filePath, "b");
const baseUrl = __ENV.K6_BASE_URL || "http://localhost:8065";
const token = __ENV.K6_TOKEN || "";

export const options = {
  vus: 10,
  duration: "30s",
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<800"],
  },
};

export default function () {
  const payload = {
    file: http.file(fileContent, "sample-upload.txt", "text/plain"),
    expirationDays: "1",
  };

  const params = {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  };

  const response = http.post(`${baseUrl}/api/files/upload`, payload, params);
  check(response, {
    "upload status is 201": (r) => r.status === 201,
  });
  sleep(1);
}
