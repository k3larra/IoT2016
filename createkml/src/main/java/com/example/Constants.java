package com.example;

/**
 * Created by K3LARA on 2016-05-03.
 */
public class Constants {
    public static final String startCode = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<kml xmlns=\"http://www.opengis.net/kml/2.2\">";
    public static final String endCode = "</kml>";
    public static final String startCode2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +
            "    <Document>\n" +
            "        <name>Paths</name>\n" +
            "        <description></description>\n" +
            "        <Style id=\"yellowLineGreenPoly\">\n" +
            "            <LineStyle>\n" +
            "                <color>7f00ffff</color>\n" +
            "                <width>4</width>\n" +
            "            </LineStyle>\n" +
            "            <PolyStyle>\n" +
            "                <color>7f00ff00</color>\n" +
            "            </PolyStyle>\n" +
            "        </Style>\n" +
            "        <Placemark>\n" +
            "            <name>Absolute Extruded</name>\n" +
            "            <description></description>\n" +
            "            <styleUrl>#yellowLineGreenPoly</styleUrl>\n" +
            "            <LineString>\n" +
            "                <extrude>1</extrude>\n" +
            "                <tessellate>1</tessellate>\n" +
            "                <altitudeMode>clampToGround</altitudeMode>";


    public static final String endCode2 = "</LineString>\n" +
            "        </Placemark>\n" +
            "    </Document>\n" +
            "</kml>";
}
