describe("Auth flow: login", () => {
  it("should login successfully with a valid existing account", () => {
    const email = `qa_login_${Date.now()}@example.com`;
    const password = "password123";

    // Create account first
    cy.visit("/register");
    cy.get("[data-cy='register-email']").type(email);
    cy.get("[data-cy='register-password']").type(password);
    cy.get("[data-cy='register-submit']").click();
    cy.url().should("include", "/upload");

    // Simulate fresh session and login path
    cy.clearLocalStorage();
    cy.visit("/login");
    cy.get("[data-cy='login-email']").type(email);
    cy.get("[data-cy='login-password']").type(password);
    cy.get("[data-cy='login-submit']").click();

    cy.url().should("include", "/upload");
  });

  it("should show an error on invalid credentials", () => {
    cy.visit("/login");
    cy.get("[data-cy='login-email']").type("unknown@example.com");
    cy.get("[data-cy='login-password']").type("wrong-password");
    cy.get("[data-cy='login-submit']").click();

    cy.get("[data-cy='login-error']").should("be.visible");
  });
});
