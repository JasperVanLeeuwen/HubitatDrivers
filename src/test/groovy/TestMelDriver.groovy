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

    Map  getMelDriverConfig() {
        InputStream  prop_stream = TestMelDriver.class.getClassLoader().getResourceAsStream("mymelcloud.properties")
        Properties props = new Properties()
        props.load(prop_stream)
        def config = [:]
        props.each {config[it.key] = it.value}
        return config
    }

    void testObtainAuthToken() {
        HubitatHubEmulator hub = new HubitatHubEmulator()
        def melType = "\"MelDriver Parent Driver for Melcloud\""
        hub.addTypeToImplementationMap(melType, "src/main/groovy/MelDriver.groovy")
        def driver = hub.addChildDevice("",melType,"0",getMelDriverConfig())

        String authCode = driver.obtainAuthToken()
        log.info "authCode: $authCode"
        assertNotNull authCode
    }

    def get_authDriver() {
        if (auth_driver==null) {
            HubitatHubEmulator hub = new HubitatHubEmulator()

            def melType = "MelDriver Parent Driver for Melcloud"
            hub.addTypeToImplementationMap(melType, "src/main/groovy/MelDriver.groovy")

            def melchildType = "MelDriver Child Driver for Melcloud"
            hub.addTypeToImplementationMap(melchildType, "src/main/groovy/MelChildDriver.groovy")

            auth_driver = hub.addChildDevice("",melType,"0",getMelDriverConfig())
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

    void testRetrieveAndUpdateDevices() {
        HubitatDeviceEmulator driver = get_authDriver()
        driver.retrieveAndUpdateDevices()
        def devices = driver.getChildDevices()
        assert devices.size()>0
    }

    void testJsonOutput() {
        log.warning JsonOutput.toJson([sd: "sd"])
    }
}