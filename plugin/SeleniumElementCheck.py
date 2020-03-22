# -----------------------------------------------------------
# Checks website elements by CSS selector using Selenium
#
# (C) 2020 Jens Dibbern
# Released under public domain
# -----------------------------------------------------------
import sys
import xml.etree.ElementTree as ET
from selenium import webdriver
from selenium.common.exceptions import NoSuchElementException, SessionNotCreatedException
from selenium.webdriver.chrome.options import Options

# prepare Chrome options for headless mode and avoid issues running in background
options = Options()
options.add_argument('--headless')
options.add_argument('--disable-gpu')
options.add_argument('--no-sandbox')
options.add_argument('--disable-dev-shm-usage')
options.add_argument('--disable-features=NetworkService')
options.add_argument('--disable-features=VizDisplayCompositor')

# setup webdriver (executable for chromedriver must be available in path
try:
    driver = webdriver.Chrome(options=options)
except SessionNotCreatedException:
    print('UNKNOWN: could not init webdriver')
    sys.exit(3)

# check command line arguments
if len(sys.argv) != 2:
    print('UNKNOWN: command uses just one argument for config file')
    sys.exit(3)

tree = ET.parse(sys.argv[1])
root = tree.getroot()

warn_count = 0
error_count = 0
messages = ""

# iterate through all the check subtrees in XML document
for check in root.iter('check'):

    # open website
    url = check.attrib['url']
    driver.get(url)

    # iterate through the elements to check
    for element in check:
        selector = element.attrib['selector']
        level = element.attrib['level']
        try:
            # check for element existence
            elem = driver.find_element_by_css_selector(selector)
            if elem.tag_name == 'img':
                # check if image is loaded
                assert isinstance(elem, object)
                img_loaded = driver.execute_script(
                    'return arguments[0].complete && typeof arguments[0].naturalWidth != "undefined" && arguments[0].naturalWidth > 0',
                    elem)
                if not img_loaded:
                    messages += 'image not loaded: %s - %s\n' % (url, selector)
                    if level == 'warn':
                        warn_count += 1
                    else:
                        error_count += 1
        except NoSuchElementException:
            messages += 'failed: %s - %s\n' % (url, selector)
            if level == 'warn':
                warn_count += 1
            else:
                error_count += 1

driver.quit()

if error_count > 0:
    print('CRITICAL: There were errors')
    print(messages)
    sys.exit(2)

if warn_count > 0:
    print('WARN: There were warnings')
    print(messages)
    sys.exit(1)

print('OK: Check successful')
sys.exit(0)
