package com.halilcanm;

import java.util.*;
import java.io.File;

public class csvParser {
    private static final char DEFAULT_SEPARATOR = ',';
    private static Hashtable idTable = new Hashtable<String, String>();

    public static void main(String ... args) throws Exception {
        String csvFile = "acc2016.csv";

        Scanner scanner = new Scanner(new File(csvFile));
        while (scanner.hasNext()) {
            List<String> line = parseLine(scanner.nextLine());
            if (line != null && !line.isEmpty()) {
                //System.out.println("Timestamp= " + line.get(0) + ", id= " + line.get(1) + ", val = " + line.get(2));
                }
            }
        scanner.close();
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
        /*System.out.println("collectingIDs = " + collectingIDs);
        System.out.println("collectingData = " + collectingData);
        System.out.println("collectingHeader = " + collectingHeader);
        System.out.println("line = " + cvsLine);
        System.out.println("result = " + result);*/

        return result;
    }
}
