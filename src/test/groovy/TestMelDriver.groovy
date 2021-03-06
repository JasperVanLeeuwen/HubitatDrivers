import org.codehaus.groovy.control.CompilerConfiguration
import groovy.json.JsonOutput
import static org.mockito.Mockito.*

class TestMelDriver extends GroovyTestCase {

    private Object auth_driver = null

    def originalState = null

    void setUp() {
        def meldevice = getMyMelDevice()
        originalState = meldevice.childDevices[0].retrieveDeviceState()
    }

    void tearDown() {
        def meldevice = getMyMelDevice()
        meldevice.childDevices[0].sendCommand(originalState)
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
        hub.addTypeToImplementationMap(melType, "src/main/groovy/MelDriver.groovy",[])
        def driver = hub.addChildDevice("",melType,"0",getMelDriverConfig())

        String authCode = driver.obtainAuthToken()
        log.info "authCode: $authCode"
        assertNotNull authCode
    }

    def get_authDriver() {
        if (auth_driver==null) {
            HubitatHubEmulator hub = new HubitatHubEmulator()

            def melType = "MelDriver Parent Driver for Melcloud"
            hub.addTypeToImplementationMap(melType, "src/main/groovy/MelDriver.groovy",[])

            def melchildType = "MelDriver Child Driver for Melcloud"
            hub.addTypeToImplementationMap(melchildType,
                    "src/main/groovy/MelChildDriver.groovy",
                    ["DeviceID", "DeviceName", "BuildingID"])

            def presetType= "PresetButton for Melcloud"
            hub.addTypeToImplementationMap(presetType,
                    "src/main/groovy/PresetButton.groovy",
                    ["DeviceID"])

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

    def melDevice = null
    Object getMyMelDevice() {
        if (melDevice==null) {
            melDevice = get_authDriver()
            melDevice.retrieveAndUpdateDevices()
            def devices = melDevice.getChildDevices()
        }
        return melDevice
    }

    void testTemperatureUpdate(){
        def meldevice = getMyMelDevice()
        meldevice.childDevices[0].retrieveAndUpdate()
        assert meldevice.childDevices[0].device.currentState['temperature'] > 0
    }

    void testOff(){
        def meldevice = getMyMelDevice()
        meldevice.childDevices[0].off()
        assert meldevice.childDevices[0].device.currentState.thermostatMode == "off"
    }

    void testCool(){
        def meldevice = getMyMelDevice()
        meldevice.childDevices[0].cool()
        assert meldevice.childDevices[0].device.currentState.thermostatMode == "cool"
    }

    void testHeat(){
        def meldevice = getMyMelDevice()
        meldevice.childDevices[0].heat()
        assert meldevice.childDevices[0].device.currentState.thermostatMode == "heat"
    }

    void testAuto(){
        def meldevice = getMyMelDevice()
        meldevice.childDevices[0].auto()
        assert meldevice.childDevices[0].device.currentState.thermostatMode == "auto"
    }

    void testSetThermostatMode() {
        def meldevice = getMyMelDevice()
        meldevice.childDevices[0].setThermostatMode("heat")
        assert meldevice.childDevices[0].device.currentState.thermostatMode == "heat"
    }

    void testSetHeatingSetpoint() {
        def meldevice = getMyMelDevice()
        meldevice.childDevices[0].setHeatingSetpoint(25)
        assert meldevice.childDevices[0].device.currentState.heatingSetpoint == 25
    }

    void testSetCoolingSetpoint() {
        def meldevice = getMyMelDevice()
        meldevice.childDevices[0].setCoolingSetpoint(15)
        assert meldevice.childDevices[0].device.currentState.coolingSetpoint == 15
    }

    void testGetPreset() {
        def meldevice = getMyMelDevice()
        def DeviceID = meldevice.childDevices[0].device.currentState.DeviceID
        def presets = meldevice.getPresets(DeviceID)
        assert presets.size()>0
    }

    void testSetPreset() {
        def meldevice = getMyMelDevice()
        def childDevice = meldevice.childDevices[0]
        def presets = childDevice.setPreset(1)
    }

    void testJsonOutput() {
        log.warning JsonOutput.toJson([sd: "sd"])
    }

    void testPreset() {
        HubitatDeviceEmulator driver = get_authDriver()
        driver.retrieveAndUpdateDevices()
        def devices = driver.getChildDevices()
        //todo : change this for others
        def presetButton = devices[3]
        presetButton.push()
        assert devices.size()>0
    }

}