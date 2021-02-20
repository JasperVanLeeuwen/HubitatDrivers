import groovyx.net.http.RESTClient
import org.apache.commons.lang.NotImplementedException
import org.apache.logging.log4j.LogManager
import org.codehaus.groovy.control.CompilerConfiguration

/*
represents Hubitat hub
 */
class HubitatHubEmulator {

    /*
    Emulation functionality
     */
    def typeToImplementationMap = [:] //[typeName : implementationPath]

    def addTypeToImplementationMap(String typeName, String implementationPath) {
        typeToImplementationMap[typeName] = implementationPath
    }


    /*
    Hubitat functionality
     */

    def devices = [:] //devices [deviceNetworkId: device_object]

    def addChildDevice(String namespace, String typeName, String deviceNetworkId, Map properties = [:]){
        if (!typeToImplementationMap.containsKey(typeName)) {
            throw new RuntimeException("no implementation for ${typeName} registered")
        }
        def binding = new Binding()
        properties.each {binding.setVariable(it.getKey(), it.getValue())}
        binding.setVariable("hub", this)

        def config = new CompilerConfiguration()
        config.scriptBaseClass = 'HubitatDeviceEmulator'
        def shell = new GroovyShell(this.class.classLoader, binding, config)
        def driver = shell.parse(new File(typeToImplementationMap[typeName]))
        devices[deviceNetworkId] = driver
        return driver
    }
}

/*
Base class for Script, emulating Hubitat hub
 */
abstract class HubitatDeviceEmulator extends Script {
    /*
    Emulation functionality
     */


    /*
    Hubitat functionality
    */
    def parent = null //parent device
    def childDevices = [:] //child devices [deviceNetworkId: device_object]
    //deviceCreators [typeName:closure(String typeName, String deviceNetworkId, Map properties = [:])]
    // closure is used to create devices
    def deviceCreators = [:]
    def metadata =  null
    def state = [:]
    String Label = ""
    def log = LogManager.getLogger()


    def httpPost( prms, closure) {
        def doCall = {resp, data->
            def test = resp.data
            closure(['data':data])}
        new RESTClient().post(prms, doCall)
    }

    def httpGet(prms, closure) {
        def doCall = {resp, data->
            def test = resp.data
            closure(['data':data])};
        new RESTClient().get(prms, doCall)
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
    def getChildDevice(String deviceNetworkId) {
        return childDevices?[deviceNetworkId]
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
    def getChildDevices() {
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
    def addChildDevice(String namespace, String typeName, String deviceNetworkId, Map properties = [:]){
        Map newProperties = properties.clone()
        newProperties.put("parent", this)
        def device = hub.addChildDevice(namespace, typeName, deviceNetworkId, properties)
        childDevices[deviceNetworkId] = device
        return device
    }
}
