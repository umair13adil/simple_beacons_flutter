package com.umair.beacons_plugin_example

data class Beacon(
        var name: String? = "",
        var uuid: String? = "",
        var major: String = "",
        var minor: String = "",
        var distance: String = "",
        var proximity: String = ""
) {

    override fun toString(): String {
        return "{\n" +
                "  \"name\": \"$name\",\n" +
                "  \"uuid\": \"$uuid\",\n" +
                "  \"major\": \"$major\",\n" +
                "  \"minor\": \"$minor\",\n" +
                "  \"distance\": \"$distance\",\n" +
                "  \"proximity\": \"$proximity\"\n" +
                "}"
    }
}