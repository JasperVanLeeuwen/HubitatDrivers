# Development information

The groovy drivers have some basic integration tests that can run off hub. For this, a basic hub emulation environment is provided:
`/src/test/groovy/HubEmulator.groovy`

with as basic entities:
* `HubitatHubEmulator`: representing an hub
*  `HubitatDeviceEmulator`: representing a device



## HubitatHubEmulator
The code of a hubitat device handler can be registered with a hub with `HubitatHubEmulator.addTypeToImplementationMap` 

Subsequently, devices can be created with `HubitatHubEmulator.addChildDevice`

## HubitatDeviceEmulator
The `HubitatDeviceEmulator` class loads the device handler script and provides hubitat-like driver functionality
this is only a limited implementation, just what I needed to implement to support my device drivers.
it provides stuff like logging, httpPost, httpGet, state, currentState, and functionality to create child devices.
