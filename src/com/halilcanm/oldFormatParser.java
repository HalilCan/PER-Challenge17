package com.halilcanm;

import java.io.File;
import java.nio.ByteBuffer;
import java.sql.Time;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static java.lang.Integer.max;
import static java.lang.Integer.parseUnsignedInt;
import static java.lang.Math.PI;
import static java.lang.Math.toIntExact;

public class oldFormatParser {
    private static final char DEFAULT_SEPARATOR = ' ';
    private static Hashtable idTable = new Hashtable<String, String>();
    private static LinkedList<LinkedList<String>> data = new LinkedList<LinkedList<String>>();
    //linkedlist because it's faster to add a new element

    public static void main(String ... args) throws Exception {
        //Insert
        String oldFile ="20150620_13_59_09_349_0_old";

        Scanner scanner = new Scanner(new File(oldFile));
        while (scanner.hasNext()) {
            LinkedList<String> line = parseLine(scanner.nextLine());
            if (line != null && !line.isEmpty()) {
                //System.out.println("Timestamp= " + line.get(0) + ", id= " + line.get(2) + line.get(1) + ", bytecount=
                // " + line.get(3) + "val = " + line.get(4));
                data.add(line);
                }
            }
        scanner.close();
        // Working getByID example: System.out.println(getByID("0x222"));
        // Working parseLittleEndian example:
        // System.out.println("Previous example:" + parseLittleEndian("[ 217, 184, 130, 65, 122, 175, 137, 65]"));
        // Working getById to get data: System.out.println(getByID("0x222").getLast().get(4));
        // Working getById + parseL.E. System.out.println(parseLittleEndian(getByID("0x222").get(2090).get(4)));
        // getSeconds("13:12:10.023");


        // The two functions below will output a speed graph, speed data, and power data. I put together the graph
        // because I was having problems with getting the actual speed. It helped me realize I had mistaken diameter
        // for radius. The graphing function might exceed your console's line limit. That is because I just wanted to
        // see fitting behavior.

        // The power data is inaccurate. My working theory is that the integration is wrong. However, I do not yet know.

        getSpeedData();
        getPowerData();
    }

    public static ArrayList<Double> parseLittleEndian(String endian) {
        ArrayList<Double> result = new ArrayList<Double>();
        LinkedList<String> extractedList = new LinkedList<String>();

        char separator = ',';
        boolean capturing = false;

        String hex1 = "";
        String hex2 = "";

        StringBuffer cur = new StringBuffer();

        char[] chars = endian.toCharArray();

        for (char ch : chars) {
            if (!capturing) {
                if (!Character.isLetter(ch) && ch != separator && ch != ' ' && ch != '[' && ch != ']') {
                    capturing = true;
                }
            }
            if (capturing) {
                if (ch == separator) {
                    extractedList.add(cur.toString());
                    cur = new StringBuffer();
                    capturing = false;
                } else if (ch == ']') {
                    extractedList.add(cur.toString());
                    capturing = false;
                } else {
                    cur.append(ch);
                }
            }
        }
        int h1Count = 0;

        for (String extractedHex : extractedList) {
            String correctedHex = Integer.toHexString(Integer.valueOf(extractedHex));
            if (correctedHex.length() == 1) {
                correctedHex = "0" + correctedHex;
            }
            if (h1Count < 4) {
                //System.out.println("raw1 = " + (Integer.valueOf(extractedHex)));
                //System.out.println("hex1 = " + correctedHex);

                hex1 = correctedHex + hex1;
            }
            if (h1Count >= 4) {
                //System.out.println("raw2 = " + (Integer.valueOf(extractedHex)));
                //System.out.println("hex2 = " + correctedHex);

                hex2 = correctedHex + hex2;
            }
            h1Count +=1;
        }

        //System.out.println(hex1);
        //System.out.println(hex2);

        //switched order to big endian
        /*Long testLong = Long.parseLong("4182b8d9", 16);
        Float testFloat = Float.intBitsToFloat(testLong.intValue());
        Double testDouble = (double) testFloat;
        System.out.println("test= " + testDouble);
        */

        Long l1 = Long.parseLong(hex1, 16);
        Float f1 = Float.intBitsToFloat(l1.intValue());
        Double d1 = (double) f1;
        result.add(d1);

        if (hex2.length() > 0) {
            Long l2 = Long.parseLong(hex2, 16);
            Float f2 = Float.intBitsToFloat(l2.intValue());
            Double d2 = (double) f2;
            result.add(d2);
        }
        return result;
    }

    public static Double RPMtoMPH (Double RPM) {
        Double tireDiameterInches = 19.5;
        //IT'S DIAMETER NOT RADIUS GODDAMMIT
        Double tireCircumferenceInches = 122.5 / 2.0;
        Double tireCircumferenceMiles = 0.0009667;

        return tireCircumferenceMiles * RPM * 60;
    }

    public static Double getSeconds (String time) {
        String h = time.substring(0,2);
        String m = time.substring(3,5);
        String s = time.substring(6,12);

        Double hd = Double.valueOf(h) * 3600.0;
        Double md = Double.valueOf(m) * 60.0;
        Double sd = Double.valueOf(s);
        Double totalSeconds = hd + md + sd;
        return totalSeconds;
    }

