metadata {
    definition(name: "PresetButton for Melcloud", namespace: "meldriver", author: "Jasper van Leeuwen") {

        capability "PushableButton"
        capability "Momentary"

        attribute "DeviceID", "string" //MelChildDriver.DeviceID to push
        attribute "presetNr", "string" // preset number

    }


}

def installed() {
    sendEvent(name:"numberOfButtons", value:1)
}

def push() {
    log.debug "pushed"
    def melChildDevice = parent.getChildDevice(currentValue('DeviceID'))
    melChildDevice.setPreset( currentValue('presetNr') )
    sendEvent(name:"pushed", value:1)
}