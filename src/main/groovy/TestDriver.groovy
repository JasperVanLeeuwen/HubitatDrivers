/*
 * Import URL: https://raw.githubusercontent.com/JasperVanLeeuwen/WeerOnlineHubitatDriver/master/WeerOnline-Driver.groovy
 *
 *	Copyright 2021 Jasper van Leeuwen
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *	use this file except in compliance with the License. You may obtain a copy
 *	of the License at:
 *
 *		http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software
 *	distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *	WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *	License for the specific language governing permissions and limitations
 *	under the License.
 *
 *
 */


import groovy.transform.Field

metadata
        {
            definition(name: "TestDriver", namespace: "JasperVanLeeuwen", author: "JasperVanLeeuwen", importUrl: "https://raw.githubusercontent.com/JasperVanLeeuwen/WeerOnlineHubitatDriver/master/WeerOnline-Driver.groovy")
                    {
                        capability "TemperatureMeasurement"
                        capability "Refresh"
                    }

        }


/*
	updated

	Doesn't do much other than call initialize().
*/

def refresh() {
    log.trace "Msg: updated ran"
    log.trace "Msg: installed ran"
    log.trace "state on installed ${state}"
    sendEvent(name: 'temperature', value: 21)
    log.trace "state on installed after sendEvent ${state}"
    state["test"] = 4
    log.trace "state on installed after state setting ${state}"
    log.trace "state on variable after state setting ${temperature}"
}

/*

	generic driver stuff

*/


/*
	installed

	Doesn't do much other than call initialize().
*/

def installed() {
    log.trace "Msg: installed ran"
    log.trace "state on installed ${state}"
    sendEvent(name: 'temperature', value: 22)
    log.trace "state on installed after sendEvent ${state}"
    state["test"] = 3
    log.trace "state on installed after state setting ${state}"
    log.trace "state on variable after state setting ${temperature}"

}
