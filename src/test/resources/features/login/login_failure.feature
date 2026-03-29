@login @regression
Feature: Failed Login

  Background:
    Given the user is on the login page

  @regression
  Scenario: User logs in with invalid credentials
    When the user logs in with invalid credentials from test data "invalid_users.json"
    Then an error message "invalid" should be displayed
    And the user should remain on the login page

  @regression
  Scenario: User logs in with wrong password
    When the user logs in with username "admin" and password "wrongpassword"
    Then an error message "incorrect" should be displayed
    And the user should remain on the login page

  @regression
  Scenario: User logs in with empty credentials
    When the user logs in with username "" and password ""
    Then an error message "required" should be displayed
    And the user should remain on the login page
