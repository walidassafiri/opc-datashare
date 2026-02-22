describe("Validation flow: forbidden extension upload", () => {
  it("should reject forbidden extension (.exe)", () => {
    const email = `qa_ext_${Date.now()}@example.com`;
    const password = "password123";

    cy.visit("/register");
    cy.get("[data-cy='register-email']").type(email);
    cy.get("[data-cy='register-password']").type(password);
    cy.get("[data-cy='register-submit']").click();

    cy.url().should("include", "/upload");
    cy.get("[data-cy='upload-input']").selectFile(
      {
        contents: Cypress.Buffer.from("malicious"),
        fileName: "evil.exe",
        mimeType: "application/octet-stream",
      },
      { force: true }
    );
    cy.get("[data-cy='upload-submit']").click();

    cy.get("[data-cy='upload-message']").should("contain", "Forbidden file type");
  });
});
