package com.umair.beacons_plugin

data class Beacon(
        var name: String? = "",
        var uuid: String? = "",
        var major: String = "",
        var minor: String = "",
        var distance: String = "",
        var proximity: String = "",
        var scanTime: String = "",
        var macAddress: String = "",
        var rssi: String = "",
        var txPower: String = ""
) {

    override fun toString(): String {
        return "{\n" +
                "  \"name\": \"$name\",\n" +
                "  \"uuid\": \"$uuid\",\n" +
                "  \"macAddress\": \"$macAddress\",\n" +
                "  \"major\": \"$major\",\n" +
                "  \"minor\": \"$minor\",\n" +
                "  \"distance\": \"$distance\",\n" +
                "  \"proximity\": \"$proximity\",\n" +
                "  \"scanTime\": \"$scanTime\",\n" +
                "  \"rssi\": \"$rssi\",\n" +
                "  \"txPower\": \"$txPower\"\n" +
                "}"
    }

    companion object {
        fun getProximityOfBeacon(beacon: org.altbeacon.beacon.Beacon): Proximity {
            return if (beacon.distance < 0.5) {
                Proximity.IMMEDIATE
            } else if (beacon.distance > 0.5 && beacon.distance < 3.0) {
                Proximity.NEAR
            } else if (beacon.distance > 3.0) {
                Proximity.FAR
            } else {
                Proximity.UNKNOWN
            }
        }
    }
}
