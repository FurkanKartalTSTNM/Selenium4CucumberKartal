Feature: Google search (demo)

  @smoke
  Scenario: Search for Cucumber
    Given I open "https://www.google.com"
    When I search for "cucumber bdd"
    Then the page title should contain "cucumber"
