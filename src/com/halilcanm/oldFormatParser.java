package com.halilcanm;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;

public class oldFormatParser {
    private static final char DEFAULT_SEPARATOR = ' ';
    private static Hashtable idTable = new Hashtable<String, String>();
    private static LinkedList<List<String>> data = new LinkedList<List<String>>();
    //linkedlist because it's faster to add a new element

    public static void main(String ... args) throws Exception {
        String oldFile ="20150620_13_59_09_349_0_old";

        Scanner scanner = new Scanner(new File(oldFile));
        while (scanner.hasNext()) {
            List<String> line = parseLine(scanner.nextLine());
            if (line != null && !line.isEmpty()) {
                //System.out.println("Timestamp= " + line.get(0) + ", id= " + line.get(2) + line.get(1) + ", bytecount=
                // " + line.get(3) + "val = " + line.get(4));
                data.add(line);
                }
            }
        scanner.close();
        // Working getByID example:
        System.out.println(getByID("0x222"));
        String example_hex = Integer.toHexString(131);
        int hex2 = 0x4182b8d9;
        hex2 = Integer.parseInt("0x4182b8d9", 32);
        float f = Float.intBitsToFloat(hex2);
        Double d = (double) f;
        System.out.println(f);
        System.out.println(d);
        System.out.printf("%f", f);

        //getSpeedData();
        //getPowerData();
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
                if (!Character.isLetter(ch) && ch != ',' && ch != ' ') {
                    capturing = true;
                }
            }
            if (capturing) {
                if (ch == ',') {
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

        for (String extractedHex : extractedList) {
            if (hex1.length() < 8) {
                hex1 = hex1 + Integer.parseInt(extractedHex);
            }
            if (hex1.length() == 8) {
                hex2 = hex2 + Integer.parseInt(extractedHex);
            }
        }
        hex1 = "0x" + hex1;


        if (hex2.length() > 0) {
            hex2 = "0x" + hex2;
        }

        return result;
    }


    /*public static String resolveID (String key) {
        //Keys need to have leading spaces right now
        return idTable.get(key).toString();
    } */

    public static Double RPMtoMPH (Double RPM) {
        Double tireDiameterInches = 19.5;
        Double tireCircumferenceInches = 122.5;
        Double tireCircumferenceMiles = 0.0019334;

        return tireCircumferenceMiles * RPM * 60;
    }

    public static List<String> parseLine (String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR);
    }

