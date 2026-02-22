describe("Critical flow: wrong password on download", () => {
  it("should show an error when download password is invalid", () => {
    const email = `qa_pwd_${Date.now()}@example.com`;
    const password = "password123";
    const filePassword = "secret123";

    cy.visit("/register");
    cy.get("[data-cy='register-email']").type(email);
    cy.get("[data-cy='register-password']").type(password);
    cy.get("[data-cy='register-submit']").click();

    cy.url().should("include", "/upload");
    cy.get("[data-cy='upload-input']").selectFile("cypress/fixtures/sample.txt", { force: true });
    cy.get("[data-cy='upload-password']").type(filePassword);
    cy.get("[data-cy='upload-submit']").click();

    cy.get("[data-cy='share-link']")
      .should("be.visible")
      .invoke("attr", "href")
      .then((href) => {
        const downloadPath = new URL(href as string).pathname;

        cy.visit(downloadPath);
        cy.get("[data-cy='download-password']").type("wrong-password");
        cy.get("[data-cy='download-submit']").click();
        cy.get("[data-cy='download-error']").should("contain", "Mot de passe incorrect");
      });
  });
});
