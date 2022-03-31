
# Author: Abeer Rafiq
# Purpose of this class: This class facilitates communication between the app and the database
# to either process user information data or stove analysis data.
# This class receives requests from the app and then processes the requests by either updating data 
# or retrieving data to send back to the app in an appropriate format.

# Import required packages
import json
import time
import socket
import sqlite3

# Create DatabaseServer class.
class DatabaseServer:
    # Constructor for database server
    def __init__(self, portReceive, appSendport, app_ip_addrs, debug):
        # To show print statements on console
        self.__DEBUG = debug
        # Set port and socke
        # t to receive requests from app
        self.__port = int(portReceive)
        self.__soc_recv = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        recv_address = ('', self.__port)
        self.__soc_recv.bind(recv_address)
        # Set port and socket to send messages to app
        self.__soc_send =  socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.__app_addrs = (app_ip_addrs, appSendport) 
        # Set ack string, ack timeouts and end times 
        self.__ackstr = '{"opcode" : "0"}'
        self.__ack_timeout = 1
        self.__ack_endTime = 3
        # Set number of times to resend a packet
        self.__numRetries = 3
        # Set up connection to loginDB database
        dbpath = 'C:/Users/amanp/Documents/SYSC 4907/loginDB.db'
        self.__dbconnect = sqlite3.connect(dbpath); 
        self.__dbconnect.row_factory = sqlite3.Row
        self.__cursor = self.__dbconnect.cursor()    
        # Set up connection to thermal_cooking database
        dbpath_cooking = 'C:/Users/amanp/Documents/SYSC 4907/thermal_cooking.db'
        self.__dbconnectCooking = sqlite3.connect(dbpath_cooking); 
        self.__dbconnectCooking.row_factory = sqlite3.Row
        self.__cursorCooking = self.__dbconnectCooking.cursor()        
        # Show initialization msg if debug
        if (self.__DEBUG):
            print("\nDatabaseServer Initialized")

    # Receives message sent by app and returns buffer.
    # Also sends ack message when message received.
    def receive(self):
        if (self.__DEBUG):
            print("\nWaiting to receive on port %d ... " % self.__port)
        # Constantly wait to receive
        while(1):
            try:
                # Try loading buffer  
                buf_noload, address = self.__soc_recv.recvfrom(self.__port)
                buf_noload = buf_noload.decode("utf-8")
                buf = json.loads(buf_noload)
                # If something received, send an ack, return buffer
                if len(buf) > 0:
                    if (self.__DEBUG):
                        print ("Received %s bytes from '%s': %s " % (len(buf), address[0], buf))
                    # Send ack
                    self.__soc_send.sendto(self.__ackstr.encode('utf-8'), self.__app_addrs)
                    if (self.__DEBUG):
                        print ("Sent %s to %s" % (self.__ackstr, (address[0], self.__port)))
                    # Return Buffer
                    return buf
                else:
                    if (self.__DEBUG):
                        print("Nothing received")
                    return None                
            except (ValueError, KeyError, TypeError):
                if (self.__DEBUG):
                    print("Loading Json String Caused an Error")
                return None
            except socket.timeout:
                if (self.__DEBUG):
                    print("Receiver is Timed Out")
                return None

    # Sends the message passed (strToString) to the app.
    # Also waits to receive an acknowledgement.
    def sendMsgOnSocket(self, strToString):   
        # Send message to app
        self.__soc_send.sendto(strToString.encode('utf-8'), self.__app_addrs)  
        
        # If Debug, print messages sent to app unless they are too long to display or contain password
        if (self.__DEBUG and (strToString.find('"opcode" : "2"') != -1)):
            print('\nMessage has been sent to App : ' + '{"opcode" : "2", "password" : -hidden- , "messages" : -too long to display- }')
        elif (self.__DEBUG and (strToString.find('"opcode" : "22"') != -1)): 
            print('\nMessage has been sent to App : ' + '{"opcode" : "22", "messages" : -too long to display- }')
        elif (self.__DEBUG) : 
            print("\nMessage has been sent to App : " + strToString)
        
        # Get start time
        startTime = time.time()
        endTime = self.__ack_endTime
        self.__soc_recv.settimeout(self.__ack_timeout)
        while (True):
            # Check if time elapsed is less than endTime amount of time
            if time.time() < (startTime + endTime):
                try:
                    # Wait to receive ack
                    if (self.__DEBUG):
                        print("Waiting for Acknowledgement . . .")
                    buf, address = self.__soc_recv.recvfrom(self.__port)
                    #If buf received, check to see if ack, if not ack then continue
                    buf = json.loads(buf)
                    if not len(buf):
                        continue
                    else:
                        # If acknowledgement received
                        if (buf.get("opcode") == "0"):
                            if (self.__DEBUG):
                                print("Acknowledgement Received")
                            return True
                        else:
                            continue
                except (ValueError, KeyError, TypeError):
                    continue
                except socket.timeout:
                    if (self.__DEBUG):
                        print("Receiver is Timed Out")
                    continue
            else:
                # No ack received in time allowed
                return False
        return  

    # Send msg to app up to numRetries amount of times.
    # Calls sendMsgOnSocket to send msg on socket to app.
    def sendAppMsg(self, message):
        i = 0
        while(i <= self.__numRetries):
            if (self.sendMsgOnSocket(message) == True):
                # Return if ack received
                return
            i = i + 1
        # number retries passed so stop trying to send message
        return   

    # To retrieve the stove ID associated with app user.
    # Returns the stove ID associated with app user.
    # Used by getStoveID and used before retrieveVideoList is called in the main method.
    def retrieveStoveID(self, enteredUsername):
        # Retrieve data from loginDB, table User_Login_Info
        mysql = """SELECT """ + """stoveID FROM User_Login_Info WHERE username = '""" + str(enteredUsername) +"""'"""
        try:
            myresult = self.__cursor.execute(mysql).fetchall()
            stoveInfo = [dict(i) for i in myresult]
            # Extract stove ID
            stoveID = stoveInfo[0].get('stoveID')
        except sqlite3.Error as e:
            if (self.__DEBUG):
                print ('\nDatabase Error %s:' % e.args[0])
        # Return stove ID 
        return stoveID  

    # When user wants to view their currently registered stove, this method
    # calls a method to retrieve the stove ID associated with app user.
    # Returns a msg with the stove ID stored to send to the app.
    def getStoveID(self, enteredUsername):
        stoveNum = self.retrieveStoveID(enteredUsername)
        toSend = '{"opcode" : "14", "stoveRegistered" : "' + str(stoveNum) + '"}'
        # Return msg with stove ID to send to app
        return toSend  

    # When user wants to view all contacts, this method retrieves the contacts associated with the user.
    # Returns appropriate message to send to app with opcode.
    def getContacts(self, enteredUsername):
        # Retrieve data from loginDB, table User_Login_Info
        mysql = """SELECT """ + """physicianContact, firstContact, secondContact, thirdContact FROM User_Login_Info WHERE username = '""" + str(enteredUsername) +"""'"""
        try:
            myresult = self.__cursor.execute(mysql).fetchall()
            stoveInfo = [dict(i) for i in myresult]
            # Extract contact usernames
            physician = stoveInfo[0].get('physicianContact')
            contact1 = stoveInfo[0].get('firstContact')
            contact2 = stoveInfo[0].get('secondContact')
            contact3 = stoveInfo[0].get('thirdContact')
            # Create msg to send to app
            toSend = '{"opcode" : "16", "physician" : "' + physician + '", "contact1" : "' + contact1 + '", "contact2" : "' + contact2 + '", "contact3" : "' + contact3 + '"}'
        except sqlite3.Error as e:
            if (self.__DEBUG):
                toSend = '{"opcode" : "16", "physician" : "", "contact1" : "", "contact2" : "", "contact3" : ""}'
                print ('\nDatabase Error %s:' % e.args[0])
        # Return msg with contact info to send to app
        return toSend  

    # To determine if a stove Id passing in (stoveID) 
    # exists as a registered stove in the database.
    # Returns number of occurrences of the stoveID passed in.
    # Used by addStoveID method.
    def stoveExists(self, stoveID):
        # Retrieve data from loginDB, table User_Login_Info
        mysql = "SELECT COUNT(*) FROM User_Login_Info WHERE stoveID = '" + stoveID + "'"
        try:
            myresult = self.__cursor.execute(mysql).fetchall()
            count = [dict(i) for i in myresult]
            # Extract number of rows that are associated with stoveID passed in
            # Stove IDs are unique so either stoveIDCount = 0 or 1
            stoveIDCount = count[0].get('COUNT(*)')
        except sqlite3.Error as e:
            if (self.__DEBUG):
                stoveIDCount = 0
                print ('\nDatabase Error %s:' % e.args[0])
        # Return number of occurrences of stoveID passed in
        return stoveIDCount 

    # When user wants to register a stove ID to a user's account, this method updates the stoveID
    # for the user.
    # Returns appropriate message to send to app with opcode.
    def addStoveID(self, username, stoveID):
        # If no stove ID entered (blank), it means database server has to clear the registered stove
        if (stoveID == ""):
            # Create cleared stoveID msg to send back to the app 
            toSend = '{"opcode" : "12", "validity" : "empty", "maxStoveID" : ""}'
            # Update data in loginDB, table User_Login_Info to clear registered stove
            mysql = "UPDATE User_Login_Info SET stoveID = '" + stoveID + "' WHERE username = '" + username + "'"  
            try:
                self.__cursor.execute(mysql)
                self.__dbconnect.commit()
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    print ('\nDatabase Error %s:' % e.args[0])
        
        # If stove ID already exists in database
        elif (self.stoveExists(stoveID) != 0):
            # Retrieve max stove ID stored in loginDB, table User_Login_Info
            mysql = "SELECT stoveID FROM  User_Login_Info where stoveID <> '' ORDER BY stoveID DESC LIMIT 1"
            try:
                myresult = self.__cursor.execute(mysql).fetchall()
                maxStove = [dict(i) for i in myresult]
                # Extract max stove ID
                maxStoveNum = maxStove[0].get('stoveID')
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    maxStoveNum = 1
                    print ('\nDatabase Error %s:' % e.args[0])
                    toSend = '{"opcode" : "12", "validity" : "no", "maxStoveID" : ""}'
            # Create msg to send to app with maximum stove number 
            # The app will suggest user to set stove ID to max stove number + 1
            toSend = '{"opcode" : "12", "validity" : "no", "maxStoveID" : "' + str(maxStoveNum) + '"}'
        
        # If valid stove ID entered by user
        else:
            # Create appropriate msg to send to app
            toSend = '{"opcode" : "12", "validity" : "yes", "maxStoveID" : ""}'
            # Update stove ID for user in loginDB, table User_Login_Info
            mysql = "UPDATE User_Login_Info SET stoveID = '" + stoveID + "' WHERE username = '" + username + "'"
            try:
                self.__cursor.execute(mysql)
                self.__dbconnect.commit()
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    print ('\nDatabase Error %s:' % e.args[0])
        # Return appropriate msg to send to app
        return toSend  

    # When user wants to view a stove analysis table pertaining to a recorded video, this method
    # retrieves that data.
    # Returns appropriate message to send to app with opcode.
    def retrieveAnalysisTableData(self, stoveID, username, tableName):
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

    # When user wants to view list of stove analysis tables, this method retrieves the video list.
    # Returns appropriate message to send to app with opcode.
    def retrieveVideoList(self, stoveID):
        # If no stove ID is registered, then create msg for app showing empty list
        if (stoveID == ""):
            toSend = '{"opcode" : "10", "videoList" : "''"}'
        # If stove ID is registered, then create msg for app showing stove video list
        else:
            # Retrieve stove video table names from thermal_cooking database, table videos
            try:
                mysql = """SELECT """ + """id, analysis_table_name, classification FROM videos WHERE stoveId = """ + str(stoveID) 
                myresult = self.__cursorCooking.execute(mysql).fetchall()
                # Extract stove video list
                list = [dict(i) for i in myresult]
                # Create msg to send to app with stove video list
                toSend = '{"opcode" : "10", "videoList" : "' + str(list) +'"}'
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    toSend = '{"opcode" : "10", "videoList" : "''"}'
                    print ('\nDatabase Error %s:' % e.args[0])
        # Return appropriate msg to send to app
        print(toSend)
        return toSend 

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
        
    # When user wants to register a username (username must not already exist in database), 
    # this method adds the user to the database.
    # Returns appropriate message to send to app with opcode.
    def register(self, username, password, isPhysician):
        if (self.usernameExists(username, 1) != 0):
            # If username already exists, create appropriate msg to send to app
            toSend = '{"opcode" : "4", "valid" : "no"}'
        else:
            # Update loginDB, table User_Login_Info to register user 
            if (isPhysician == True):
                # Physician column is Yes
                mysql = "INSERT INTO User_Login_Info VALUES ('" + username + "', '" + password + "', '', '', '', '', '', '-----------------------------------------', 'Yes')"                    
            else:
                # Physician column is No
                mysql = "INSERT INTO User_Login_Info VALUES ('" + username + "', '" + password + "', '', '', '', '', '', '-----------------------------------------', 'No')"    
            try:
                self.__cursor.execute(mysql)
                self.__dbconnect.commit()
                 # Create msg to send to app showing successful registration
                toSend = '{"opcode" : "4", "valid" : "yes"}'
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    toSend = '{"opcode" : "4", "valid" : "no"}'
                    print ('\nDatabase Error %s:' % e.args[0])
        # Send appropriate msg to app
        return toSend    
    
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
        
    # Checks to see if username is physician.
    # Returns Yes or No to determine if username is physician or not.
    # Used by addPhysicianContact method.
    def checkIfPhysician(self, username):
        try:
            # Retrieve if username stored in loginDB, table User_Login_Info is physician or not
            mysql = """SELECT """ + """isPhysician FROM User_Login_Info WHERE username = '""" + str(username) +"""'"""
            myresult = self.__cursor.execute(mysql).fetchall()
            result = [dict(i) for i in myresult]
            # Extract if user is physician
            isPhysician = result[0].get('isPhysician')
        except sqlite3.Error as e:
            if (self.__DEBUG):
                print ('\nDatabase Error %s:' % e.args[0])
        # Return if user is physician or not
        return isPhysician 
    
    # When user wants to add a physician contact (username must be a physician), this method adds the physician.
    # Returns appropriate message to send to app with opcode.
    def addPhysicianContact(self, username, physician):
        # If not empty username
        if (physician != ''):
            # If physician username doesn't exist, invalid
            if(self.usernameExists(physician, 0) != 1):
                validity = 1
            # If username isn't a physician, invalid
            elif (self.checkIfPhysician(physician) == 'No'):
                validity = 2
            # If physician to add is the stove owner them self
            elif (physician == username):
                validity = 3
            # Valid to add physician
            else:
                validity = 4
        # If empty username, validity = 5 to clear physician field
        else:
            validity = 5

        # Create appropriate msg to send to app
        toSend = '{"opcode" : "8", "physician" : "' + str(validity) + '"}'

        # If physician is valid to add or to clear, update in loginDB, table User_Login_Info
        if (validity == 4 or validity == 5):
            mysql = "UPDATE User_Login_Info SET physicianContact = '" + physician + "' WHERE username = '" + username + "'"   
            try:
                self.__cursor.execute(mysql)
                self.__dbconnect.commit()
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    print ('\nDatabase Error %s:' % e.args[0])
        # Send appropriate msg to app
        return toSend    

    # This method updates messages for a given username.
    # Used by getAndSetMessage method.
    def setMessages(self, username, Messages):
        mysql = "UPDATE User_Login_Info SET messages = '" + Messages + "' WHERE username = '" + username + "'"
        try:
            # Update messages for user in loginDB, table User_Login_Info
            self.__cursor.execute(mysql)
            self.__dbconnect.commit()
        except sqlite3.Error as e:
            if (self.__DEBUG):
                print ('\nDatabase Error %s:' % e.args[0])
    
    # This method gets the message for a user, updates the message string
    # and then calls another method to set the new messages.
    # Used by updateUserAndContactMessages and updateUserAndPhysicianMessages methods.
    def getAndSetMessage(self, username, ContactMessages):
        mysql = """SELECT """ + """messages FROM User_Login_Info WHERE username = '""" + str(username) +"""'"""
        try:
            # Retrieve previously stored messages for user from loginDB, table User_Login_Info
            myresult = self.__cursor.execute(mysql).fetchall()
            stoveInfo = [dict(i) for i in myresult]
            # Extract messages
            notif = stoveInfo[0].get('messages')
        except sqlite3.Error as e:
            if (self.__DEBUG):
                print ('\nDatabase Error %s:' % e.args[0])
        # Join the previously stored messages with the new message message passed in
        newMessages = notif + ContactMessages
        # Update the messages in the database 
        self.setMessages(username, newMessages)
   
    # When the stove owner and regular contact messages are to be updated (not physician),
    # then this method calls required methods to update messages of stove owner and all three users.
    # Doesn't require information to be returned back to app.
    def updateUserAndContactMessages(self, username, UserMessages, ContactMessages):
        # Update stove owner's messages
        self.getAndSetMessage(username, UserMessages)
        # ContactMessages can be "" when only the stove owner's messages are to be updated
        # such as when the stove owner registers a stove
        if (ContactMessages != ""):
            # Contact messages are to be updated
            # Retrieve the three contacts of the stove owner from loginDB, table User_Login_Info
            mysql = """SELECT """ + """firstContact, secondContact, thirdContact FROM User_Login_Info WHERE username = '""" + str(username) +"""'"""
            try:
                myresult = self.__cursor.execute(mysql).fetchall()
                stoveInfo = [dict(i) for i in myresult]
                # Extract the three stove owner's contacts
                contact1 = stoveInfo[0].get('firstContact')
                contact2 = stoveInfo[0].get('secondContact')
                contact3 = stoveInfo[0].get('thirdContact')
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    print ('\nDatabase Error %s:' % e.args[0])
            # If the contacts are not empty, update the contact's messages
            if (contact1 != ""):
                self.getAndSetMessage(contact1, ContactMessages)
            if (contact2 != ""):
                self.getAndSetMessage(contact2, ContactMessages)
            if (contact3 != ""):
                self.getAndSetMessage(contact3, ContactMessages)

    # When the stove owner and physician messages are to be updated,
    # then this method calls required methods to update messages for stove owner and physician.
    # Doesn't require information to be returned back to app.
    def updateUserAndPhysicianMessages(self, username, UserMessages, PhysicianMessages):
        # Update stove owner's messages
        self.getAndSetMessage(username, UserMessages)
        # Physician messages are to be updated
        # Retrieve the Physician contact of the stove owner from loginDB, table User_Login_Info
        mysql = """SELECT """ + """physicianContact FROM User_Login_Info WHERE username = '""" + str(username) +"""'"""
        try:
            myresult = self.__cursor.execute(mysql).fetchall()
            stoveInfo = [dict(i) for i in myresult]
            # Extract the physician contact
            physicianContact = stoveInfo[0].get('physicianContact')
        except sqlite3.Error as e:
            if (self.__DEBUG):
                print ('\nDatabase Error %s:' % e.args[0])
        # If the physician contact is not empty, update the physician's messages
        if (physicianContact != ""):
            self.getAndSetMessage(physicianContact, PhysicianMessages)

    # When the user wants to clear their messages, this method
    # clears previous messages. 
    # Doesn't require information to be returned back to app.
    def clearMessages(self, username):
        # Clear messages: set messages as --- line to act as a message separator for future messages
        mysql = "UPDATE User_Login_Info SET messages = '-----------------------------------------' WHERE username = '" + username + "'"
        try:
            self.__cursor.execute(mysql)
            self.__dbconnect.commit()
        except sqlite3.Error as e:
            if (self.__DEBUG):
                print ('\nDatabase Error %s:' % e.args[0])

    # When user presses the view messages button, this method
    # gets messages from database and puts them into a string
    # with the appropriate format to return to send to the app.
    def getMessages(self, username):
        # Retrieve stored messages for user from loginDB, table User_Login_Info 
        mysql = """SELECT """ + """messages FROM User_Login_Info WHERE username = '""" + str(username) +"""'"""
        try:
            myresult = self.__cursor.execute(mysql).fetchall()
            stoveInfo = [dict(i) for i in myresult]
            # Extract messages
            notif = stoveInfo[0].get('messages')
            # Create meg with messages to send to app
            toSend = '{"opcode" : "22", "messages" : "' + notif + '"}'
        except sqlite3.Error as e:
            if (self.__DEBUG):
                toSend = '{"opcode" : "22", "messages" : ""}'
                print ('\nDatabase Error %s:' % e.args[0])
        # Return appropriate msg to send to app
        return toSend

    # When user presses the view messages button, this method
    # gets messages from database and puts them into a string
    # with the appropriate format to return to send to the app.
    def getOnlyMessages(self, username):
        # Retrieve stored messages for user from loginDB, table User_Login_Info 
        mysql = """SELECT """ + """messages FROM User_Login_Info WHERE username = '""" + str(username) +"""'"""
        try:
            myresult = self.__cursor.execute(mysql).fetchall()
            stoveInfo = [dict(i) for i in myresult]
            # Extract messages
            notif = stoveInfo[0].get('messages')
            # Create msg with messages to send to app
            toSend = '{"opcode" : "22", "messages" : "' + notif + '"}'
        except sqlite3.Error as e:
            if (self.__DEBUG):
                toSend = '{"opcode" : "22", "messages" : ""}'
                print ('\nDatabase Error %s:' % e.args[0])
        # Return appropriate msg to send to app
        return toSend

    # When user presses "see who has added you" link, this method gets the usernames from database 
    # that have added currently logged in user as a contact.
    # Puts the usernames into a string with the appropriate format to return to send to the app.
    def getWhoHasAddedUserAsContact(self, username):
        # Retrieve usernames from loginDB, table User_Login_Info 
        try:
            # Added as Regular Contact
            mysql1 = """SELECT """ + """username FROM User_Login_Info WHERE firstContact = '""" + str(username) + """' or secondContact = '""" + str(username) + """' or thirdContact = '""" + str(username) + """'"""
            myresult1 = self.__cursor.execute(mysql1).fetchall()
            stoveInfo1 = [dict(i) for i in myresult1]
            # Extract usernames if any exist
            usernames1 = "_"
            for i in range(len(stoveInfo1)):
                if i == 0:
                    usernames1 = ""
                # Make List of usernames to send back to app
                usernames1 = usernames1 + stoveInfo1[i].get('username') + ","
            
            # Added as Physician Contact
            mysql2 = """SELECT """ + """username FROM User_Login_Info WHERE physicianContact = '""" + str(username) + """'"""
            myresult2 = self.__cursor.execute(mysql2).fetchall()
            stoveInfo2 = [dict(i) for i in myresult2]
            # Extract usernames if any exist
            usernames2 = "_"
            for i in range(len(stoveInfo2)):
                if i == 0:
                    usernames2 = ""
                # Make List of usernames to send back to app
                usernames2 = usernames2 + stoveInfo2[i].get('username') + ","
            # Create msg with usernames to send to app
            toSend = '{"opcode" : "25", "usernames" : "' + usernames1 + '", "physician" : "' + usernames2 + '"}'
        except sqlite3.Error as e:
            if (self.__DEBUG):
                toSend = '{"opcode" : "25", "usernames" : "", "physician" : ""}'
                print ('\nDatabase Error %s:' % e.args[0])
        # Return appropriate msg to send to app
        return toSend


