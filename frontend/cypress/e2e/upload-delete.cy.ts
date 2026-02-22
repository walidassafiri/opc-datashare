describe("Critical flow: register -> upload -> delete", () => {
  it("should upload a file then delete it from history", () => {
    const email = `qa_delete_${Date.now()}@example.com`;
    const password = "password123";

    cy.visit("/register");
    cy.get("[data-cy='register-email']").type(email);
    cy.get("[data-cy='register-password']").type(password);
    cy.get("[data-cy='register-submit']").click();

    cy.url().should("include", "/upload");
    cy.get("[data-cy='upload-input']").selectFile("cypress/fixtures/sample.txt", { force: true });
    cy.get("[data-cy='upload-submit']").click();
    cy.get("[data-cy='upload-message']").should("contain", "Upload successful");

    cy.on("window:confirm", () => true);
    cy.get("[data-cy='history-delete-btn']").first().click();
    cy.get("[data-cy='upload-message']").should("contain", "File deleted successfully");
  });
});
