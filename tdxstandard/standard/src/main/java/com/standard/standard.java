package com.standard;

import java.io.IOException;
import java.util.Scanner;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
// import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.File;
import org.apache.commons.lang3.StringUtils;
import org.json.*;


public class standard 
{

    static JSONObject jsonResponse = null;

    static JSONObject postedJson = null;

    //Just need to decide the standard way to pass in args in the command line
    //I.e what order the token is passed in compared to the sheet of the asset Ids

    /* The input should come from a well formatted input file following the convention:
     * First line is the bearer token
     * Second and following lines are a singular assetID. */
    public static void main( String[] args ) throws IOException, InterruptedException {
        System.setIn(new FileInputStream(new File("TDXtest.txt")));
        
        Scanner input = new Scanner(System.in);

        String Bearer = input.nextLine();

        File output = new File("TestOut.txt");

        PrintStream stream = new PrintStream(output);

        System.setOut(stream);

        System.out.println("Asset ID: Serial In  Memory In  MAC In  Processor In  |  Serial Out  Memory Out  MAC Out  Processor Out");

        while(input.hasNextLine()){
            //Run the get and post requests here
            String assetID = input.nextLine();

            System.out.print(assetID + ": ");

            Thread.sleep(300);

            getRequest(assetID, Bearer);

            SNstandard();

            memStand();

            standardizedMAC();

            processor();

            postRequest(assetID, Bearer);

            checker();

            jsonResponse = null;

            postedJson = null;
        }

        //System.out.println(jsonResponse.toString(4));

        input.close();
    }

