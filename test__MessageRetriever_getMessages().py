# Author: Abeer Rafiq

# Import required packages
import json
import time
import socket
import sqlite3

# Purpose of this class: This class tests the getMessages method in the Message Retriever.
# The method requires a username as a parameter.
# It extracts the messages associating with the username from the loginDB database.
# It combines the extracted data into a string that is to be returned.
class test_messageRetriever_getMessages:
    def __init__(self):
        # Set up connection to loginDB database
        dbpath = 'C:/Users/amanp/Documents/SYSC 4907/Testing/loginDB_test.db'
        self.__dbconnect = sqlite3.connect(dbpath); 
        self.__dbconnect.row_factory = sqlite3.Row
        self.__cursor = self.__dbconnect.cursor()    

    # The app polls for user messages every couple of seconds
    # so this method retrieves user messages and puts into a string
    # to send back to app.
    def getMessages(self, username):
        # Retrieve stored messages for user from loginDB, table User_Login_Info 
        mysql = """SELECT """ + """messages FROM User_Login_Info WHERE username = '""" + str(username) +"""'"""
        try:
            myresult = self.__cursor.execute(mysql).fetchall()
            stoveInfo = [dict(i) for i in myresult]
            # Extract messages
            notif = stoveInfo[0].get('messages')
            # Create string with messages to send to app
            toSend = '{"opcode" : "2", "messages" : "' + notif + '"}'
        except sqlite3.Error as e:
            if (self.__DEBUG):
                toSend = '{"opcode" : "2", "messages" : ""}'
                print ('\nDatabase Error %s:' % e.args[0])
        # Return appropriate msg to send to app
        return toSend

    # TEST #4.1 - To test if messages are properly retrieved from the database
    # when a valid username is given. The string returned from the getMessages
    # method is compared with the expected string (manually copied from database).
    def test_getMessages(self):
        # Input Parameter and call method
        testUsername = "Abeer"
        output = self.getMessages(testUsername)

        # Expected String to be Returned from the Method
        messageInDB = "-----------------------------------------\n\n [2022-03-21 10:59:30]\n\nStove with ID 1 has been registered successfully!\n\n-----------------------------------------\n\n [2022-03-21 10:59:45]\n\nYour stove was on too long!\nMessages have been sent to your contacts\n\n-----------------------------------------"
        expectedOutput =  '{"opcode" : "2", "messages" : "' + messageInDB +'"}' 

        # Print Statements
        print("\n ****************************  TEST 4.1 -Testing getMessages()  ****************************")
        print("\nInput: ")
        print("- " + "Inputted username" + " = " + testUsername) 
        print("- " + "DB messages" + " = " + messageInDB.replace("\n", " "))
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
    testClass = test_messageRetriever_getMessages()
    
    # Run the test
    testClass.test_getMessages()

    return
    
if __name__== "__main__":
    main()
