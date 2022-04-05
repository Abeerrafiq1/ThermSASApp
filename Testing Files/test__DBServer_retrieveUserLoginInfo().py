# Author: Abeer Rafiq

# Import required packages
import json
import time
import socket
import sqlite3
import textwrap

# Purpose of this class: This class tests the retrieveUserLoginInfo method in 
# in the Database Server. The method requires a username as a parameter.
# It extracts the password and messages associating with the username from the loginDB database.
# It combines the extracted data into a string that is to be returned.
class test_DBServer_retrieveUserLoginInfo:
    def __init__(self):
        # Set up connection to loginDB database
        dbpath = 'C:/Users/amanp/Documents/SYSC 4907/Testing/loginDB_test.db'
        self.__dbconnect = sqlite3.connect(dbpath); 
        self.__dbconnect.row_factory = sqlite3.Row
        self.__cursor = self.__dbconnect.cursor()    

    # When user wants to login, this method retrieves the user's password (to compare against the entered password)
    # and gets messages for the user.
    # Returns appropriate message to send to app with opcode.
    def retrieveUserLoginInfo(self, enteredUsername):
        try:
            # Retrieve password stored in loginDB, table User_Login_Info
            mysql = """SELECT """ + """password FROM User_Login_Info WHERE username = '""" + str(enteredUsername) +"""'"""
            myresult = self.__cursor.execute(mysql).fetchall()
            password = [dict(i) for i in myresult]
            
            # Retrieve messages stored in loginDB, table User_Login_Info
            mysql = """SELECT """ + """messages FROM User_Login_Info WHERE username = '""" + str(enteredUsername) +"""'"""
            myresult = self.__cursor.execute(mysql).fetchall()
            message = [dict(i) for i in myresult]

            # If password isn't empty
            if (len(password) > 0):
                # Extract password and messages
                DBpassword = password[0].get('password')
                messages = message[0].get('messages')
                # Create msg with password and messages to send to app
                toSend = '{"opcode" : "2", "password" : "' + str(DBpassword) + '", "messages" : "' + str(messages) + '"}'              
            else:
                toSend = '{"opcode" : "2", "password" : "", "messages" : "''"}' 
        except sqlite3.Error as e:
            if (self.__DEBUG):
                toSend = '{"opcode" : "2", "password" : "", "messages" : "''"}'
                print ('\nDatabase Error %s:' % e.args[0])
        # Return appropriate msg to send to app with messages and password
        return toSend  

    # TEST #1.1 - To test if password and messages are properly retrieved from the database
    # when a valid username is given. The string returned from the retrieveUserLoginInfo
    # method is compared with the expected string (manually copied from database).
    def test_retrieveUserLoginInfo_withUsername(self):
        # Input Parameter and call method
        testUsername = "Abeer"
        output = self.retrieveUserLoginInfo(testUsername)

        # Expected String to be Returned from the Method
        passwordInDB = "MyTest"
        messageInDB = "-----------------------------------------\n\n [2022-03-21 10:59:30]\n\nStove with ID 1 has been registered successfully!\n\n-----------------------------------------\n\n [2022-03-21 10:59:45]\n\nYour stove was on too long!\nMessages have been sent to your contacts\n\n-----------------------------------------"
        expectedOutput =  '{"opcode" : "2", "password" : "' + passwordInDB + '", "messages" : "' + messageInDB + '"}' 

        # Print Statements
        print("\n ****************************  TEST 1.1 -Testing retrieveUserLoginInfo() with username ****************************")
        print("\nInput: ")
        print("- " + "Inputted username" + " = " + testUsername) 
        print("- " + "DB password" + " = " + passwordInDB) 
        print("- " + "DB messages" + " = " + messageInDB.replace("\n", " "))
        print("\nExpected Ouput: \n- %s" % expectedOutput.replace("\n", " "))

        # Compare output returned by method and expected output
        if output.replace(" ", "") == expectedOutput.replace(" ", "") :
            print ("\nResult: PASS ")
        else:
            print ("\nResult: FAIL ")
            print ("\nReceived: \n- " + output.replace("\n", " "))
        print ("\n")
        return

    # TEST #1.2 - To test if the string returned from the retrieveUserLoginInfo is empty
    # if no username is given.
    def test_retrieveUserLoginInfo_withoutUsername(self):
        # Input Parameter and call method
        testUsername = ""
        output = self.retrieveUserLoginInfo(testUsername)
        
        # Expected String to be Returned from the Method
        expectedOutput =  '{"opcode" : "2", "password" : "" , "messages" : ""}' 

        # Print Statements
        print("\n ****************************  TEST 1.2 -Testing retrieveUserLoginInfo() with empty username ****************************")
        print("\nInput: ")
        print("- " + "Inputted username" + " = " + "None") 
        print("- " + "DB password" + " = " + "None") 
        print("- " + "DB messages" + " = " + "None")
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
    testClass = test_DBServer_retrieveUserLoginInfo()

    # Run Two Tests
    testClass.test_retrieveUserLoginInfo_withUsername()
    testClass.test_retrieveUserLoginInfo_withoutUsername()
    return
    
if __name__== "__main__":
    main()
