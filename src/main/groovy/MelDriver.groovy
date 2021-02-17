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
    state.authCode = obtainAuthToken()
}

def obtainAuthToken() {
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
    def authCode = ""

    try {

        httpPost(postParams)
                { resp ->
                    log.info("obtainAuthToken: ${resp.data}")
                    authCode = resp?.data?.LoginData?.ContextKey
                    log.info "obtainAuthToken: ContextKey - ${authCode}"
                }
    }
    catch (Exception e) {
        log.error("obtainAuthToken: Unable to query Mitsubishi Electric MELCloud: ${e}")
    }
    return authCode
}

def getListDevices() {

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
            log.info "data returned from ListDevices: ${resp.data}"
            data = resp.data
        }
    }
    catch (Exception e) {
        log.error "createChildACUnits : Unable to query Mitsubishi Electric MELCloud: ${e}"
    }
    return data

}

def updateChildDevices(buildings) {
    def addDevice = { acUnit -> // Each Device
        if (acUnit.size() > 0) {
            log.info "adding device ${acUnit}"
            vRoom = acUnit.DeviceName
            vUnitId = acUnit.DeviceID
            log.info "createChildACUnit: ${vUnitId}, ${vRoom}"

            def childDevice = findChildDevice(vUnitId, "AC")
            if (childDevide == null) {
                createChildDevice(vUnitId, vRoom, "AC")
                childDevice = findChildDevice(vUnitId, "AC")
                childDevice.sendEvent(name: "unitId", value: vUnitId)
            }
            childDevice.refresh()
        }

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
    }
}