    /*
    public static void getPowerData() {
        String currentAddress = "170";
        String voltageAddress = "172";
        String powerAddress = "173";

        Double init_power = 0.0;
        Double initial_time_difference = 0.0;

        LinkedList<List<String>> powerList = getByID("173");
        LinkedList<Double> numPowerList = new LinkedList<>();
        LinkedList<Double> numTimeDifferenceList = new LinkedList<>();
        LinkedList<Double> trapezoids = new LinkedList<>();

        Double prevPower = 0.0;

        for (List<String> line : powerList) {
            Double power = Double.parseDouble(line.get(2));
            Double absTime = Double.parseDouble(line.get(0));
            Double timeDiff = 0.0;

            numPowerList.add(power);

            if (numTimeDifferenceList.size() < 1 || numTimeDifferenceList.isEmpty()) {
                numTimeDifferenceList.add(absTime);
                timeDiff = absTime;
            } else {
                timeDiff = absTime - numTimeDifferenceList.get(numTimeDifferenceList.size()-1);
                numTimeDifferenceList.add(timeDiff);
            }

            trapezoids.add(((power - prevPower) * timeDiff / 2.0) + (prevPower * timeDiff / 2));
            prevPower = power;
        }

        Double totalPower = sumAll(trapezoids) / 1000000000.0;
        System.out.println("Total Power kWh= " + totalPower);
    }

    public static Double sumAll(LinkedList<Double> traps) {
        Double sum = 0.0;
        for (Double trap : traps) {
            sum = sum + trap;
        }
        return sum;
    }

    public static void getSpeedData() {
        LinkedList<List<String>> speedListFront0 = getByID("95");
        LinkedList<List<String>> speedListFront1 = getByID("96");
        LinkedList<List<String>> speedListRear0 = getByID("97");
        LinkedList<List<String>> speedListRear1 = getByID("98");

        LinkedList<Double> numericSpeedListF0 = new LinkedList<Double>();

        Double minSpeedF0 = 100000.0;
        Double maxSpeedF0 = 0.0;
        Double cumulativeSpeedF0 = 0.0;

        for (List<String> msg : speedListFront0) {
            Double speed = Double.parseDouble(msg.get(2));
            if (speed < minSpeedF0 && speed > 0.0) {
                minSpeedF0 = speed;
            }
            if (speed > maxSpeedF0) {
                maxSpeedF0 = speed;
            }
            cumulativeSpeedF0 = cumulativeSpeedF0 + speed;
            numericSpeedListF0.add(speed);
        }

        Double avgSpeedF0 = cumulativeSpeedF0 / (speedListFront0.size());

        System.out.println("Average front-0 mph= " + avgSpeedF0);
        System.out.println("Minimum front-0 mph= " + minSpeedF0);
        System.out.println("Maximum front-0 mph= " + maxSpeedF0);


        LinkedList<Double> numericSpeedListF1 = new LinkedList<Double>();

        Double minSpeedF1= 100000.0;
        Double maxSpeedF1 = 0.0;
        Double cumulativeSpeedF1 = 0.0;

        for (List<String> msg : speedListFront1) {
            Double speed = Double.parseDouble(msg.get(2));
            if (speed < minSpeedF1 && speed > 0.0) {
                minSpeedF1 = speed;
            }
            if (speed > maxSpeedF1) {
                maxSpeedF1 = speed;
            }
            cumulativeSpeedF1 = cumulativeSpeedF1 + speed;
            numericSpeedListF1.add(speed);
        }

        Double avgSpeedF1 = cumulativeSpeedF1 / (speedListFront1.size());

        System.out.println("Average front-1 mph= " + avgSpeedF1);
        System.out.println("Minimum front-1 mph= " + minSpeedF1);
        System.out.println("Maximum front-1 mph= " + maxSpeedF1);
        System.out.println("");


        LinkedList<Double> numericSpeedListR0 = new LinkedList<Double>();

        Double minSpeedR0= 100000.0;
        Double maxSpeedR0 = 0.0;
        Double cumulativeSpeedR0 = 0.0;

        for (List<String> msg : speedListRear0) {
            Double speed = Double.parseDouble(msg.get(2));
            if (speed < minSpeedR0 && speed > 0.0) {
                minSpeedR0 = speed;
            }
            if (speed > maxSpeedR0) {
                maxSpeedR0 = speed;
            }
            cumulativeSpeedR0 = cumulativeSpeedR0 + speed;
            numericSpeedListR0.add(speed);
        }

        Double avgSpeedR0 = cumulativeSpeedR0 / (speedListRear0.size());

        System.out.println("Average rear-0 mph = " + avgSpeedR0);
        System.out.println("Minimum rear-0 mph= " + minSpeedR0);
        System.out.println("Maximum rear-0 mph= " + maxSpeedR0);

        LinkedList<Double> numericSpeedListR1 = new LinkedList<Double>();

        Double minSpeedR1= 100000.0;
        Double maxSpeedR1 = 0.0;
        Double cumulativeSpeedR1 = 0.0;

        for (List<String> msg : speedListRear1) {
            Double speed = Double.parseDouble(msg.get(2));
            if (speed < minSpeedR1 && speed > 0.0) {
                minSpeedR1 = speed;
            }
            if (speed > maxSpeedR1) {
                maxSpeedR1 = speed;
            }
            cumulativeSpeedR1 = cumulativeSpeedR1 + speed;
            numericSpeedListR1.add(speed);
        }

        Double avgSpeedR1 = cumulativeSpeedR1 / (speedListRear1.size());

        System.out.println("Average rear-1 mph= " + avgSpeedR1);
        System.out.println("Minimum rear-1 mph= " + minSpeedR1);
        System.out.println("Maximum rear-1 mph= " + maxSpeedR1);
    }
    */
    public static LinkedList<List<String>> getByID(String id) {
        LinkedList<List<String>> sortedList = new LinkedList<List<String>>();
        for (List<String> line: data) {
            if ((line.get(2)+line.get(1)).equals(id)) {
                sortedList.add(line);
            }
        }
        return sortedList;
    }

    public static  List<String> parseLine(String cvsLine, char separator) {
        List<String> result = new ArrayList<>();
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
