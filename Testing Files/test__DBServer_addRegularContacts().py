# Author: Abeer Rafiq

# Import required packages
import json
import time
import socket
import sqlite3
import textwrap

# Purpose of this class: This class tests the addRegularContacts method in the Database Server.
# The method requires a username and 3 contacts to add to the username's account as parameters.
# It first checks to see if the 3 contacts are valid to add. Then it updates the loginDB database
# to add the 3 contacts to the username's information in the database.
# It combines the validity of all three contacts into a string that is to be returned.
class test_DBServer_addRegularContacts:
    def __init__(self):
        # Set up connection to thermal_cooking database
        dbpath = 'C:/Users/amanp/Documents/SYSC 4907/Testing/loginDB_test.db'
        self.__dbconnect = sqlite3.connect(dbpath); 
        self.__dbconnect.row_factory = sqlite3.Row
        self.__cursor = self.__dbconnect.cursor()    


    # To check if username in database already exists.
    # Returns number of occurrences in the database for a specific username.
    # Used by register, addRegularContacts and addPhysicianContact methods.
    def usernameExists(self, username, defaultValue):
        mysql = "SELECT COUNT(*) FROM User_Login_Info WHERE username = '" + username + "'"
        try:
            # Retrieve number of occurrences of the username stored in loginDB, table User_Login_Info
            myresult = self.__cursor.execute(mysql).fetchall()
            count = [dict(i) for i in myresult]
            # Extract the number of occurrences
            userCount = count[0].get('COUNT(*)')
        except sqlite3.Error as e:
            if (self.__DEBUG):
                userCount = defaultValue
                print ('\nDatabase Error %s:' % e.args[0])
        # Return number of occurrences of username passed in
        return userCount 

    # When user wants to add regular contacts to the database, this method checks to see if contacts 
    # are valid and then updates the contacts for the user.
    # Returns appropriate message to send to app with opcode.
    def addRegularContacts(self, username, contactOne, contactTwo, contactThree):
        # Check Validity of contact 1
        if (contactOne != ''):
            # If username doesn't exist, it is invalid to add as contact
            if (self.usernameExists(contactOne, 0) != 1):
                one = 1
            # If username is current User, it is invalid to add the user them self
            elif (contactOne == username):
                one = 2
            # If there are repeated contacts, it is invalid
            elif (contactOne == contactTwo) or (contactOne == contactThree):
                one = 3
            # Valid contact 1 - to add contact to database
            else:
                one = 4
        else:
            # Valid contact 1 - to clear contact in database
            one = 4
        
        # Check Validity of contact 2
        if (contactTwo != ''):
            # If username doesn't exist, it is invalid to add as contact
            if (self.usernameExists(contactTwo, 0) != 1):
                two = 1
            # If username is current User, it is invalid to add the user them self
            elif (contactTwo == username):
                two = 2
            # If there are repeated contacts, it is invalid
            elif (contactTwo == contactOne) or (contactTwo == contactThree):
                two = 3
            # Valid contact 2 - to add contact to database
            else:
                two = 4
        else:
             # Valid contact 2 - to clear contact in database
            two = 4
        
        # Check Validity of contact 3
        if (contactThree != ''):
            # If username doesn't exist, it is invalid to add as contact
            if (self.usernameExists(contactThree, 0) != 1):
                three = 1
            # If username is current User, it is invalid to add the user them self
            elif (contactThree == username):
                three = 2
            # If there are repeated contacts, it is invalid
            elif (contactThree == contactOne) or (contactThree == contactTwo):
                three = 3
            # Valid contact 3 - to add contact to database
            else:
                three = 4
        else:
            # Valid contact 3 - to clear contact in database
            three = 4

        # If valid contacts to add or to clear contacts, update database contacts
        if (one == 4 and two == 4 and three == 4):
            mysql = "UPDATE User_Login_Info SET firstContact = '" + contactOne + "', secondContact = '" + contactTwo + "', thirdContact = '" + contactThree + "' WHERE username = '" + username + "'"   
            try:
                self.__cursor.execute(mysql)
                self.__dbconnect.commit()
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    print ('\nDatabase Error %s:' % e.args[0])

        # Setting to 5 means contacts were cleared (used to let app know of all cleared contacts)
        if (contactThree == '' and contactTwo == '' and contactOne == ''):
            one = 5
            two = 5
            three = 5
        # Create and send appropriate msg to app
        toSend = "{'opcode' : '7', 'contactOne' : '" + str(one) + "', 'contactTwo' : '" + str(two) + "', 'contactThree' : '" + str(three) + "'}"
        return toSend 
    
    
    # After the addRegularContacts method is called (supposed to update database) in a test, this method extracts the data from the database and
    # returns it to the test to ensure the addRegularContacts method properly updated the data.
    def getContactsToVerify(self, enteredUsername):
        # Retrieve data from loginDB, table User_Login_Info
        mysql = """SELECT """ + """firstContact, secondContact, thirdContact FROM User_Login_Info WHERE username = '""" + str(enteredUsername) +"""'"""
        try:
            myresult = self.__cursor.execute(mysql).fetchall()
            stoveInfo = [dict(i) for i in myresult]
            # Extract contact usernames
            contact1 = stoveInfo[0].get('firstContact')
            contact2 = stoveInfo[0].get('secondContact')
            contact3 = stoveInfo[0].get('thirdContact')
            # Create array to send to app
            ContactsUpdated = [contact1, contact2, contact3]
        except sqlite3.Error as e:
            if (self.__DEBUG):
                ContactsUpdated = []
                print ('\nDatabase Error %s:' % e.args[0])
        # Return array with contact info to send to app
        return ContactsUpdated  
    
    # TEST #3.1 - To test if contacts are properly added into the database
    # when valid contacts are given. The string returned from the addRegularContacts
    # method is compared with the expected string. The expected string is that each contact
    # should have number 4 associating with it to represent that the contact was valid to add. 
    def test_addRegularContacts_ValidContacts(self):

        # Initial Test to ensure returned string of addRegularContacts is correct

        # Input Parameter and call method
        testUsername = "Roland"
        testContactOne = "Melissa"
        testContactTwo = "Sammy"
        testContactThree = "Justin"
        output = self.addRegularContacts(testUsername, testContactOne, testContactTwo, testContactThree)

        # Expected String to be Returned from the Method
        expectedOutput =  "{'opcode' : '7', 'contactOne' : '" + "4" + "', 'contactTwo' : '" + '4' + "', 'contactThree' : '" "4" + "'}"

        # Print Statements
        print("\n ****************************  TEST 3.1 -Testing Output of addRegularContacts() with all three contacts properly entered  ****************************")
        print("\nInput: ")
        print("- " + "Inputted username" + " = " + testUsername) 
        print("- " + "Inputted contact 1" + " = " + testContactOne) 
        print("- " + "Inputted contact 2" + " = " + testContactTwo) 
        print("- " + "Inputted contact 3" + " = " + testContactThree) 
        print("\nExpected Ouput: \n- %s" % expectedOutput.replace("\n", " "))

        # Compare output returned by method and expected output
        if output.replace(" ", "") == expectedOutput.replace(" ", ""):
            print ("\nResult: PASS ")
        else:
            print ("\nResult: FAIL ")
            print ("\nReceived: \n- " + output.replace("\n", " "))

        # SUB TEST to ensure database was successfully updated
        
        # Get contacts stored in Database
        contactsFromDB = self.getContactsToVerify(testUsername)
       
        # Print Statements
        print("                                         ****  SUB-TEST  **** ")
        print("\nExpected DB Contact Updates for the Username: ")
        print("- " + "Contact 1" + " = " + testContactOne) 
        print("- " + "Contact 2" + " = " + testContactTwo) 
        print("- " + "Contact 3" + " = " + testContactThree) 

        # Compare contacts stored in database and expected output
        if contactsFromDB[0] == testContactOne and contactsFromDB[1] == testContactTwo and contactsFromDB[2] == testContactThree:
            print ("\nResult: PASS ")
        else:
            print ("\nResult: FAIL ")
            print ("\nWhich contacts got stored in DB: ")
            print("- " + "Contact 1" + " = " + contactsFromDB[0]) 
            print("- " + "Contact 2" + " = " + contactsFromDB[1]) 
            print("- " + "Contact 3" + " = " + contactsFromDB[2]) 
        print ("\n")
       
        return

    # TEST #3.2 - To test if contacts are properly removed from the database
    # when all empty contacts are given. The string returned from the addRegularContacts
    # method is compared with the expected string. The expected string is that each contact
    # should have number 5 associating with it to represent that all contacts were valid to remove. 
    def test_addRegularContacts_ClearingContacts(self):
        
        # Initial Test to ensure returned string of addRegularContacts is correct

        # Input Parameter and call method
        testUsername = "Roland"
        testContactOne = ''
        testContactTwo = ''
        testContactThree = ''
        output = self.addRegularContacts(testUsername, testContactOne, testContactTwo, testContactThree)
        
        # Expected String to be Returned from the Method
        expectedOutput =  "{'opcode' : '7', 'contactOne' : '" + "5" + "', 'contactTwo' : '" + '5' + "', 'contactThree' : '" "5" + "'}"

        # Print Statements
        print("\n ****************************  TEST 3.2 -Testing Output of addRegularContacts() with all three contacts to be cleared  ****************************")
        print("\nInput: ")
        print("- " + "Inputted username" + " = " + testUsername) 
        print("- " + "Inputted contact 1" + " = " + testContactOne) 
        print("- " + "Inputted contact 2" + " = " + testContactTwo) 
        print("- " + "Inputted contact 3" + " = " + testContactThree) 
        print("\nExpected Ouput: \n- %s" % expectedOutput.replace("\n", " "))

        # Compare output returned by method and expected output
        if output.replace(" ", "") == expectedOutput.replace(" ", ""):
            print ("\nResult: PASS ")
        else:
            print ("\nResult: FAIL ")
            print ("\nReceived: \n- " + output.replace("\n", " "))

        # SUB TEST to ensure database was successfully updated
        
        # Get contacts stored in Database
        contactsFromDB = self.getContactsToVerify(testUsername)
        
        # Print Statements
        print("\n                                         ****  SUB-TEST  **** ")
        print("\nExpected DB Contact Updates for the Username: ")
        print("- " + "Contact 1" + " = " + testContactOne) 
        print("- " + "Contact 2" + " = " + testContactTwo) 
        print("- " + "Contact 3" + " = " + testContactThree) 

        # Compare contacts stored in database and expected output
        if contactsFromDB[0] == testContactOne and contactsFromDB[1] == testContactTwo and contactsFromDB[2] == testContactThree:
            print ("\nResult: PASS ")
        else:
            print ("\nResult: FAIL ")
            print ("\nWhich contacts got stored in DB: ")
            print("- " + "Contact 1" + " = " + contactsFromDB[0]) 
            print("- " + "Contact 2" + " = " + contactsFromDB[1]) 
            print("- " + "Contact 3" + " = " + contactsFromDB[2]) 
        print ("\n")
        return

    # TEST #3.3 - To test when contact usernames are not regisered. The string returned from the addRegularContacts
    # method is compared with the expected string. The expected string is that each contact
    # should have number 1 associating with it to represent that non existing contacts are not allowed to be added.
    def test_addRegularContacts_NonExistantUsername(self):
        # Input Parameter and call method
        testUsername = "Roland"
        testContactOne = 'IdoNotExistInDB_1'
        testContactTwo = 'IdoNotExistInDB_2'
        testContactThree = 'IdoNotExistInDB_3'
        output = self.addRegularContacts(testUsername, testContactOne, testContactTwo, testContactThree)

        # Expected String to be Returned from the Method
        expectedOutput =  "{'opcode' : '7', 'contactOne' : '" + "1" + "', 'contactTwo' : '" + '1' + "', 'contactThree' : '" "1" + "'}"

        # Print Statements
        print("\n ****************************  TEST 3.3 -Testing Output of addRegularContacts() with contacts that do not exist  ****************************")
        print("\nInput: ")
        print("- " + "Inputted username" + " = " + testUsername) 
        print("- " + "Inputted contact 1" + " = " + testContactOne) 
        print("- " + "Inputted contact 2" + " = " + testContactTwo) 
        print("- " + "Inputted contact 3" + " = " + testContactThree) 
        print("\nExpected Ouput: \n- %s" % expectedOutput.replace("\n", " "))

        # Compare output returned by method and expected output
        if output.replace(" ", "") == expectedOutput.replace(" ", ""):
            print ("\nResult: PASS ")
        else:
            print ("\nResult: FAIL ")
            print ("\nReceived: \n- " + output.replace("\n", " "))
        print ("\n")
        return

    # TEST #3.4 - To test when contact usernames are the same. The string returned from the addRegularContacts
    # method is compared with the expected string. The expected string is that each contact
    # should have number 3 associating with it to represent that repeated contacts are not allowed to be added. 
    def test_addRegularContacts_RepeatedContacts(self):
        # Input Parameter and call method
        testUsername = "Roland"
        testContactOne = 'Melissa'
        testContactTwo = 'Melissa'
        testContactThree = 'Melissa'
        output = self.addRegularContacts(testUsername, testContactOne, testContactTwo, testContactThree)

        # Expected String to be Returned from the Method
        expectedOutput =  "{'opcode' : '7', 'contactOne' : '" + "3" + "', 'contactTwo' : '" + '3' + "', 'contactThree' : '" "3" + "'}"

        # Print Statements
        print("\n ****************************  TEST 3.4 -Testing Output of addRegularContacts() with all contacts same  ****************************")
        print("\nInput: ")
        print("- " + "Inputted username" + " = " + testUsername) 
        print("- " + "Inputted contact 1" + " = " + testContactOne) 
        print("- " + "Inputted contact 2" + " = " + testContactTwo) 
        print("- " + "Inputted contact 3" + " = " + testContactThree) 
        print("\nExpected Ouput: \n- %s" % expectedOutput.replace("\n", " "))

        # Compare output returned by method and expected output
        if output.replace(" ", "") == expectedOutput.replace(" ", ""):
            print ("\nResult: PASS ")
        else:
            print ("\nResult: FAIL ")
            print ("\nReceived: \n- " + output.replace("\n", " "))
        print ("\n")
        return

    # TEST #3.5 - To test when contact usernames are the same as the currently logged in username.
    # The string returned from the addRegularContacts method is compared with the expected string. 
    # The expected string is that each contact should have number 2 associating with it to represent
    # the contact username is the same as the currently logged in username which isn't allowed.
    def test_addRegularContacts_ContactSameAsUser(self):
        # Input Parameter and call method
        testUsername = "Roland"
        testContactOne = 'Roland'
        testContactTwo = 'Roland'
        testContactThree = 'Roland'
        expectedOutput =  "{'opcode' : '7', 'contactOne' : '" + "2" + "', 'contactTwo' : '" + '2' + "', 'contactThree' : '" "2" + "'}"

        # Expected String to be Returned from the Method
        output = self.addRegularContacts(testUsername, testContactOne, testContactTwo, testContactThree)

        # Print Statements
        print("\n ****************************  TEST 3.5 -Testing Output of addRegularContacts() with contacts being same as user  ****************************")
        print("\nInput: ")
        print("- " + "Inputted username" + " = " + testUsername) 
        print("- " + "Inputted contact 1" + " = " + testContactOne) 
        print("- " + "Inputted contact 2" + " = " + testContactTwo) 
        print("- " + "Inputted contact 3" + " = " + testContactThree) 
        print("\nExpected Ouput: \n- %s" % expectedOutput.replace("\n", " "))

        # Compare output returned by method and expected output
        if output.replace(" ", "") == expectedOutput.replace(" ", ""):
            print ("\nResult: PASS ")
        else:
            print ("\nResult: FAIL ")
            print ("\nReceived: \n- " + output.replace("\n", " "))
        print ("\n")
        return

    # TEST #3.6 - To test a mix of conditions. Test when contactOne is valid to add, contactTwo is the same
    # as the currently logged in user and contactThree is not registered.
    # The string returned from the addRegularContacts method is compared with the expected string. 
    # The expected string is that contactOne = 4 since it is a valid contact to add to DB, 
    # contactTwo = 2 since it is the same as current user and contactThree = 1 since NonExistantUser 
    # is not a registered user. DB will not be modified since all contacts are not valid or all empty.
    def test_addRegularContacts_CombinedConditions(self):
        # Input Parameter and call method
        testUsername = "Roland"
        testContactOne = 'Melissa'
        testContactTwo = 'Roland'
        testContactThree = 'NonExistantUser'
        output = self.addRegularContacts(testUsername, testContactOne, testContactTwo, testContactThree)

        # Expected String to be Returned from the Method
        expectedOutput =  "{'opcode' : '7', 'contactOne' : '" + "4" + "', 'contactTwo' : '" + '2' + "', 'contactThree' : '" "1" + "'}"

        # Print Statements
        print("\n ****************************  TEST 3.6 -Testing Output of addRegularContacts() with combined conditions  ****************************")
        print("\nInput: ")
        print("- " + "Inputted username" + " = " + testUsername) 
        print("- " + "Inputted contact 1 (Exists in DB)" + " = " + testContactOne) 
        print("- " + "Inputted contact 2 (Same as user)" + " = " + testContactTwo) 
        print("- " + "Inputted contact 3 (Doesn't exist in DB)" + " = " + testContactThree) 
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
    testClass = test_DBServer_addRegularContacts()

    # Run 6 tests
    testClass.test_addRegularContacts_ValidContacts()
    testClass.test_addRegularContacts_ClearingContacts()
    testClass.test_addRegularContacts_NonExistantUsername()
    testClass.test_addRegularContacts_RepeatedContacts()
    testClass.test_addRegularContacts_ContactSameAsUser()
    testClass.test_addRegularContacts_CombinedConditions()
    return
    
if __name__== "__main__":
    main()
