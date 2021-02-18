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

    /*
    //https://docs.hubitat.com/index.php?title=Driver_Object#getChildDevice
    getChildDevice
    Gets a specific child device with the device network id specified.
    Signature
    ChildDeviceWrapper getChildDevice(String deviceNetworkId)
    Parameters
    deviceNetworkId - The unique identifier for the device
    Returns
    ChildDeviceWrapper
     */
    def getChildDevice = { String deviceNetworkId ->
        return childDevices[deviceNetworkId]
    }

    /*
    https://docs.hubitat.com/index.php?title=Driver_Object#getChildDevices
    getChildDevices
    Gets a list of all child devices for this device.
    Signature
    List<ChildDeviceWrapper> getChildDevices()
    Parameters
    None
    Returns
    List<ChildDeviceWrapper>
     */
    def getChildDevices =  { ->
        return childDevices.values()
    }

    /*
    https://docs.hubitat.com/index.php?title=Driver_Object#addChildDevice
    addChildDevice
    Creates a new child device and returns that device from the method call.
    Signature
    ChildDeviceWrapper addChildDevice(String namespace, String typeName, String deviceNetworkId, Map properties = [:])
    ChildDeviceWrapper addChildDevice(String typeName, String deviceNetworkId, Map properties = [:])
    Parameters
    namespace - The namespace of the child driver to add as a child device (optional, if not specified it will default the the namespace of the parent)
    typeName - The name of the child driver to add as a child device
    deviceNetworkId - unique identifier for this device
    properties - optional parameters for this child device. Possible values listed below
    Properties
    boolean isComponent - true or false, if true, device will still show up in device list but will not be able to be deleted or edited in the UI. If false, device can be modified/deleted on the UI.
    String name - name of child device, if not specified, driver name is used.
    String label - label of child device, if not specified it is left blank.
    Returns
    ChildDeviceWrapper
     */
    def addChildDevice = {String namespace, String typeName, String deviceNetworkId, Map properties = [:] ->
        //TODO implement
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
        binding.setVariable('childDevices', [:])
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