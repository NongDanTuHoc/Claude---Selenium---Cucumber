@login @smoke
Feature: Successful Login

  Background:
    Given the user is on the login page

  @smoke
  Scenario: User logs in with valid credentials from data file
    When the user logs in with valid credentials from test data "valid_users.json"
    Then the user should be logged in successfully
    And the user should be redirected to the dashboard
    And the login form should be visible

  @smoke
  Scenario: User logs in with explicit username and password
    When the user logs in with username "admin" and password "secret123"
    Then the user should be logged in successfully
    And the user should be redirected to the dashboard

  Scenario: Login page elements are present
    Then the login form should be visible
    And the username field should be present