    public static LinkedList<String> parseLine (String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR);
    }

    public static void getPowerData() {
        System.out.println("SPEED DATA ABOVE:");
        System.out.println("POWER DATA BELOW:");
        String currentAddress = "0x311";
        String voltageAddress = "0x313";

        Double init_power = 0.0;
        Double initial_time_difference = 0.0;
        Double zero_time = getSeconds(data.getFirst().get(0));

        LinkedList<LinkedList<String>> currentList = getByID(currentAddress);
        LinkedList<LinkedList<String>> voltageList = getByID(voltageAddress);

        LinkedList<Double> numTimeDifferenceListC = new LinkedList<>();
        LinkedList<Double> currentTrapezoids = new LinkedList<>();

        LinkedList<Double> numCurrentList = new LinkedList<>();
        Double prevCurrent = 0.0;

        //integrate current over time here
        for (List<String> line : currentList) {
            Double current = parseLittleEndian(line.get(4)).get(0);
            Double absTime = getSeconds(line.get(0)) - zero_time;
            Double timeDiff = 0.0;

            numCurrentList.add(current);

            if (numTimeDifferenceListC.size() < 1 || numTimeDifferenceListC.isEmpty()) {
                numTimeDifferenceListC.add(absTime);
                timeDiff = absTime;
            } else {
                timeDiff = absTime - numTimeDifferenceListC.get(numTimeDifferenceListC.size()-1);
                numTimeDifferenceListC.add(absTime);
            }

            currentTrapezoids.add((current + prevCurrent) * timeDiff / 2.0);
            prevCurrent = current;
        }

        Double totalCurrent = sumAll(currentTrapezoids);
        System.out.println("Integrated current= " + totalCurrent);

        LinkedList<Double> numTimeDifferenceListV = new LinkedList<>();
        LinkedList<Double> voltageTrapezoids = new LinkedList<>();

        LinkedList<Double> numVoltageList = new LinkedList<>();
        Double prevVoltage = 0.0;

        //integrate voltage over time here
        for (List<String> line : voltageList) {
            Double voltage = parseLittleEndian(line.get(4)).get(0);
            Double absTime = getSeconds(line.get(0)) - zero_time;
            Double timeDiff = 0.0;

            numVoltageList.add(voltage);

            if (numTimeDifferenceListV.size() < 1 || numTimeDifferenceListV.isEmpty()) {
                numTimeDifferenceListV.add(absTime);
                timeDiff = absTime;
            } else {
                timeDiff = absTime - numTimeDifferenceListV.get(numTimeDifferenceListV.size()-1);
                numTimeDifferenceListV.add(absTime);
            }
            voltageTrapezoids.add((voltage + prevVoltage) * timeDiff / 2.0);
            prevVoltage= voltage;
        }

        Double totalVoltage = sumAll(voltageTrapezoids);
        System.out.println("Integrated voltage= " + totalVoltage);

        Double totalPower = (totalCurrent * totalVoltage) / (numTimeDifferenceListV.getLast()-numTimeDifferenceListV
                .getFirst()) / 3000000.0;
        System.out.println("Total power kWh= " + totalPower);

        LinkedList<Double> powerTrapezoids2 = new LinkedList<Double>();
        Double prevPower2 = 0.0;

        LinkedList<Double> numTimeDifferenceListC2 = new LinkedList<>();

        for (List<String> line : currentList) {
            Double current = parseLittleEndian(line.get(4)).get(0);;
            Double absTime = getSeconds(line.get(0)) - zero_time;
            Double voltage = parseLittleEndian(voltageList.removeFirst().get(4)).get(0);
            Double power = current * voltage;
            Double timeDiff = 0.0;

            if (numTimeDifferenceListC2.size() < 1 || numTimeDifferenceListC2.isEmpty()) {
                numTimeDifferenceListC2.add(absTime);
                timeDiff = absTime;
            } else {
                timeDiff = absTime - numTimeDifferenceListC2.get(numTimeDifferenceListC2.size()-1);
                // the problem might be here: change .add from timeDiff to absTime, otherwise it seems too large a diff
                numTimeDifferenceListC2.add(absTime);
            }
            //dammit I put an extra 2.0 divisor in the pp2*tD  It doesn't have to be a rectangle!
            powerTrapezoids2.add((power + prevPower2) * timeDiff / 2.0);
            prevPower2 = power;
        }

        Double totalPower2 = sumAll(powerTrapezoids2) / 3000000.0;
        System.out.println("Total power from node by node calculation kWh: " + totalPower2);
    }

    public static void printXGraph(Double d) {
        System.out.println("GRAPH BEGIN");
        d = Math.floor(d);
        while (d > 0) {
            System.out.print("x");
            d  = d - 2;
        }
        System.out.println("GRAPH END");
        System.out.println("");
    }

    public static Double sumAll(LinkedList<Double> traps) {
        Double sum = 0.0;
        for (Double trap : traps) {
            sum = sum + trap;
        }
        return sum;
    }

    public static void getSpeedData() {
        LinkedList<LinkedList<String>> speedList = getByID("0x222");
        LinkedList<Double> speedLeft = new LinkedList<Double>();
        LinkedList<Double> speedRight = new LinkedList<Double>();

        for (LinkedList<String> speedData: speedList) {
            speedLeft.add(parseLittleEndian(speedData.get(4)).get(0));
            speedRight.add(parseLittleEndian(speedData.get(4)).get(1));
        }

        Double minSpeedF0 = 100000.0;
        Double maxSpeedF0 = 0.0;
        Double cumulativeSpeedF0 = 0.0;

        for (Double speed: speedLeft) {
            printXGraph(RPMtoMPH(speed));
            if (RPMtoMPH(speed) < minSpeedF0 && speed > 0.1) {
                minSpeedF0 = RPMtoMPH(speed);
            }
            if (RPMtoMPH(speed) > maxSpeedF0) {
                maxSpeedF0 = RPMtoMPH(speed);
            }
            if (speed > 0.0) {
                cumulativeSpeedF0 = cumulativeSpeedF0 + RPMtoMPH(speed);
            }
        }

        Double avgSpeedF0 = cumulativeSpeedF0 / speedLeft.size();
        //maxSpeedF0 = RPMtoMPH(maxSpeedF0);
        //minSpeedF0 = RPMtoMPH(minSpeedF0);

        System.out.println("Average front-0 mph= " + avgSpeedF0);
        System.out.println("Minimum front-0 mph= " + minSpeedF0);
        System.out.println("Maximum front-0 mph= " + maxSpeedF0);

        Double minSpeedF1 = 1000000.0;
        Double maxSpeedF1 = 0.0;
        Double cumulativeSpeedF1 = 0.0;

        for (Double speed: speedRight) {
            if (RPMtoMPH(speed) < minSpeedF1 && speed > 0.1) {
                minSpeedF1 = RPMtoMPH(speed);
            }
            if (RPMtoMPH(speed) > maxSpeedF1) {
                maxSpeedF1 = RPMtoMPH(speed);
            }
            if (speed > 0.0) {
                cumulativeSpeedF1 = cumulativeSpeedF1 + RPMtoMPH(speed);
            }
        }

        Double avgSpeedF1 = cumulativeSpeedF1 / (speedRight.size());
        //maxSpeedF1 = RPMtoMPH(maxSpeedF1);
        //minSpeedF1 = RPMtoMPH(minSpeedF1);

        System.out.println("Average front-1 mph= " + avgSpeedF1);
        System.out.println("Minimum front-1 mph= " + minSpeedF1);
        System.out.println("Maximum front-1 mph= " + maxSpeedF1);
        System.out.println("");
    }

    public static LinkedList<LinkedList<String>> getByID(String id) {
        LinkedList<LinkedList<String>> sortedList = new LinkedList<LinkedList<String>>();
        for (LinkedList<String> line: data) {
            String right = line.get(2).substring(2);
            String left = line.get(1);
            String construct = "0x" + left + right;
            if (construct.equals(id)) {
                sortedList.add(line);
            }
        }
        return sortedList;
    }

    public static  LinkedList<String> parseLine(String cvsLine, char separator) {
        LinkedList<String> result = new LinkedList<>();
        List<String> IDs = new ArrayList<>();
        List<String> header = new ArrayList<>();

        if (cvsLine == null && cvsLine.isEmpty()) {
            return result;
        }

        char dataSeparator = ' ';
        char headerSeparator = '-';

        StringBuffer cur = new StringBuffer();
        boolean collectingData = false;
        boolean collectingIDs = false;
        boolean collectingHeader = false;
        boolean inline = false;
        boolean inCurlies = false;

        String currentId = "";
        String currentIdVal = "";

        char[] chars = cvsLine.toCharArray();

        for (char ch : chars) {
            if (!inline) {
                if (Character.isLetter(ch)) {
                    if (ch == 'P') {
                        collectingHeader = true;
                    }
                } else {
                    collectingData = true;
                }
                inline = true;
            }

            if (collectingHeader) {
                if (ch != headerSeparator && ch != '\n' && ch != '\r') {
                    cur.append(ch);
                } else if (ch == headerSeparator) {
                    header.add(cur.toString());
                    cur = new StringBuffer();
                } else if (ch == '\r') {
                    continue;
                } else if (ch == '\n') {
                    header.add(cur.toString());
                    break;
                } else {
                    cur.append(ch);
                }
            }
            if (collectingData) {
                if (ch != separator && ch != '\n' && ch != '\r') {
                    cur.append(ch);
                    if (ch == '[') {
                        inCurlies = true;
                    }
                    if (ch == ']') {
                        inCurlies = false;
                    }
                } else if (ch == separator && inCurlies == false) {
                    result.add(cur.toString());
                    cur = new StringBuffer();
                } else if (ch == '\r') {
                    continue;
                } else if (ch == '\n') {
                    break;
                } else {
                    cur.append(ch);
                }
            }
        }

        if (collectingData) {
            result.add(cur.toString());
        }

        return result;
    }
}
