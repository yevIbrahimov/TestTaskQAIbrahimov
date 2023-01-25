package com.example.testtaskqaibrahimov;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class MainPageTest {
    WebDriver driver;

    @BeforeTest
    public void setupDriver(){
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }

    @Test
    public void TestCase1(){
        //Navigate to web page
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("http://demowebshop.tricentis.com/");

        //Wait for page load and navigate to desktop page
        waitForPage();

        WebElement computersButton = driver.findElement(By.xpath("//a[contains(text(),'Computers')]"));
        computersButton.click();

        WebElement desktopsButton = driver.findElements(By.className("item-box")).get(0);
        desktopsButton.click();

        //Set Display to "4" per page and check only 4 items displayed after that
        WebElement dropDownCount = driver.findElement(By.id("products-pagesize"));
        dropDownCount.click();
        WebElement itemCountOption = driver.findElement(By.xpath("//option[contains(text(), '4')]"));
        itemCountOption.click();

        List<WebElement> itemsList = driver.findElements(By.className("item-box"));
        Assert.assertEquals(itemsList.size(), 4, "Items count ont the page is wrong");

        //Sort "Price: High to Low", and click add to cart the most expensive item
        WebElement dropDownSort = driver.findElement(By.id("products-orderby"));
        WebElement sortOption = driver.findElement(By.xpath("//option[contains(text(), 'Price: High to Low')]"));

        dropDownSort.click();
        sortOption.click();

        itemsList = driver.findElements(By.className("product-item"));
        itemsList.get(0).click(); //add to cart button does not add to cart, but navigates to item page, maybe it is a kind of bug

        String expectedItemTitle = driver.findElement(By.cssSelector("h1[itemprop = 'name']")).getText();

        WebElement addToCardButton = driver.findElement(By.className("add-to-cart-button"));
        addToCardButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//p[contains(text(), 'The product has been added to your')]")));

        //Navigate to shopping cart and check the item is in the shopping cart.
        WebElement shoppingCardButton = driver.findElement(By.id("topcartlink"));
        shoppingCardButton.click();

        List<WebElement> shoppingCartItemList = driver.findElements(By.className("cart-item-row"));
        Assert.assertEquals(shoppingCartItemList.size(), 1,
                "Added items count to the shopping list is wrong");

        String actualItemTitle = driver.findElement(By.className("product-name")).getText();
        Assert.assertEquals(actualItemTitle, expectedItemTitle, "Added item title is wrong");
    }

    @Test
    public void TestCase2(){
        //Navigate to web page
        float totalExpectedPrice = 0;
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("http://demowebshop.tricentis.com/build-your-own-expensive-computer-2");
        waitForPage();

        //Set all necessary params and count price
        WebElement fastProcessorLabel = driver.findElement(By.xpath("//label[contains(text(), 'Fast')]"));
        WebElement fastProcessorCheckbox = driver.findElement(By.id(fastProcessorLabel.getAttribute("for")));
        fastProcessorCheckbox.click();
        totalExpectedPrice += getPrice(fastProcessorLabel);

        WebElement ramLabel = driver.findElement(By.xpath("//label[contains(text(), '8GB')]"));
        WebElement ramCheckbox = driver.findElement(By.id(ramLabel.getAttribute("for")));
        ramCheckbox.click();
        totalExpectedPrice += getPrice(ramLabel);

        List<WebElement> softwareCheckbox = driver.findElements(By.cssSelector("input[type = 'checkbox']"));
        for (WebElement checkbox : softwareCheckbox) {
            String id = checkbox.getAttribute("id");
            WebElement softwareLabel = driver.findElement(By.cssSelector("label[for='" + id + "']"));
            totalExpectedPrice += getPrice(softwareLabel);
            softwareLabel.click();
        }

        String basePrice = driver.findElement(By.cssSelector("span[itemprop='price']")).getText();
        totalExpectedPrice += Float.parseFloat(basePrice);

        //Click "Add to cart" and check the shopping cart has +1 item.
        int cartCountBefore = Integer.parseInt(driver.findElement(
                By.className("cart-qty")).getText().replaceAll("[^0-9]", ""));

        WebElement addToCartButton = driver.findElement(By.id("add-to-cart-button-74"));
        addToCartButton.click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//p[contains(text(), 'The product has been added to your')]")));

        int cartCountAfter = Integer.parseInt(driver.findElement(
                By.className("cart-qty")).getText().replaceAll("[^0-9]", ""));

        Assert.assertEquals(cartCountAfter, cartCountBefore + 1, "Shopping cart counter is wrong");

        //Open the Shopping cart and check the item is there and the price is correct
        String expectedItemTitle = driver.findElement(By.cssSelector("h1[itemprop = 'name']")).getText();

        WebElement shoppingCardButton = driver.findElement(By.id("topcartlink"));
        shoppingCardButton.click();

        List<WebElement> shoppingCartItemList = driver.findElements(By.className("cart-item-row"));
        Assert.assertEquals(shoppingCartItemList.size(), 1, "Items count in shopping cart is wrong");

        String actualItemTitle = driver.findElement(By.className("product-name")).getText();
        Assert.assertEquals(actualItemTitle, expectedItemTitle, "Added item title is wrong");

        float actualPrice = Float.parseFloat(driver.findElement(By.className("product-unit-price")).getText());
        Assert.assertEquals(actualPrice, totalExpectedPrice, "Item total price is wrong");

        //Remove the item from the shopping cart
        WebElement removeCheckbox = driver.findElement(By.cssSelector("input[name = 'removefromcart']"));
        removeCheckbox.click();

        WebElement updateCartList = driver.findElement(By.cssSelector("input[name = 'updatecart']"));
        updateCartList.click();

        shoppingCartItemList = driver.findElements(By.className("cart-item-row"));
        Assert.assertEquals(shoppingCartItemList.size(), 0, "Items count in shopping cart is wrong");
    }

    @AfterTest
    public void closeWindow(){
        driver.quit();
    }

    private float getPrice(WebElement element){
        String text = element.getText();
        return Float.parseFloat(text.substring(text.indexOf('+')+1, text.indexOf(']')));
    }

    private void waitForPage(){
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
    }
}