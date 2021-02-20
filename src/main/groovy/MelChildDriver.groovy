import groovy.json.JsonOutput

metadata {
    definition(name: "MelDriver Child Driver for Melcloud", namespace: "meldriver", author: "Jasper van Leeuwen") {
        capability "Refresh"
        capability "TemperatureMeasurement"
        capability "Thermostat"
        capability "thermostatMode"
    }
}

def refresh() {

}

def retrieveDeviceState() {

    if (parent.state.authCode==null) {
        log.error("Not authenticated")
        return
    }

    def data = null
    def headers = [
            "Content-Type": "application/json; charset=UTF-8",
            "Accept": "application/json",
            "Referer": parent.BaseURL,
            "X-MitsContextKey": parent.state.authCode
    ]
    def getParams = [
            uri        : "${parent.BaseURL}/Mitsubishi.Wifi.Client/Device/Get?id=${DeviceID}&buildingID=${BuildingID}",
            headers    : headers,
            contentType: "application/json; charset=UTF-8",
    ]
    try {

        httpGet(getParams) { resp ->
            log.trace "data returned from ListDevices: ${resp.data}"
            data = resp.data

        }
    }
    catch (Exception e) {
        log.error "createChildACUnits : Unable to query Mitsubishi Electric MELCloud: ${e}"
    }
    return data
}

def update(data) {
    temperature = data['RoomTemperature']
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
            Referer: parent.BaseURL,
            Origin: parent.BaseURL,
            "X-MitsContextKey": parent.state.authCode,
            "Sec-Fetch-Mode": "cors"
    ]
    def postParams = [
            uri        : "${parent.BaseURL}/Mitsubishi.Wifi.Client/Device/SetAta",
            headers    : headers,
            contentType: "application/json; charset=UTF-8",
            body       : JsonOutput.toJson(data)
    ]

    httpPost(postParams)
            { resp ->
                result = resp.data
                log.trace("off: ${data}")
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
    data = sendCommand(data)
    update(data)
}

def emergencyHeat(){
    def data = retrieveDeviceState()
    data['EffectiveFlags'] = 287
    data['Power'] = true
    data['OperationMode'] = 1
    data = sendCommand(data)
    update(data)
}
def heat() {
    def data = retrieveDeviceState()
    data['EffectiveFlags'] = 287
    data['Power'] = true
    data['OperationMode'] = 1
    data = sendCommand(data)
    update(data)
}
def setThermostatMode(thermostatmode) {

}