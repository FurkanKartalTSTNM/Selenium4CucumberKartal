package com.example.demo.steps;

import com.example.demo.support.DriverFactory;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

public class GoogleSteps {

    @Given("I open {string}")
    public void i_open(String url) {
        DriverFactory.get().get(url);
        // Cookie banner'ı engellerse ek kontrol ekleyebilirsin
    }

    @When("I search for {string}")
    public void i_search_for(String query) {
        WebDriver d = DriverFactory.get();
        d.findElement(By.name("q")).sendKeys(query + Keys.ENTER);
    }

    @Then("the page title should contain {string}")
    public void the_page_title_should_contain(String text) {
        String title = DriverFactory.get().getTitle().toLowerCase();
        Assertions.assertThat(title).contains(text.toLowerCase());
    }
}
