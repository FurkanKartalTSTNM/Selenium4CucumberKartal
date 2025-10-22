package com.example.demo.support;

import com.testinium.driver.TestiniumSeleniumDriver; // <- senin sınıfın (RemoteWebDriver extends)
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;

public class DriverFactory {
    private static final ThreadLocal<WebDriver> TL = new ThreadLocal<>();

    // Varsayılanlar
    private static final String DEFAULT_BROWSER = "chrome";
    private static final String DEFAULT_HUB =
            // ENV’de nodeUrl varsa onu, yoksa hubURL, yoksa sabit fallback
            firstNonBlank(System.getenv("nodeUrl"),
                    System.getenv("hubURL"),
                    System.getProperty("hubURL"),
                    "http://172.25.1.110:4444/wd/hub");

    public static WebDriver get() {
        WebDriver d = TL.get();
        if (d == null) {
            d = create();
            TL.set(d);
        }
        return d;
    }

    public static void quit() {
        WebDriver d = TL.get();
        if (d != null) {
            try { d.quit(); } finally { TL.remove(); }
        }
    }

    private static WebDriver create() {
        String browser = propOrEnv("browser", DEFAULT_BROWSER).toLowerCase();
        String hubUrl  = propOrEnv("hubURL", DEFAULT_HUB);

        // Testinium tarafına iletmek istediğin key/capabilities
        String key              = propOrEnv("key", null);                 // -Dkey=... / ENV KEY
        String profile          = propOrEnv("profile", null);
        String takeScreenshot   = propOrEnv("takeScreenshot", null);      // YES/NO
        String takeRecording    = propOrEnv("takeScreenRecording", null); // true/false

        MutableCapabilities opts;
        switch (browser) {
            case "firefox" -> {
                FirefoxOptions fx = new FirefoxOptions();
                fx.setAcceptInsecureCerts(true);
                // ekstra argüman istiyorsan:
                // fx.addArguments("--width=1366", "--height=768");
                opts = fx;
            }
            default -> {
                ChromeOptions ch = new ChromeOptions();
                ch.setAcceptInsecureCerts(true);
                ch.addArguments("--disable-gpu");             // CI ortamlarında faydalı
                ch.addArguments("--no-sandbox");              // container’larda yaygın
                // headless istenirse: -Dheadless=true
                if (Boolean.parseBoolean(propOrEnv("headless", "false"))) {
                    ch.addArguments("--headless=new");
                }
                opts = ch;
            }
        }

        // Testinium’a özel capability’ler (adlarını kendi tarafına göre ayarla)
        if (notBlank(key))            opts.setCapability("key", key); // veya "testinium:key"
        if (notBlank(profile))        opts.setCapability("profile", profile);
        if (notBlank(takeScreenshot)) opts.setCapability("takeScreenshot", takeScreenshot);
        if (notBlank(takeRecording))  opts.setCapability("takeScreenRecording", takeRecording);

        try {
            TestiniumSeleniumDriver driver = new TestiniumSeleniumDriver(new URL(hubUrl), opts);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(longPropOrEnv("implicitWaitSec", 5)));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(longPropOrEnv("pageLoadTimeoutSec", 30)));
            // İstersen window maximize’i grid node tarafında yaparsın; burada da ekleyelim:
            try { driver.manage().window().maximize(); } catch (Exception ignored) {}
            return driver;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid hubURL: " + hubUrl, e);
        }
    }

    // -------- helpers --------
    private static String propOrEnv(String key, String def) {
        String v = System.getProperty(key);
        if (notBlank(v)) return v;
        v = System.getenv(key);
        return notBlank(v) ? v : def;
    }

    private static long longPropOrEnv(String key, long def) {
        String v = propOrEnv(key, null);
        if (!notBlank(v)) return def;
        try { return Long.parseLong(v); } catch (NumberFormatException e) { return def; }
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private static String firstNonBlank(String... vals) {
        if (vals == null) return null;
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }
}
