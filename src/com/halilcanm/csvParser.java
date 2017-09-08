package com.halilcanm;

import java.util.*;
import java.io.File;

public class csvParser {
    private static final char DEFAULT_SEPARATOR = ',';
    private static Hashtable idTable = new Hashtable<String, String>();
    private static LinkedList<List<String>> data = new LinkedList<List<String>>();
    //linkedlist because it's faster to add a new element

    public static void main(String ... args) throws Exception {
        String csvFile ="end2016.csv";

        Scanner scanner = new Scanner(new File(csvFile));
        while (scanner.hasNext()) {
            List<String> line = parseLine(scanner.nextLine());
            if (line != null && !line.isEmpty()) {
                //System.out.println("Timestamp= " + line.get(0) + ", id= " + line.get(1) + ", val = " + line.get(2));
                data.add(line);
                }
            }
        scanner.close();
        // Working getByID example: System.out.println(getByID("89"));
        getSpeedData();
        getPowerData();
    }

    private Hashtable genTermDict() {
        Hashtable semiConductorTerms = new Hashtable<String, String>();
        semiConductorTerms.put ("PCM", "pulse-code modulation");
        semiConductorTerms.put ("CHG", "");
        semiConductorTerms.put ("MOC", "model of computation");
        semiConductorTerms.put ("AMS", "analog/mixed signal");
        semiConductorTerms.put ("IMD", "intermetal dielectric");
        semiConductorTerms.put ("TEL", "telecommunication?");
        semiConductorTerms.put ("DIC", "Differential interference contrast");

        return semiConductorTerms;
    }

    public static String resolveID (String key) {
        //Keys need to have leading spaces right now
        return idTable.get(key).toString();
    }

    public static List<String> parseLine (String cvsLine) {
        return parseLine(cvsLine, DEFAULT_SEPARATOR);
    }

    public LinkedList<Double> getPowerFromVandI(LinkedList<Double> numCurrentList, LinkedList<Double> numVoltageList){
        LinkedList<Double> numPowerList = new LinkedList<Double>();

        while (numCurrentList.size() > 0) {
            Double current = numCurrentList.removeFirst();
            Double voltage = numVoltageList.removeFirst();
            Double power = current * voltage;
            numPowerList.add(power);
        }

        return numPowerList;
    }

