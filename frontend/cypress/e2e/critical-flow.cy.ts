describe("Critical flow: register -> upload -> download", () => {
  it("should register, upload a file, and download it", () => {
    const email = `qa_${Date.now()}@example.com`;
    const password = "password123";

    cy.visit("/register");
    cy.get("[data-cy='register-email']").type(email);
    cy.get("[data-cy='register-password']").type(password);
    cy.get("[data-cy='register-submit']").click();

    cy.url().should("include", "/upload");
    cy.get("[data-cy='upload-input']").selectFile("cypress/fixtures/sample.txt", { force: true });
    cy.get("[data-cy='upload-submit']").click();

    cy.get("[data-cy='share-link']")
      .should("be.visible")
      .invoke("attr", "href")
      .then((href) => {
        expect(href).to.contain("/download/");
        const downloadPath = new URL(href as string).pathname;

        cy.intercept("GET", "/api/files/download/*").as("download");
        cy.visit(downloadPath);
        cy.get("[data-cy='download-submit']").click();
        cy.wait("@download").its("response.statusCode").should("eq", 200);
      });
  });
});
