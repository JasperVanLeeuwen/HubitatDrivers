import groovy.json.JsonOutput
import groovyx.net.http.RESTClient

class TestMelDriver extends GroovyTestCase {

    private Object auth_driver = null

    def doPost = {prms, closure ->
        def doCall = {resp, data->
            def test = resp.data
            closure(['data':data])};
        new RESTClient().post(prms, doCall)
        //new HTTPBuilder().post()
    }

    def doGet = {prms, closure ->
        def doCall = {resp, data->
            def test = resp.data
            closure(['data':data])};
        new RESTClient().get(prms, doCall)
        //new HTTPBuilder().post()
    }

    void testProperties() {
        InputStream  prop_stream = TestMelDriver.class.getClassLoader().getResourceAsStream("mymelcloud.properties")
        if (prop_stream==null) {
            log.warning("please create a file called 'mymelcloud.properties' in src\\test\\resources based on 'melcloud.properties'")
        }
        assertNotNull prop_stream

        Properties props = new Properties()
        props.load(prop_stream)

        assertNotNull props.getProperty("BaseURL")
        assertNotNull props.getProperty("UserName")
        assertNotNull props.getProperty("Password")
    }

    Object getMelDriver() {
        InputStream  prop_stream = TestMelDriver.class.getClassLoader().getResourceAsStream("mymelcloud.properties")
        Properties props = new Properties()
        props.load(prop_stream)

        String BaseURL = props.getProperty("BaseURL")
        String UserName = props.getProperty("UserName")
        String Password = props.getProperty("Password")

        def binding = new Binding()
        def shell = new GroovyShell(binding)
        binding.setVariable('metadata', { })
        binding.setVariable('state', [:])
        binding.setVariable('BaseURL', BaseURL)
        binding.setVariable('UserName', UserName)
        binding.setVariable('Password', Password)
        //binding.setVariable('log',new doLog(log))
        binding.setVariable('log',log)

        binding.setVariable('httpPost', doPost)
        binding.setVariable('httpGet', doGet)

        binding.setVariable('authCode', "")

        def driver = shell.parse(new File("src/main/groovy/MelDriver.groovy"))
        assertNotNull(driver)
        return driver

    }
    void testObtainAuthToken() {
        def driver = getMelDriver()
        String authCode = driver.obtainAuthToken()
        log.info "authCode: $authCode"
        assert !authCode.empty
    }

    def get_authDriver() {
        if (auth_driver==null) {
            auth_driver = getMelDriver()
            auth_driver.refresh()

        }
        return auth_driver
    }

    void testGetListDevices() {
        def driver = get_authDriver()
        def devices = driver.getListDevices()
        log.info("${devices}")
        assertNotNull devices
    }


    void testJsonOutput() {
        log.warning JsonOutput.toJson([sd: "sd"])
    }
}