# The main function that continuously receives json packets. 
# Based on opcode received in the json packets, the main method
# invokes other methods; if a message is to be sent back to app
# dbServer.sendAppMsg() is called.
def main():
    DEBUG = True
    # Instantiate a Database server (receive port, port to send info to app, app's IP address, debug mode)
    dbServer = DatabaseServer(1000, 1100,'192.168.137.77', DEBUG)
    ackString = '{"opcode" : "0"}'
    while True:
        # Receive json packets
        data = dbServer.receive()
        if (data ==  None):
            # If nothing receives, keep trying to receive
            continue
        else:
            # Based on opcode received, call appropriate functions and send msg back to app
            if (data.get('opcode') == "1"):
                # When user presses login button
                msg = dbServer.retrieveUserLoginInfo(data.get('username'))
                dbServer.sendAppMsg(msg)
            if (data.get('opcode') == "3"):
                # When user presses register button
                msg = dbServer.register(data.get('username'), data.get('password'), data.get('physician'))
                dbServer.sendAppMsg(msg)
            if (data.get('opcode') == "5"):
                # When user wants to add regular contacts
                msg = dbServer.addRegularContacts(data.get('username'), data.get('contactOne'), data.get('contactTwo'), data.get('contactThree'))
                dbServer.sendAppMsg(msg)
            if (data.get('opcode') == "6"):
                # When user wants to add a physician contact
                msg = dbServer.addPhysicianContact(data.get('username'), data.get('physician'))
                dbServer.sendAppMsg(msg)
            if (data.get('opcode') == "9"):
                # When user wants to view the list of stove analysis tables 
                # get the stove ID of the user and then get the stove analysis tables
                # that associate with the stove ID.
                stoveID = dbServer.retrieveStoveID(data.get('username'))
                msg = dbServer.retrieveVideoList(stoveID)
                dbServer.sendAppMsg(msg)
            if (data.get('opcode') == "11"):
                # When user wants to register or clear a stove
                msg = dbServer.addStoveID(data.get('username'), data.get('stoveID'))
                dbServer.sendAppMsg(msg)
            if (data.get('opcode') == "13"):
                # If user wants to video currently registered stove.
                msg = dbServer.getStoveID(data.get('username'))
                dbServer.sendAppMsg(msg)
            if (data.get('opcode') == "15"):
                # If user wants to view currently registered contacts
                msg = dbServer.getContacts(data.get('username'))
                dbServer.sendAppMsg(msg)
            if (data.get('opcode') == "17"):
                # If user wants to view data pertaining to a stove analysis table
                msg = dbServer.retrieveAnalysisTableData("2", data.get('username'), data.get('tableName'))
                dbServer.sendAppMsg(msg)
            if (data.get('opcode') == "19"):
                # If user or/and contact messages have to be updated
                dbServer.updateUserAndContactMessages(data.get('username'), data.get('UserMessages'), data.get('ContactMessages'))
            if (data.get('opcode') == "20"):
                # If user wants to clear their messages
                dbServer.clearMessages(data.get('username'))
            if (data.get('opcode') == "21"):
                # If user wants to view their messages
                msg = dbServer.getMessages(data.get('username'))
                dbServer.sendAppMsg(msg)
            if (data.get('opcode') == "23"):
                # If user and physician contact messages are to be updated
                dbServer.updateUserAndPhysicianMessages(data.get('username'), data.get('UserMessages'), data.get('PhysicianMessages'))
            if (data.get('opcode') == "24"):
                # To get who has added current user as a contact
                msg = dbServer.getWhoHasAddedUserAsContact(data.get('username'))
                dbServer.sendAppMsg(msg)

    self.__soc_recv.shutdown(1)
    self.__soc_send.shutdown(1)
    self.__cursor.close()
    return
    
if __name__== "__main__":
    main()



