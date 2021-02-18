import org.codehaus.groovy.control.CompilerConfiguration
import groovy.json.JsonOutput

class TestMelDriver extends GroovyTestCase {

    private Object auth_driver = null



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
        binding.setVariable('BaseURL', BaseURL)
        binding.setVariable('UserName', UserName)
        binding.setVariable('Password', Password)
        binding.setVariable('authCode', "")

        def config = new CompilerConfiguration()
        config.scriptBaseClass = 'HubitatEmulator'
        def shell = new GroovyShell(this.class.classLoader, binding, config)
        def driver = shell.parse(new File("src/main/groovy/MelDriver.groovy"))
        assertNotNull(driver)
        return driver

    }
    void testObtainAuthToken() {
        def driver = getMelDriver()
        String authCode = driver.obtainAuthToken()
        log.info "authCode: $authCode"
        assertNotNull authCode
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