import groovy.json.JsonOutput

metadata {
    definition(name: "MelDriver Parent Driver for Melcloud", namespace: "meldriver", author: "Jasper van Leeuwen") {

        capability "Refresh"

        attribute "authCode", "string"
    }

    preferences {
        input(name: "BaseURL", type: "string", title: "MELCloud Base URL", description: "Enter the base URL for the Mitsubishi Electric MELCloud Service", defaultValue: "https://app.melcloud.com", required: true, displayDuringSetup: true)
        input(name: "UserName", type: "string", title: "MELCloud Username / Email", description: "Username / Email used to authenticate on Mitsubishi Electric MELCloud", displayDuringSetup: true)
        input(name: "Password", type: "password", title: "MELCloud Account Password", description: "Password for authenticating on Mitsubishi Electric MELCloud", displayDuringSetup: true)
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: true
    }

}

def initialize() {
    if (![UserName, BaseURL, Password].({String val-> val.isEmpty()})) {
        refresh()
    }
}

def refresh() {
    try {
        state.authCode = obtainAuthToken()
        retrieveAndUpdateDevices()
    }
    catch (Exception e) {
        log.error("refresh: Unable to query Mitsubishi Electric MELCloud: ${e}")
    }
}

def obtainAuthToken() throws Exception {
    def body = [
            Email: UserName,
            Password: Password,
            Language: "0",
            AppVersion: "1.18.5.1",
            Persist: "True",
            CaptchaResponse: ""
    ]

    def headers = [
            "Content-Type": "application/json; charset=UTF-8",
            Accept: "application/json",
            Referer: BaseURL,
            Origin: BaseURL,
            "Sec-Fetch-Mode": "cors"
    ]

    def postParams = [
            uri        : "${BaseURL}/Mitsubishi.Wifi.Client/Login/ClientLogin",
            headers    : headers,
            contentType: "application/json; charset=UTF-8",
            body       : JsonOutput.toJson(body)
    ]
    def authCode = null

    httpPost(postParams)
            { resp ->
                log.trace("obtainAuthToken: ${resp.data}")
                authCode = resp?.data?.LoginData?.ContextKey
                log.trace "obtainAuthToken: ContextKey - ${authCode}"
            }

    return authCode
}

def retrieveAndUpdateDevices() {
    def buildings = getListDevices()
    updateChildDevices(buildings)
    updatePresetButtons(buildings)
}

def getListDevices() {
    if (state.authCode==null) {
        log.error("Not authenticated")
        return
    }

    def data = null
    def headers = [
            "Content-Type": "application/json; charset=UTF-8",
            "Accept": "application/json",
            "Referer": BaseURL,
            "X-MitsContextKey": state.authCode
            ]

    def getParams = [
            uri        : "${BaseURL}/Mitsubishi.Wifi.Client/User/ListDevices",
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

def extractDevices(buildings) {
    def devices = []

    def addDevice = {device -> devices.add(device)}

    buildings?.each { building ->
        building?.Structure?.Floors?.each { floor ->
            floor?.Areas?.each { area ->
                area.Devices?.each addDevice
            }
            floor?.Devices?.each addDevice
        }
        building?.Structure?.Areas?.each { area ->
            area?.Devices?.each addDevice
        }
        building?.Structure?.Devices?.each addDevice
    }
    return devices
}

def updateChildDevices(buildings) {
    def addDevice = { acUnit -> // Each Device
        if (acUnit.size() > 0) {
            log.trace "adding device ${acUnit}"
            vRoom = acUnit.DeviceName
            vUnitId = "${acUnit.DeviceID}"
            log.trace "createChildACUnit: ${vUnitId}, ${vRoom}"

            def childDevice = getChildDevice("vUnitId")
            if (childDevice == null) {
                childDevice = addChildDevice("meldriver", "MelDriver Child Driver for Melcloud", vUnitId, ["label":vRoom])
                childDevice.sendEvent("DeviceID", "${acUnit.DeviceID}")
                childDevice.sendEvent("DeviceName", "${acUnit.DeviceName}")
                childDevice.sendEvent("BuildingID", "${acUnit.BuildingID}")
            }
        }
    }
    log.trace "iterate over buildings"
    extractDevices(buildings).each addDevice
}

def getPresets(DeviceID) {
    def buildings = getListDevices()
    List devices = extractDevices(buildings)
    def device = devices.find {device -> "${device.DeviceID}"==DeviceID}
    return device['Presets']
}

def updatePresetButtons(buildings) {
    log.trace "iterate over buildings"
    extractDevices(buildings).each { device ->
        def presets = getPresets(device.DeviceID)
        presets.each {preset->createButton(device.DeviceID, preset)}
    }

}

/*
create a button per DeviceID-preset if necessary
 */
def createButton(DeviceID, preset) {
//todo: MAGIC
}