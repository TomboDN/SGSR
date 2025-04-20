package org.example;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v131.page.Page;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DriverFactory {

    WebDriver driver;
    static final String SCREENSHOTS_DIR = "screenshots/";
    private ExecutorService screenshotExecutor;
    private static final long SCREENSHOT_INTERVAL_MS = 10;
    private volatile boolean isRecording = true;
    private DevTools devTools;

    public DriverFactory() throws Exception {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/webdriver/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("window-size=1920x1080");
        options.addArguments("--user-data-dir=C:\\Users\\Admin\\Desktop\\profiles");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        devTools = ((ChromeDriver) driver).getDevTools();
        devTools.createSession();

        AtomicInteger frameNumber = new AtomicInteger(1);
        devTools.addListener(Page.screencastFrame(), frame -> {
            String base64Image = frame.getData();
            byte[] decodedImage = Base64.getDecoder().decode(base64Image);

            String fileName = "frames/" + "frame_" + frameNumber.getAndIncrement() + ".png";
            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                fos.write(decodedImage);
                System.out.println("Сохранен кадр: " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }

            devTools.send(Page.screencastFrameAck(frame.getSessionId()));
        });


        devTools.send(Page.startScreencast(
                Optional.of(Page.StartScreencastFormat.PNG),
                Optional.of(80),
                Optional.of(1280),
                Optional.of(720),
                Optional.of(1)
        ));

        Files.createDirectories(Paths.get(SCREENSHOTS_DIR));
    }

    public void startRecording() {
        screenshotExecutor = Executors.newSingleThreadExecutor();
        AtomicInteger count = new AtomicInteger(1);
        screenshotExecutor.execute(() -> {
            while (isRecording) {
                try {
                    takeScreenshot(count);
                    count.getAndIncrement();
                    Thread.sleep(SCREENSHOT_INTERVAL_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stopRecording() {
        isRecording = false;
        if (screenshotExecutor != null) {
            screenshotExecutor.shutdownNow();
        }
    }

    public void executeTest(Runnable testLogic) {
        try {
            startRecording();
            System.out.println("Начало: " + System.nanoTime());
            testLogic.run();
        } finally {
            stopRecording();
            devTools.send(Page.stopScreencast());
            System.out.println("Конец: " + System.nanoTime());
            driver.quit();
        }
    }

    private void takeScreenshot(AtomicInteger count) throws IOException {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File destination = new File(SCREENSHOTS_DIR + "screenshot_" + count + ".png");
        Files.copy(screenshot.toPath(), destination.toPath());
    }

    public WebElement awaitElement(By by, int timeout) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        return wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }
}
