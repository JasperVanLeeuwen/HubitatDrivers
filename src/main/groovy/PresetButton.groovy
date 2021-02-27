metadata {
    definition(name: "PresetButton for Melcloud", namespace: "meldriver", author: "Jasper van Leeuwen") {

        capability "PushableButton"
        capability "Momentary"

        attribute "DeviceID", "string" //MelChildDriver.DeviceID to push
        attribute "presetNr", "number" // preset number

    }


}

def installed() {
    sendEvent(name:"numberOfButtons", value:1)
}

def push() {
    def deviceId = device.currentValue('DeviceID')
    def presetNr = device.currentValue('presetNr')
    log.info " melcloud preset fort device ${deviceId} - preset $presetNr"
    def melChildDevice = parent.getChildDevice(deviceId)
    melChildDevice.setPreset( presetNr )
}