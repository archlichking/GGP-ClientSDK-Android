1. Import GGPClient-Automation and OFQAAPI into your eclipse
2. Improt GreeSdk, PullToRefresh, signpost-commonshttp4 and
   signpost-core from Android GGP Client SDK build
3. Add GreeSdk and OFQAAPI as the android library of
   GGPClient-Automation
4. Modify *res/xml/gree_platform_configuraton.xml* to set the server
   and application config as we do at showcase app
5. Install GGPClient-Automation into your device
6. The first time you setup the automation app, you have to login
   manually, then if you don't change the server setting, next time you
setup a new version of automation app, login is not need again.
7. Input the test suite id then you can load test case from that suite
   of TCM
8. Also if you fill a valid test run id, the result of the test case will submit to that test run of TCM
