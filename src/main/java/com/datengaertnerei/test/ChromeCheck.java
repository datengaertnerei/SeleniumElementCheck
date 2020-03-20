package com.datengaertnerei.test;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class to perform website checks with chrome browser
 * Can be started from command line main entry class or from an test class (JUnit/TestNG)
 * 
 * It can check the existence of a list of elements and will verify loaded images for one URL. 
 * 
 * @author Jens Dibbern
 *
 */
public class ChromeCheck {

	// constant
	private static final String ERROR_LEVEL = "error";

	private WebDriver internalDriver;
	private NodeList elements;
	private String url;

	/**
	 * ctor
	 * 
	 * @param url	the one URL to check for elements
	 * @param elements	the list of elements to check
	 * @param mobileDevice	optional for Chrome mobile device emulation
	 */
	public ChromeCheck(String url, NodeList elements, String mobileDevice) {
		this.elements = elements;
		this.url = url;

		ChromeOptions chromeOptions = new ChromeOptions();

		// use Chrome mobile device emulation (optional)
		if (null != mobileDevice) {
			Map<String, String> mobileEmulation = new HashMap<>();
			mobileEmulation.put("deviceName", mobileDevice);
			chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
		}

		// chrome options to avoid problems while running in background without display
		chromeOptions.addArguments("--headless");
		chromeOptions.addArguments("--no-sandbox");
		chromeOptions.addArguments("--disable-dev-shm-usage");
		chromeOptions.addArguments("--disable-gpu");
		chromeOptions.addArguments("--disable-features=NetworkService");
		chromeOptions.addArguments("--disable-features=VizDisplayCompositor");

		internalDriver = new ChromeDriver(chromeOptions);
	}

	/**
	 * Check method, open URL and iterate through all the checks of the element list
	 * 
	 * @return	result list with warnings and errors if any occured
	 */
	public CheckResultList<CheckResult> runChecks() {
		CheckResultList<CheckResult> results = new CheckResultList<>();

		internalDriver.get(url);

		for (int i = 0; i < elements.getLength(); i++) {
			Node element = elements.item(i);
			if (element.getNodeName().equals("element")) {
				String selector = element.getAttributes().getNamedItem("selector") == null ? null
						: element.getAttributes().getNamedItem("selector").getTextContent();
				String level = element.getAttributes().getNamedItem("level") == null ? ERROR_LEVEL
						: element.getAttributes().getNamedItem("level").getTextContent();

				try {
					WebElement pageElement = internalDriver.findElement(By.cssSelector(selector));
					if (pageElement.getTagName().equals("img")) {
						Boolean isImageLoaded = (Boolean) ((JavascriptExecutor) internalDriver).executeScript(
								"return arguments[0].complete && typeof arguments[0].naturalWidth != \"undefined\" && arguments[0].naturalWidth > 0",
								pageElement);

						if (!isImageLoaded) {
							results.add(new CheckResult(level.equals(ERROR_LEVEL), selector, "Image not loaded"));
						}
					}
				} catch (NoSuchElementException | IllegalArgumentException e) {
					results.add(new CheckResult(level.equals(ERROR_LEVEL), selector, e.getMessage()));
				}
			}
		}

		internalDriver.quit();
		return results;
	}

}
