package org.example;

import org.openqa.selenium.By;

public class Main {

    public static void main(String[] args) throws Exception {
        DriverFactory recorder = new DriverFactory();

        recorder.executeTest(() -> {
            recorder.driver.get("https://www.ark.org/corp-search/index.php");
            recorder.awaitElement(By.cssSelector("#CorporationName"), 10).sendKeys("Ben");
            recorder.awaitElement(By.cssSelector("button[type=submit]"), 10).click();
        });
    }
}