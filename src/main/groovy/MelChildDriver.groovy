import groovy.json.JsonOutput

metadata {
    definition(name: "MelDriver Child Driver for Melcloud", namespace: "meldriver", author: "Jasper van Leeuwen") {
        capability "Refresh"
        capability "TemperatureMeasurement"
        //capability "Thermostat"
        capability "ThermostatMode"
//        capability "ThermostatHeatingSetpoint"
//        capability "ThermostatCoolingSetpoint"
        attribute "DeviceID", "string"
        attribute "BuildingID", "string"
        attribute "coolingSetpoint", "number"
        attribute "heatingSetpoint", "number"
        attribute "thermostatSetpoint", "number"

    }
}

def refresh() {
    retrieveAndUpdate()
}

def retrieveDeviceState() {
    def authCode = parent.getTheAuthcode()
    def baseUrl = parent.getTheBaseURL()
    if (authCode==null) {
        log.error("Not authenticated")
        return
    }
    def data = null
    def headers = [
            "Content-Type": "application/json; charset=UTF-8",
            "Accept": "application/json",
            "Referer": baseUrl,
            "X-MitsContextKey": authCode
    ]
    def getParams = [
            uri        : "${baseUrl}/Mitsubishi.Wifi.Client/Device/Get?id=${device.currentValue('DeviceID')}&buildingID=${device.currentValue('BuildingID')}",
            headers    : headers,
            contentType: "application/json; charset=UTF-8",
    ]
    try {

        httpGet(getParams) { resp ->
            data = resp.data

        }
    }
    catch (Exception e) {
        log.error "retrieveDeviceState : Unable to query Mitsubishi Electric MELCloud: ${e}"
    }
    return data
}

def update(data) {
    def temperature = data['RoomTemperature']
    def operationMode = data['OperationMode']
    def power = data['Power']
    //thermostatMode: ENUM ["heat", "cool", "emergency heat", "auto", "off"]
    def thermostatModeMapper = [
            8: "auto",
            1: "heat",
            3: "cool",
    ]
    def thermostatModeTmp = thermostatModeMapper.getOrDefault(operationMode, null)
    if (!power) {
        thermostatModeTmp = "off"
    }
    sendEvent(name:"temperature", value:temperature)
    sendEvent(name:"thermostatMode", value:thermostatModeTmp)
    sendEvent(name:"thermostatSetpoint", value:data['SetTemperature'])
    sendEvent(name:"coolingSetpoint", value:data['DefaultCoolingSetTemperature'])
    sendEvent(name:"heatingSetpoint", value:data['DefaultHeatingSetTemperature'])
}

def retrieveAndUpdate() {
    def data = retrieveDeviceState()
    update(data)
}

def sendCommand(data) {
    def result = null
    def headers = [
            "Content-Type": "application/json; charset=UTF-8",
            Accept: "application/json",
            Referer: parent.getTheBaseURL(),
            Origin: parent.getTheBaseURL(),
            "X-MitsContextKey": parent.getTheAuthcode() ,
            "Sec-Fetch-Mode": "cors"
    ]
    def postParams = [
            uri        : "${parent.getTheBaseURL()}/Mitsubishi.Wifi.Client/Device/SetAta",
            headers    : headers,
            contentType: "application/json; charset=UTF-8",
            body       : JsonOutput.toJson(data)
    ]

    httpPost(postParams)
            { resp ->
                result = resp.data
            }
    return result
}
/*
thermostatmode

attributes

thermostatMode - ENUM ["heat", "cool", "emergency heat", "auto", "off"]
Commands

auto()
cool()
emergencyHeat()
heat()
off()
setThermostatMode(thermostatmode)
thermostatmode required (ENUM) - Thermostat mode to set
 */
def off() {
    def data = retrieveDeviceState()
    data['EffectiveFlags'] = 1
    data['Power'] = false
    data = sendCommand(data)
    update(data)
}

def auto() {
    def data = retrieveDeviceState()
    data['EffectiveFlags'] = 287
    data['Power'] = true
    data['OperationMode'] = 8
    data = sendCommand(data)
    update(data)
}

def cool() {
    def data = retrieveDeviceState()
    data['EffectiveFlags'] = 287
    data['Power'] = true
    data['OperationMode'] = 3
    data['SetTemperature'] = data['DefaultCoolingSetTemperature']
    data = sendCommand(data)
    update(data)
}

def emergencyHeat(){
    log.error "emergencyHeat not supported by MelCloud"
}

def heat() {
    def data = retrieveDeviceState()
    data['EffectiveFlags'] = 287
    data['Power'] = true
    data['OperationMode'] = 1
    data['SetTemperature'] = data['DefaultHeatingSetTemperature']
    data = sendCommand(data)
    update(data)
}
def setThermostatMode(thermostatmode) {

    def mapThermostatmode = [
            "heat": {heat()},
            "cool": {cool()},
            "emergency heat": {emergencyHeat()},
            "auto": {auto()},
            "off": {off()}
    ]
    mapThermostatmode[thermostatmode]()
}
/*
setCoolingSetpoint(temperature)
temperature required (NUMBER) - Cooling setpoint in degrees
setHeatingSetpoint(temperature)
*/
def setCoolingSetpoint(temperature) {
    def data = retrieveDeviceState()
    data['DefaultCoolingSetTemperature'] = temperature
    data = sendCommand(data)
    update(data)
}

def setHeatingSetpoint(temperature) {
    def data = retrieveDeviceState()
    data['DefaultHeatingSetTemperature'] = temperature
    data = sendCommand(data)
    update(data)
}

def setPreset(presetNr) {
    def deviceId = device.currentValue('DeviceID')
    def presets = parent.getPresets( deviceId )
    Map thePreset = presets.find {preset -> preset.Number == presetNr}

    if (thePreset) {
        def data = retrieveDeviceState()

        thePreset.each {
            if (data.containsKey(it.key)) {
                data[it.key] = it.value
            }
        }
        data['EffectiveFlags'] = 287
        data = sendCommand(data)
        update(data)
    }
}
