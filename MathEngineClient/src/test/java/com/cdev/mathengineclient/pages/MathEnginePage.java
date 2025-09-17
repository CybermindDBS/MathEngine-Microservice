package com.cdev.mathengineclient.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class MathEnginePage {
    WebDriver webDriver;

    private By calcBtn = By.xpath("//button[text()='Calculate']");
    private By inputBox = By.id("input");
    private By outputBox = By.xpath("//textarea[@placeholder='Result.']");

    public MathEnginePage(WebDriver webDriver) {
        this.webDriver = webDriver;
        PageFactory.initElements(webDriver, this);
    }

    public void calculate(String input) {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(inputBox));

        webDriver.findElement(inputBox).sendKeys(input);
        webDriver.findElement(calcBtn).click();
    }

    public String getResult() {
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(outputBox));

        return webDriver.findElement(outputBox).getText();
    }
}
