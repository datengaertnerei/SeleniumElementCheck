package com.datengaertnerei.test;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ChromeCheck {

	private static final String ERROR_LEVEL = "error";
	private ChromeDriver internalDriver;
	private NodeList elements;
	private String url;

	public ChromeCheck(String url, NodeList elements, String mobileDevice) {
		this.elements = elements;
		this.url = url;

		ChromeOptions chromeOptions = new ChromeOptions();

		if (null != mobileDevice) {
			Map<String, String> mobileEmulation = new HashMap<>();
			mobileEmulation.put("deviceName", mobileDevice);
			chromeOptions.setExperimentalOption("mobileEmulation", mobileEmulation);
		}
		chromeOptions.addArguments("--headless");
		internalDriver = new ChromeDriver(chromeOptions);
	}

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
					if(pageElement.getTagName().equals("img")) {
						Boolean isImageLoaded = (Boolean) ((JavascriptExecutor)internalDriver).executeScript("return arguments[0].complete && typeof arguments[0].naturalWidth != \"undefined\" && arguments[0].naturalWidth > 0", element);
						if(!isImageLoaded) {
							results.add(new CheckResult(level.equals(ERROR_LEVEL), selector, "Image not loaded"));							
						}
					}
				} catch (NoSuchElementException e) {
					results.add(new CheckResult(level.equals(ERROR_LEVEL), selector, e.getMessage()));
				}
			} 
		}

		internalDriver.quit();
		return results;
	}

}
