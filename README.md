## How to build the automation app
1. Import GGPClient-Automation and OFQAAPI into your eclipse
2. Improt GreeSdk, PullToRefresh, signpost-commonshttp4 and
   signpost-core from Android GGP Client SDK build
3. Add GreeSdk and OFQAAPI as the android library of
   GGPClient-Automation
4. You can Modify **res/xml/gree_platform_configuraton.xml** to set the server
   and application config but the default config should be working.

## How to use automation app
1. Login:
   * The first time you setup the automation app, you have to login manually before you run the test case
   * If next time you setup a new version of automation app without change the server setting, login is not need again
2. Load test case:
   * Input the test suite id of TCM into the TCM Suite ID blank
   * Then click Load/ReSync button you can load test case from that suite of TCM
   * Default test suite is can be set in **res/layout/main.xml**, search android:id="@+id/text_suite_id" in this xml
3. Run test case:
   * Select the test case you want, also there is a "select all" button
   * Click "Start Selected" button to run these test cases
   * Test result will display in the list next to the checkbox, result include U untest, P passed, F failed and R retested
4. Result submit to TCM:
   * Input the test run id of TCM into TCM Run ID blank
   * Then the result of your run will automatic submit to that test run of TCM
   * Default test suite is can be set in **res/layout/main.xml**, search android:id="@+id/text_run_id" in this xml
   * If you want to temporary disable the result submit to make your debug more faster, then comment out the line 
   tcm.setTestCasesResult(run_text.getText().toString(), adapter.getSelectedCases()); in MainActivity.java

## Configuration
### Server: production sandbox
* App Id: 11787
* App Key: 97f61d7b8f43
* App Secret: 38a4325e76d9b66fb5cd2bda5a2eaa59
* Test User: auto3@163.com/123456
* TCMS Suites Id: 178