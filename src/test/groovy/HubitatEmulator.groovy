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
    def typeToImplementationMap = [:] //[typeName : [implementationPath, [attributes in device]]]

    def addTypeToImplementationMap(String typeName, String implementationPath, List attributes=[]) {
        typeToImplementationMap[typeName] = [implementationPath, attributes]
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
        def attributes = typeToImplementationMap[typeName][1]
        attributes.each { it -> binding.setVariable(it, null)}
        binding.setVariable("hub", this)
        binding.setVariable("deviceNetworkId", deviceNetworkId)
        binding.setVariable("state", [:])
        DeviceState device = new DeviceState()
        device.setCurrentValue("temperature",null)
        device.setCurrentValue("thermostatMode",null)
        device.setCurrentValue("thermostatSetpoint",null)
        binding.setVariable("device", device)


        def config = new CompilerConfiguration()
        config.scriptBaseClass = 'HubitatDeviceEmulator'
        def shell = new GroovyShell(this.class.classLoader, binding, config)
        def driver = shell.parse(new File(typeToImplementationMap[typeName][0]))
        devices[deviceNetworkId] = driver
        return driver
    }
}

/*
Manages the current state of a device
 */
class DeviceState {

    def currentState = [:]

    def currentValue(name) {
        return currentState[name]
    }

    def setCurrentValue(name, value) {
        currentState[name] = value
    }
}

/*
Base class for Script, emulating Hubitat hub
 */
abstract class HubitatDeviceEmulator extends Script {
    /*
    Emulation functionality
     */
    //def hub // Hub, defined in binding

    /*
    Hubitat functionality
    */
    /*
    variables from binding:
    def parent // parent device
    def deviceNetworkId
    */
    def childDevices = [:] //child devices [deviceNetworkId: device_object]
    //deviceCreators [typeName:closure(String typeName, String deviceNetworkId, Map properties = [:])]
    // closure is used to create devices
    def deviceCreators = [:]
    def state = [:]
    def log = LogManager.getLogger()


    static def  httpPost( prms, closure) {
        def doCall = {resp, data->
            closure(['data':data])}
        new RESTClient().post(prms, doCall)
    }

    static def httpGet(prms, closure) {
        def doCall = {resp, data->
            closure(['data':data])}
        new RESTClient().get(prms, doCall)
    }

    def sendEvent(Map properties) {
        String name = properties["name"]
        def value = properties["value"]
        device.setCurrentValue(name, value)
    }

    /*
    runIn
Signature
void runIn(Long delayInSeconds, String handlerMethod, Map options = null)
Parameters
delayInSeconds - How long to wait in seconds until the handler should be called, don't expect that it will be called in exactly that time.
handlerMethod - The name of a handler method in your driver or app. The method name should not contain parentheses.
options - Optional values to control the scheduling of this method
    overwrite - defaults to true which cancels the previous scheduled running of the handler method and schedules new, if set to false this will create a duplicate schedule.
    data - optional data to be passed to the handler method.
misfire - If set to "ignore" then the scheduler will simply try to fire it as soon as it can. NOTE: if a scheduler uses this instruction, and it has missed several of its scheduled firings, then several rapid firings may occur as the scheduler attempts to catch back up to where it would have been.
Example
    runIn(50, 'myMethod', [data: ["myKey":"myValue"]])
     */
    static void runIn(Long delayInSeconds, String handlerMethod, Map options = null) {
        //we actually do not wait. perhaps make this configurable
        "$handlerMethod"()
    }

    static void unschedule() {

    }

    void runEvery1Minute(String handlerMethod, Map options = null) {
        //we actually do not wait. perhaps make this configurable
        "$handlerMethod"()
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
        def device = hub.addChildDevice(namespace, typeName, deviceNetworkId, newProperties)
        childDevices[deviceNetworkId] = device
        return device
    }
}
