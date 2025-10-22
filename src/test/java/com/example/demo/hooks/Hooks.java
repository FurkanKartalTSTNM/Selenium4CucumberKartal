package com.example.demo.hooks;

import com.example.demo.support.DriverFactory;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.openqa.selenium.WebDriver;

public class Hooks {

    @Before
    public void beforeScenario() {
        WebDriver driver = DriverFactory.get();
        driver.manage().window().maximize();
    }

    @After
    public void afterScenario() {
        DriverFactory.quit();
    }
}