    /* Sends the getRequest to the API and saves the json response to the 
     * jsonResponse object initialized in the main function. This function 
     * does not change anything in the API, only takes in the data. */
    public static void getRequest(String assetID, String Bearer) throws IOException, InterruptedException {
        //First build the URI
        URL url = new URL("https://teamdynamix.umich.edu/SBTDWebApi/api/48/assets/" + assetID);
        HttpURLConnection connect = (HttpURLConnection) url.openConnection();

        //setting the header to include the bearer token
        connect.setRequestProperty("Authorization", "Bearer " + Bearer);

        //Double check that the application/json type is actually useful or if using the jsoup import works better
        connect.setRequestProperty("Content-Type","application/json; charset=utf-8");
        connect.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));

        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null){
            response.append(inputLine);
        }

        in.close();

        jsonResponse = new JSONObject(response.toString());

        connect.disconnect();
    }


    /* Sends the postRequest to the API, with the updated version of the original Json response.
     * This function will make changes to the API.
     */
    public static void postRequest(String assetId, String Bearer) throws IOException, InterruptedException{
        URL url = new URL("https://teamdynamix.umich.edu/SBTDWebApi/api/48/assets/" + assetId);
        String postData = jsonResponse.toString();

        HttpURLConnection connect = (HttpURLConnection) url.openConnection();

        connect.setDoOutput(true);
        connect.setRequestMethod("POST");
        connect.setRequestProperty("Authorization", "Bearer " + Bearer);
        connect.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        connect.setRequestProperty("Content-Length", Integer.toString(postData.length()));
        // connect.setUseCaches(false);

        try (OutputStream outputStream = connect.getOutputStream()){
            outputStream.write(postData.getBytes());
            outputStream.flush();
        }

        // DataOutputStream dos = new DataOutputStream(connect.getOutputStream());
        // dos.writeBytes(postData);

        BufferedReader bf = new BufferedReader(new InputStreamReader(connect.getInputStream()));
        String line;
        StringBuffer response = new StringBuffer();
        while((line = bf.readLine()) != null){
            response.append(line);
        }
        postedJson = new JSONObject(response.toString());
        bf.close();

        connect.disconnect();
    }

    /* Takes in the Serial Number from the jsonResponse variable and updates it 
     * to an all caps version. Once updated it sets the updated value of the 
     * serial number to the jsonResponse variable. Prints the original value 
     * to the output file. */
    public static void SNstandard(){
        String updatedSN = jsonResponse.getString("SerialNumber").toUpperCase();

        System.out.print(jsonResponse.getString("SerialNumber") + " ");

        jsonResponse.put("SerialNumber", updatedSN);

    }

    /* Searches the jsonResponse to isolate the memory field. Once isolated
     * it will change the field to a standardized version with the memory input
     * (i.e. amount of memory an int) with an abreviated version of the units 
     * the memory is measured in. If there is no unit listed will change nothing.
     * If there are notes after the unit, THEY WILL BE DELETED.
     * If there are any units besides TB, GB, or MB, the field will be left unchanged. 
     * Requires: The memory to start with an int to make changes. */
    public static void memStand(){
        //first navigate the json object to find the memInput
        String memInput = "";
        JSONArray arrayMem = jsonResponse.getJSONArray("Attributes");
        int arrayIndexInput = 0;
        for(int i = 0; i < arrayMem.length(); i++){
            JSONObject currentObj = arrayMem.getJSONObject(i);
            if(currentObj.getInt("ID") == 3507){
                memInput = currentObj.getString("Value");
                arrayIndexInput = i;
            }
        }

        System.out.print(memInput + " ");

        //Empty string should remain empty
        if(memInput == ""){
            return;
        }

        //Most common case is 16 gb or 16 GB, while standard is 16GB
        int endDigit = 0;
        String standard = "";
        char[] string = memInput.toCharArray();
        //Potential Index Out of bounds error
        boolean afterDecimal = false;
        int numAfterDecimal = 0;
        while(endDigit < memInput.length() && (string[endDigit] >= '.' && string[endDigit] <= '9')){
            if(string[endDigit] == '.'){
                afterDecimal = true;
                numAfterDecimal++;
            }
            if(afterDecimal){
                if(string[endDigit] > '0' && string[endDigit] <= '9'){
                    if(numAfterDecimal == 1){
                        standard += '.';
                        standard += string[endDigit];
                        numAfterDecimal++;
                        endDigit++;
                    }else{
                        standard += string[endDigit];
                        endDigit++;
                        numAfterDecimal++;
                    }
                }else if(string[endDigit] == '0' && (string[endDigit + 1] > '0' && string[endDigit + 1] <= '9')){
                    if(numAfterDecimal == 1){
                        standard += '.';
                        standard += string[endDigit];
                        numAfterDecimal++;
                        endDigit++;
                    }else{
                        standard += string[endDigit];
                        endDigit++;
                        numAfterDecimal++;
                    }
                }else{
                    endDigit++;
                }
            }else{
                standard += string[endDigit];
                endDigit++;
            }
        }
        
        //Prevents indexing out of bounds
        if(endDigit == memInput.length() || endDigit == 0){
            return;
        }



        //Adds on the correct label with no space if no label at end, cant get to this code
        if(string[endDigit] == 'm' || string[endDigit] == 'M'){
            standard += "MB";
        }else if(string[endDigit] == 'g' || string[endDigit] == 'G'){
            standard += "GB";    
        }else if(string[endDigit] == 't' || string[endDigit] == 'T'){
            standard += "TB";
        }else if(string[endDigit] == ' ' && endDigit < memInput.length() - 1){
            if(string[endDigit + 1] == 'm' || string[endDigit + 1] == 'M'){
                standard += "MB";
            }else if(string[endDigit + 1] == 'g' || string[endDigit + 1] == 'G'){
                standard += "GB";
            }else if(string[endDigit + 1] == 't' || string[endDigit + 1] == 'T'){
                standard += "TB";
            }else{
                return;
            }
        }else{
            return;
        }

        arrayMem.getJSONObject(arrayIndexInput).put("Value", standard);
        return;
    }

    private static boolean validChar(final char s){
        if(s >= '0' && s <= '9'){
            return true;
        }else if(s >= 'A' && s <= 'Z'){
            return true;
        }else if(s >= 'a' && s <= 'z'){
            return true;
        }else{
            return false;
        }
    }

    /* Prints out the updated version of the JsonResponse
     * for the updated fields. If a field is empty, an extra space
     * will be printed. Otherwise, will print in hte order of SerialNumber
     * Memory, then MAC. */
    // Currently configed for only processor standardization
    public static void checker(){
        System.out.print(postedJson.get("SerialNumber") + " ");

        JSONArray attributes = postedJson.getJSONArray("Attributes");
        String mem = "";
        String mac = "";
        String processor = "";
        for(int i = 0; i < attributes.length(); i++){
            JSONObject currentObj = attributes.getJSONObject(i);
            if(currentObj.getInt("ID") == 3506){
                mac = currentObj.getString("Value");
            }
            if(currentObj.getInt("ID") == 3507){
                mem = currentObj.getString("Value");
            }
            if(currentObj.getInt("ID") == 3512){
                processor = currentObj.getString("Value");
            }
        }

        // System.out.println(processor);
        // Line below is the one that should run all the time
        System.out.println(mem + " " + mac + " " + processor);
    }

    /* Isolates and updates the MAC address in the jsonResponse to these specs:
     * Removes all separating characters from the address, makes all the 
     * letters capitalized. If more than one address in the field, will separate
     * the two addresses by a semi colon. Notable bug: If there are notes in the
     * MAC address field that are 12 characters long, the note will be treated
     * as another MAC address (pinged on one asset: 60404) */
    public static void standardizedMAC(){

        //Parsing for the current mac address
        String macInput = "";
        JSONArray arrayMem = jsonResponse.getJSONArray("Attributes");
        int indexMacAddress = 0;
        for(int i = 0; i < arrayMem.length(); i++){
            JSONObject currentObj = arrayMem.getJSONObject(i);
            if(currentObj.getInt("ID") == 3506){
                macInput = currentObj.getString("Value");
                indexMacAddress = i;
            }
        }

        System.out.print(macInput + " ");

        //if empty returns the empty string
        if(macInput == ""){
            return;
        }
    
        //focus on the larger scale problem of dividers (colons) 
        if(macInput.length() > 12){
            int i = 0;
            String standard = "";
            String capitalized = macInput.toUpperCase();
            char[] string = capitalized.toCharArray();
            int numDividors = 0;
            while(i < macInput.length()){
                if(validChar(string[i])){
                    standard += string[i];
                    if(((standard.length() - numDividors) % 12 == 0) && i < macInput.length() - 1){
                        standard += ';';
                        numDividors++;
                    }
                }
                i++;
            }
            char[] standardArr = standard.toCharArray();
            if(standardArr[standard.length() - 1] == ';'){
                standard = StringUtils.chop(standard);
                numDividors--;
            }
            if((standard.length() - numDividors) % 12 != 0){
                return;
            }else{
                arrayMem.getJSONObject(indexMacAddress).put("Value", standard);
                return;
            }
        }else if(macInput.length() == 12){
            arrayMem.getJSONObject(indexMacAddress).put("Value", macInput.toUpperCase());
            return;
        }else
            return;
        }

    /* 
    Next function to remove the trademarks from the processor field
    Need to find the id in the custom attributes array
    Take in the value, if the string doesn't contain () move on
    If so check for the trademarks, ie (TM) or (R)
    Remove those and leave the rest of the string unchanged, return that string to put into the json
    */
    // Processor Standardizer, missing json configuration
    static void processor(){
        
        String input = "";
        JSONArray arr = jsonResponse.getJSONArray("Attributes");
        int index = 0;
        for(int i = 0; i < arr.length(); i++){
            JSONObject currentObj = arr.getJSONObject(i);
            if(currentObj.getInt("ID") == 3512){
                input = currentObj.getString("Value");
                index = i;
            }
        }
        
        // This is now correct as processor has been added as last change in full program
        System.out.print(input + " | ");

        if(input == ""){
            return;
        }
        
        if (input.contains("(TM)") || input.contains("(R)")){
            while(input.contains("(TM)")){
                input = input.replace("(TM)", "");
            }
            while(input.contains("(R)")){
                input = input.replace("(R)", "");
            }
        }

        arr.getJSONObject(index).put("Value", input);

        return;
    }
}
