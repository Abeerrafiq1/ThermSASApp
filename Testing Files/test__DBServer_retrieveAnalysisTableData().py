# Author: Abeer Rafiq

# Import required packages
import json
import time
import socket
import sqlite3
import textwrap

# Purpose of this class: This class tests the retrieveAnalysisTableData method in the Database Server.
# The method requires a stoveID (uniquely associates with username) and a tableName as parameters.
# It extracts the stove analysis data associating with the stoveID and tableName from the thermal_cooking database.
# It combines the extracted data into a string that is to be returned.
class test_DBServer_retrieveAnalysisTableData:
    def __init__(self):
        # Set up connection to thermal_cooking database
        dbpath_cooking = 'C:/Users/amanp/Documents/SYSC 4907/Testing/thermal_cooking_test.db'
        self.__dbconnectCooking = sqlite3.connect(dbpath_cooking); 
        self.__dbconnectCooking.row_factory = sqlite3.Row
        self.__cursorCooking = self.__dbconnectCooking.cursor()   

    # When user wants to view a stove analysis table pertaining to a recorded video, this method
    # retrieves that data.
    # Returns appropriate message to send to app with opcode.
    def retrieveAnalysisTableData(self, stoveID, tableName):
        # If no stove ID is registered, then create msg for app showing empty data
        if (stoveID == ""):
            toSend = '{"opcode" : "18", "data" : "''"}'
        # If stove ID is registered, then create msg for app showing stove video data
        else:
            # Retrieve stove analysis data for requested table (tableName) from thermal_cooking database
            try:
                mysql = """SELECT """ + """time_elapsed, pan_temp, pan_area, num_food, food_temp, food_area, classification FROM """ + tableName
                myresult = self.__cursorCooking.execute(mysql).fetchall()
                # Extract data
                list = [dict(i) for i in myresult]
                # Create msg to send to app with stove analysis data
                toSend = '{"opcode" : "18", "data" : "' + str(list) +'"}'
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    toSend = '{"opcode" : "10", "data" : "''"}'
                    print ('\nDatabase Error %s:' % e.args[0])
        # Return appropriate msg to send to app
        return toSend  

    # TEST #2.1 - To test if stove analysis data is properly retrieved from the database
    # when a valid stoveID and tableName are given as parameters. The string returned from the retrieveAnalysisTableData
    # method is compared with the expected string (manually copied from database).
    def test_retrieveAnalysisTableData_withStoveID(self):
        # Input Parameter and call method
        testStoveID = "1"
        testtableName = "Burger_Analysis_Table_1"
        output = self.retrieveAnalysisTableData(testStoveID, testtableName)

        # Expected String to be Returned from the Method
        stoveDataInDB = "[{'time_elapsed': 200, 'pan_temp': 218.5, 'pan_area': 7107, 'num_food': 3, 'food_temp': '[158.2, 162.6, 155.6, 169.1]', 'food_area': '[293, 518, 217]', 'classification': 'on too long'}, {'time_elapsed': 210, 'pan_temp': 218.5, 'pan_area': 7081, 'num_food': 3, 'food_temp': '[158.2, 162.6, 155.6, 169.1]', 'food_area': '[308, 519, 225]', 'classification': 'off'}, {'time_elapsed': 220, 'pan_temp': 218.5, 'pan_area': 7036, 'num_food': 3, 'food_temp': '[158.2, 162.6, 155.6, 169.1]', 'food_area': '[306, 524, 239]', 'classification': 'on'}, {'time_elapsed': 230, 'pan_temp': 218.5, 'pan_area': 7030, 'num_food': 3, 'food_temp': '[158.2, 162.6, 155.6, 169.1]', 'food_area': '[310, 531, 253]', 'classification': 'off'}, {'time_elapsed': 240, 'pan_temp': 218.5, 'pan_area': 6813, 'num_food': 4, 'food_temp': '[158.2, 162.6, 155.6, 169.1]', 'food_area': '[333, 328, 234, 281]', 'classification': 'off'}, {'time_elapsed': 290, 'pan_temp': 218.5, 'pan_area': 6693, 'num_food': 3, 'food_temp': '[158.2, 162.6, 155.6, 169.1]', 'food_area': '[331, 618, 308]', 'classification': 'off'}, {'time_elapsed': 300, 'pan_temp': 218.5, 'pan_area': 6699, 'num_food': 3, 'food_temp': '[158.2, 162.6, 155.6, 169.1]', 'food_area': '[340, 630, 303]', 'classification': 'off'}, {'time_elapsed': 310, 'pan_temp': 218.5, 'pan_area': 6803, 'num_food': 3, 'food_temp': '[158.2, 162.6, 155.6, 169.1]', 'food_area': '[338, 610, 291]', 'classification': 'off'}, {'time_elapsed': 320, 'pan_temp': 218.5, 'pan_area': 7076, 'num_food': 3, 'food_temp': '[158.2, 162.6, 155.6, 169.1]', 'food_area': '[312, 554, 248]', 'classification': 'off'}, {'time_elapsed': 330, 'pan_temp': 218.5, 'pan_area': 6425, 'num_food': 4, 'food_temp': '[158.2, 162.6, 155.6, 169.1]', 'food_area': '[441, 373, 388, 355]', 'classification': 'off'}, {'time_elapsed': 350, 'pan_temp': 218.5, 'pan_area': 6565, 'num_food': 4, 'food_temp': '[158.2, 162.6, 155.6, 169.1]', 'food_area': '[364, 422, 376, 329]', 'classification': 'off'}, {'time_elapsed': 380, 'pan_temp': 218.5, 'pan_area': 6530, 'num_food': 4, 'food_temp': '[158.2, 162.6, 155.6, 169.1]', 'food_area': '[432, 372, 375, 331]', 'classification': 'off'}, {'time_elapsed': 390, 'pan_temp': 218.5, 'pan_area': 6753, 'num_food': 3, 'food_temp': '[158.2, 162.6, 155.6, 169.1]', 'food_area': '[338, 633, 314]', 'classification': 'off'}]"
        expectedOutput =  '{"opcode" : "18", "data" : "' + stoveDataInDB + '"}'

        # Print Statements
        print("\n ****************************  TEST 2.1 -Testing retrieveAnalysisTableData() with stoveID and table name ****************************")
        print("\nInput: ")
        print("- " + "Inputted stove ID" + " = " + testStoveID) 
        print("- " + "Inputted table name" + " = " + testtableName) 
        print("- " + "DB stove analysis data" + " = " + stoveDataInDB.replace("\n", " "))
        print("\nExpected Ouput: \n- %s" % expectedOutput.replace("\n", " "))

        # Compare output returned by method and expected output
        if output.replace(" ", "") == expectedOutput.replace(" ", ""):
            print ("\nResult: PASS ")
        else:
            print ("\nResult: FAIL ")
            print ("\nReceived: \n- " + output.replace("\n", " "))
        print ("\n")
        return
        
    # TEST #2.2 - To test if the string returned from the retrieveAnalysisTableData is empty
    # if no stoveID and tableName are given.
    def test_retrieveAnalysisTableData_withoutStoveID(self):
        # Input Parameter and call method
        testStoveID = ""
        testtableName = ""
        output = self.retrieveAnalysisTableData(testStoveID, testtableName)
        
        # Expected String to be Returned from the Method
        expectedOutput =  '{"opcode" : "18", "data" : "''"}'

        # Print Statements
        print("\n ****************************  TEST 2.2 -Testing retrieveAnalysisTableData() with empty table name and stoveID  ****************************")
        print("\nInput: ")
        print("- " + "Inputted stove ID" + " = " + "None") 
        print("- " + "Inputted table name" + " = " + "None") 
        print("- " + "DB stove analysis data" + " = " + "None")
        print("\nExpected Ouput: \n- %s" % expectedOutput.replace("\n", " "))

        # Compare output returned by method and expected output
        if output.replace(" ", "") == expectedOutput.replace(" ", ""):
            print ("\nResult: PASS ")
        else:
            print ("\nResult: FAIL ")
            print ("\nReceived: \n- " + output.replace("\n", " "))
        print ("\n")
        return

def main():
    DEBUG = True
    testClass = test_DBServer_retrieveAnalysisTableData()

    # Run Two Tests
    testClass.test_retrieveAnalysisTableData_withStoveID()
    testClass.test_retrieveAnalysisTableData_withoutStoveID()
    return
    
if __name__== "__main__":
    main()