    public static void getPowerData() {
        String currentAddress = "170";
        String voltageAddress = "172";
        String powerAddress = "173";

        Double init_power = 0.0;
        Double initial_time_difference = 0.0;

        LinkedList<List<String>> powerList = getByID("173");
        LinkedList<List<String>> currentList = getByID("170");
        LinkedList<List<String>> voltageList = getByID("172");

        LinkedList<Double> numPowerList = new LinkedList<>();
        LinkedList<Double> numCurrentList = new LinkedList<>();
        LinkedList<Double> numVoltageList = new LinkedList<>();

        LinkedList<Double> numCTimeDifferenceList = new LinkedList<>();
        LinkedList<Double> numVTimeDifferenceList = new LinkedList<>();

        LinkedList<Double> currentTrapezoids = new LinkedList<>();
        LinkedList<Double> voltageTrapezoids = new LinkedList<>();

        Double prevCurrent = 0.0;

        for (List<String> line : currentList) {
            Double current = Double.parseDouble(line.get(2));
            Double absTime = Double.parseDouble(line.get(0));
            Double timeDiff = 0.0;

            numCurrentList.add(current);

            if (numCTimeDifferenceList.size() < 1 || numCTimeDifferenceList.isEmpty()) {
                numCTimeDifferenceList.add(absTime);
                timeDiff = absTime;
            } else {
                timeDiff = absTime - numCTimeDifferenceList.get(numCTimeDifferenceList.size()-1);
                numCTimeDifferenceList.add(absTime);
            }

            currentTrapezoids.add ((current + prevCurrent) * timeDiff / 2.0);
            prevCurrent = current;
        }

        Double totalCurrent = sumAll(currentTrapezoids);

        Double prevVoltage = 0.0;

        for (List<String> line : voltageList) {
            Double voltage = Double.parseDouble(line.get(2));
            Double absTime = Double.parseDouble(line.get(0));
            Double timeDiff = 0.0;

            numCurrentList.add(voltage);

            if (numVTimeDifferenceList.size() < 1 || numVTimeDifferenceList.isEmpty()) {
                numVTimeDifferenceList.add(absTime);
                timeDiff = absTime;
            } else {
                timeDiff = absTime - numVTimeDifferenceList.get(numVTimeDifferenceList.size()-1);
                numVTimeDifferenceList.add(absTime);
            }

            voltageTrapezoids.add ((voltage + prevVoltage) * timeDiff / 2.0);
            prevVoltage = voltage;
        }

        Double totalVoltage = sumAll(voltageTrapezoids);

        Double totalPower = totalVoltage * totalCurrent / (numVTimeDifferenceList.getLast() - numVTimeDifferenceList
                .getFirst());

        System.out.println("Total integrated voltage= " + totalVoltage);
        System.out.println("Total integrated current= " + totalCurrent);

        System.out.println("Total Power (From V and I) kWh= " + totalPower);

        LinkedList<Double> powerTrapezoids2 = new LinkedList<Double>();
        Double prevPower2 = 0.0;

        for (List<String> line : currentList) {
            Double current = Double.parseDouble(line.get(2));
            Double absTime = Double.parseDouble(line.get(0));
            Double voltage = Double.parseDouble(voltageList.removeFirst().get(2));
            Double power = current * voltage;
            Double timeDiff = 0.0;

            if (numCTimeDifferenceList.size() < 1 || numCTimeDifferenceList.isEmpty()) {
                numCTimeDifferenceList.add(absTime);
                timeDiff = absTime;
            } else {
                timeDiff = absTime - numCTimeDifferenceList.get(numCTimeDifferenceList.size()-1);
                numCTimeDifferenceList.add(absTime);
            }

            powerTrapezoids2.add ((power + prevPower2) * timeDiff / 2.0);
            prevPower2 = power;
        }

        Double totalPower2 = sumAll(powerTrapezoids2);

        System.out.println("Total Power from node by node calculation kWh= " + totalPower2);
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

    public static LinkedList<List<String>> getByID(String id) {
        LinkedList<List<String>> sortedList = new LinkedList<List<String>>();
        for (List<String> line: data) {
            if (line.get(1).equals(id)) {
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

        if (separator == ' ') {
            separator = ',';
        }

        char idSeparator = ':';
        char headerSeparator = ' ';

        StringBuffer cur = new StringBuffer();
        boolean collectingData = false;
        boolean collectingIDs = false;
        boolean collectingHeader = false;
        boolean inline = false;

        String currentId = "";
        String currentIdVal = "";

        char[] chars = cvsLine.toCharArray();

        for (char ch : chars) {
            if (!inline) {
                if (Character.isLetter(ch)) {
                    if (ch == 'V') {
                        collectingIDs = true;
                    }
                    if (ch == 'P') {
                        collectingHeader = true;
                    }
                } else {
                    collectingData = true;
                }
                inline = true;
            }

            if (collectingIDs) {
                if (ch != idSeparator && ch != '\n' && ch != '\r') {
                    cur.append(ch);
                } else if (ch == idSeparator) {
                    //char[] idTag = Arrays.copyOfRange(cur.toString().toCharArray(), 6, cur.length()-1);
                    currentId = cur.toString();
                    cur = new StringBuffer();
                } else if (ch == '\r') {
                    continue;
                } else if (ch == '\n') {
                    currentId = "";
                    currentIdVal = "";
                    break;
                } else {
                    cur.append(ch);
                }
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
                } else if (ch == separator) {
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

        if (collectingIDs) {
            currentIdVal = cur.toString();
            idTable.put(currentIdVal,currentId);
            currentId = "";
            currentIdVal = "";
        }

        if (collectingData) {
            result.add(cur.toString());
        }

        return result;
    }
}
