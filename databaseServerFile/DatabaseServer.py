import json
import time
import socket
import sys
import sqlite3
from datetime import datetime, date

class DatabaseServer:
    def __init__(self, portReceive, appSendport, app_ip_addrs, debug):
        self.__port = int(portReceive)
        nothing = False
        self.__DEBUG = debug
        self.__soc_recv = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        recv_address = ('', self.__port)
        self.__soc_recv.bind(recv_address)
        self.__soc_send =  socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.__app_addrs = (app_ip_addrs, appSendport)     

        dbpath = 'C:/Users/amanp/Documents/SYSC 4907/loginDB.db'
        self.__dbconnect = sqlite3.connect(dbpath); 
        self.__dbconnect.row_factory = sqlite3.Row
        self.__cursor = self.__dbconnect.cursor()    

        self.__ackstr = '{"opcode" : "0"}'
        self.__ack_timeout = 1
        self.__receive_timeout = 10
        self.__ack_endTime = 3

        dbpath_cooking = 'C:/Users/amanp/Documents/SYSC 4907/thermal_cooking.db'
        self.__dbconnectCooking = sqlite3.connect(dbpath_cooking); 
        self.__dbconnectCooking.row_factory = sqlite3.Row
        self.__cursorCooking = self.__dbconnectCooking.cursor()        

        if (self.__DEBUG):
            print("\nDatabaseServer Initialized")

    #Receives/returns buffer and sends ack 
    def receive(self):
        if (self.__DEBUG):
            print("\nWaiting to receive on port %d ... " % self.__port)
        while(1):
            try:
                buf_noload, address = self.__soc_recv.recvfrom(self.__port)
                buf_noload = buf_noload.decode("utf-8")
                buf = json.loads(buf_noload)
                if len(buf) > 0:
                    if (self.__DEBUG):
                        print ("Received %s bytes from '%s': %s " % (len(buf), address[0], buf))
                    return buf
                else:
                    if (self.__DEBUG):
                        print("Nothing was received")
                    return None                
            except (ValueError, KeyError, TypeError):
                if (self.__DEBUG):
                    print("Error in Loading Json String")
                return None
            except socket.timeout:
                if (self.__DEBUG):
                    print("Receiving is Timed Out")
                return None

    def sendAppMsg(self, strToString):   
        self.__soc_send.sendto(strToString.encode('utf-8'), self.__app_addrs)  
        return
     
    def retrieveStoveNumber(self, enteredUsername):
        mysql = """SELECT """ + """stoveID FROM User_Login_Info WHERE username = '""" + str(enteredUsername) +"""'"""
        try:
            myresult = self.__cursor.execute(mysql).fetchall()
            stoveInfo = [dict(i) for i in myresult]
            stoveID = stoveInfo[0].get('stoveID')
        except sqlite3.Error as e:
            if (self.__DEBUG):
                print ('\nDatabase Error %s:' % e.args[0])
        return stoveID  

    def getSubscribers(self, enteredUsername):
        mysql = """SELECT """ + """physicianContact, firstContact, secondContact, thirdContact FROM User_Login_Info WHERE username = '""" + str(enteredUsername) +"""'"""
        try:
            myresult = self.__cursor.execute(mysql).fetchall()
            stoveInfo = [dict(i) for i in myresult]
            physician = stoveInfo[0].get('physicianContact')
            contact1 = stoveInfo[0].get('firstContact')
            contact2 = stoveInfo[0].get('secondContact')
            contact3 = stoveInfo[0].get('thirdContact')
            toSend = '{"opcode" : "16", "physician" : "' + physician + '", "contact1" : "' + contact1 + '", "contact2" : "' + contact2 + '", "contact3" : "' + contact3 + '"}'
        except sqlite3.Error as e:
            if (self.__DEBUG):
                toSend = '{"opcode" : "16", "physician" : "", "contact1" : "", "contact2" : "", "contact3" : ""}'
                print ('\nDatabase Error %s:' % e.args[0])
        return toSend  


    def getStoveNumber(self, enteredUsername):
        stoveNum = self.retrieveStoveNumber(enteredUsername)
        toSend = '{"opcode" : "14", "stoveRegistered" : "' + stoveNum + '"}'
        return toSend  

    def stoveExists(self, stoveID):
        mysql = "SELECT COUNT(*) FROM User_Login_Info WHERE stoveID = '" + stoveID + "'"
        try:
            myresult = self.__cursor.execute(mysql).fetchall()
            count = [dict(i) for i in myresult]
            stoveCount = count[0].get('COUNT(*)')
        except sqlite3.Error as e:
            if (self.__DEBUG):
                stoveCount = 1
                print ('\nDatabase Error %s:' % e.args[0])
        return stoveCount 


    def addStoveNumber(self, currentUser, stoveID):
        if (stoveID == ""):
            toSend = '{"opcode" : "12", "validity" : "empty", "maxStoveID" : ""}'
            mysql = "UPDATE User_Login_Info SET stoveID = '" + stoveID + "' WHERE username = '" + currentUser + "'"
            try:
                self.__cursor.execute(mysql)
                self.__dbconnect.commit()
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    print ('\nDatabase Error %s:' % e.args[0])
        elif (self.stoveExists(stoveID) != 0):
            mysql = "SELECT stoveID FROM  User_Login_Info where stoveID <> '' ORDER BY stoveID DESC LIMIT 1"
            try:
                myresult = self.__cursor.execute(mysql).fetchall()
                maxStove = [dict(i) for i in myresult]
                maxStoveNum = maxStove[0].get('stoveID')
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    maxStoveNum = 1
                    print ('\nDatabase Error %s:' % e.args[0])
                    toSend = '{"opcode" : "12", "validity" : "no", "maxStoveID" : ""}'
            toSend = '{"opcode" : "12", "validity" : "no", "maxStoveID" : "' + maxStoveNum + '"}'
        else:
            toSend = '{"opcode" : "12", "validity" : "yes", "maxStoveID" : ""}'
            mysql = "UPDATE User_Login_Info SET stoveID = '" + stoveID + "' WHERE username = '" + currentUser + "'"
            try:
                self.__cursor.execute(mysql)
                self.__dbconnect.commit()
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    print ('\nDatabase Error %s:' % e.args[0])
        return toSend  

    def retrieveAnalysisTableData(self, stoveID, username, tableName):
        if (stoveID == ""):
            toSend = '{"opcode" : "18", "data" : "''"}'
        else:
            try:
                mysql = """SELECT """ + """time_elapsed, pan_temp, pan_area, num_food, food_temp, food_area, classification FROM """ + tableName
                myresult = self.__cursorCooking.execute(mysql).fetchall()
                list = [dict(i) for i in myresult]
                toSend = '{"opcode" : "18", "data" : "' + str(list) +'"}'
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    toSend = '{"opcode" : "10", "data" : "''"}'
                    print ('\nDatabase Error %s:' % e.args[0])
        return toSend  

    def retrieveVideoList(self, stoveID):
        if (stoveID == ""):
            toSend = '{"opcode" : "10", "videoList" : "''"}'
        else:
            try:
                mysql = """SELECT """ + """id, analysis_table_name FROM videos WHERE stoveId = """ + str(stoveID) 
                myresult = self.__cursorCooking.execute(mysql).fetchall()
                list = [dict(i) for i in myresult]
                toSend = '{"opcode" : "10", "videoList" : "' + str(list) +'"}'
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    toSend = '{"opcode" : "10", "videoList" : "''"}'
                    print ('\nDatabase Error %s:' % e.args[0])
        return toSend 

    def retrieveUserLoginInfo(self, enteredUsername):
        try:
            mysql = """SELECT """ + """password FROM User_Login_Info WHERE username = '""" + str(enteredUsername) +"""'"""
            myresult = self.__cursor.execute(mysql).fetchall()
            password = [dict(i) for i in myresult]
            
            mysql = """SELECT """ + """notifications FROM User_Login_Info WHERE username = '""" + str(enteredUsername) +"""'"""
            myresult = self.__cursor.execute(mysql).fetchall()
            notification = [dict(i) for i in myresult]

            if (len(password) > 0):
                DBpassword = password[0].get('password')
                notifications = notification[0].get('notifications')
                toSend = '{"opcode" : "2", "password" : "' + str(DBpassword) + '", "notifications" : "' + str(notifications) + '"}'              
            else:
                toSend = '{"opcode" : "2", "password" : "", "notifications" : "''"}' 
        except sqlite3.Error as e:
            if (self.__DEBUG):
                toSend = '{"opcode" : "2", "password" : "", "notifications" : "''"}'
                print ('\nDatabase Error %s:' % e.args[0])
        return toSend  

    def usernameExists(self, username, defaultValue):
        mysql = "SELECT COUNT(*) FROM User_Login_Info WHERE username = '" + username + "'"
        try:
            myresult = self.__cursor.execute(mysql).fetchall()
            count = [dict(i) for i in myresult]
            userCount = count[0].get('COUNT(*)')
        except sqlite3.Error as e:
            if (self.__DEBUG):
                userCount = defaultValue
                print ('\nDatabase Error %s:' % e.args[0])
        return userCount 
        
    def register(self, username, password, isPhysician):
        if (self.usernameExists(username, 1) != 0):
            toSend = '{"opcode" : "4", "valid" : "no"}'
        else:
            if (isPhysician == True):
                mysql = "INSERT INTO User_Login_Info VALUES ('" + username + "', '" + password + "', '', '', '', '', '', '-----------------------------------------', 'Yes')"                    
            else:
                mysql = "INSERT INTO User_Login_Info VALUES ('" + username + "', '" + password + "', '', '', '', '', '', '-----------------------------------------', 'No')"    
            try:
                self.__cursor.execute(mysql)
                self.__dbconnect.commit()
                toSend = '{"opcode" : "4", "valid" : "yes"}'
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    toSend = '{"opcode" : "4", "valid" : "no"}'
                    print ('\nDatabase Error %s:' % e.args[0])
        return toSend    
        
    def addRegularSubscribers(self, currentUser, contactOne, contactTwo, contactThree):
        if (contactOne != ''):
            if (self.usernameExists(contactOne, 0) != 1):
                one = 1
            elif (contactOne == currentUser):
                one = 2
            elif (contactOne == contactTwo) or (contactOne == contactThree):
                one = 3
            else:
                one = 4
        else:
            one = 4
        if (contactTwo != ''):
            if (self.usernameExists(contactTwo, 0) != 1):
                two = 1
            elif (contactTwo == currentUser):
                two = 2
            elif (contactTwo == contactOne) or (contactTwo == contactThree):
                two = 3
            else:
                two = 4
        else:
            two = 4
        if (contactThree != ''):
            if (self.usernameExists(contactThree, 0) != 1):
                three = 1
            elif (contactThree == currentUser):
                three = 2
            elif (contactThree == contactOne) or (contactThree == contactTwo):
                three = 3
            else:
                three = 4
        else:
            three = 4
        if (one == 4 and two == 4 and three == 4):
            mysql = "UPDATE User_Login_Info SET firstContact = '" + contactOne + "', secondContact = '" + contactTwo + "', thirdContact = '" + contactThree + "' WHERE username = '" + currentUser + "'"   
            try:
                self.__cursor.execute(mysql)
                self.__dbconnect.commit()
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    print ('\nDatabase Error %s:' % e.args[0])
        if (contactThree == '' and contactTwo == '' and contactOne == ''):
            one = 5
            two = 5
            three = 5
        toSend = "{'opcode' : '7', 'contactOne' : '" + str(one) + "', 'contactTwo' : '" + str(two) + "', 'contactThree' : '" + str(three) + "'}"
        return toSend 
        
    def checkIfPhysician(self, physician):
        try:
            mysql = """SELECT """ + """isPhysician FROM User_Login_Info WHERE username = '""" + str(physician) +"""'"""
            myresult = self.__cursor.execute(mysql).fetchall()
            result = [dict(i) for i in myresult]
            isPhysician = result[0].get('isPhysician')
        except sqlite3.Error as e:
            if (self.__DEBUG):
                print ('\nDatabase Error %s:' % e.args[0])
        return isPhysician 
        
    def addPhysicianSubscriber(self, currentUser, physician):
        if (physician != ''):
            if(self.usernameExists(physician, 0) != 1):
                validity = 1
            elif (self.checkIfPhysician(physician) == 'No'):
                validity = 2
            elif (physician == currentUser):
                validity = 3
            else:
                validity = 4
        else:
            validity = 5
        toSend = '{"opcode" : "8", "physician" : "' + str(validity) + '"}'
        if (validity == 4 or validity == 5):
            mysql = "UPDATE User_Login_Info SET physicianContact = '" + physician + "' WHERE username = '" + currentUser + "'"   
            try:
                self.__cursor.execute(mysql)
                self.__dbconnect.commit()
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    print ('\nDatabase Error %s:' % e.args[0])
        return toSend    

    def setNotifications(self, username, Notifications):
        mysql = "UPDATE User_Login_Info SET notifications = '" + Notifications + "' WHERE username = '" + username + "'"
        try:
            self.__cursor.execute(mysql)
            self.__dbconnect.commit()
        except sqlite3.Error as e:
            if (self.__DEBUG):
                print ('\nDatabase Error %s:' % e.args[0])
    
    def getAndSetNotification(self, username, ContactNotifications):
        mysql = """SELECT """ + """notifications FROM User_Login_Info WHERE username = '""" + str(username) +"""'"""
        try:
            myresult = self.__cursor.execute(mysql).fetchall()
            stoveInfo = [dict(i) for i in myresult]
            notif = stoveInfo[0].get('notifications')
        except sqlite3.Error as e:
            if (self.__DEBUG):
                print ('\nDatabase Error %s:' % e.args[0])
        newNotifications = notif + ContactNotifications
        self.setNotifications(username, newNotifications)
   
    def updateContactNotifications(self, username, UserNotifications, ContactNotifications):
        self.getAndSetNotification(username, UserNotifications)
        if (ContactNotifications != ""):
            mysql = """SELECT """ + """firstContact, secondContact, thirdContact FROM User_Login_Info WHERE username = '""" + str(username) +"""'"""
            try:
                myresult = self.__cursor.execute(mysql).fetchall()
                stoveInfo = [dict(i) for i in myresult]
                contact1 = stoveInfo[0].get('firstContact')
                contact2 = stoveInfo[0].get('secondContact')
                contact3 = stoveInfo[0].get('thirdContact')
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    print ('\nDatabase Error %s:' % e.args[0])
            if (contact1 != ""):
                self.getAndSetNotification(contact1, ContactNotifications)
            if (contact2 != ""):
                self.getAndSetNotification(contact2, ContactNotifications)
            if (contact3 != ""):
                self.getAndSetNotification(contact3, ContactNotifications)

   
    def updatePhysicianNotifications(self, username, UserNotifications, PhysicianNotifications):
        self.getAndSetNotification(username, UserNotifications)
        if (PhysicianNotifications != ""):
            mysql = """SELECT """ + """physicianContact FROM User_Login_Info WHERE username = '""" + str(username) +"""'"""
            try:
                myresult = self.__cursor.execute(mysql).fetchall()
                stoveInfo = [dict(i) for i in myresult]
                physicianContact = stoveInfo[0].get('physicianContact')
            except sqlite3.Error as e:
                if (self.__DEBUG):
                    print ('\nDatabase Error %s:' % e.args[0])
            if (physicianContact != ""):
                self.getAndSetNotification(physicianContact, PhysicianNotifications)


    def clearNotifications(self, username):
        mysql = "UPDATE User_Login_Info SET notifications = '-----------------------------------------' WHERE username = '" + username + "'"
        try:
            self.__cursor.execute(mysql)
            self.__dbconnect.commit()
        except sqlite3.Error as e:
            if (self.__DEBUG):
                print ('\nDatabase Error %s:' % e.args[0])

    def getNotifications(self, username):
        mysql = """SELECT """ + """notifications FROM User_Login_Info WHERE username = '""" + str(username) +"""'"""
        try:
            myresult = self.__cursor.execute(mysql).fetchall()
            stoveInfo = [dict(i) for i in myresult]
            notif = stoveInfo[0].get('notifications')
            toSend = '{"opcode" : "22", "notifications" : "' + notif + '"}'
        except sqlite3.Error as e:
            if (self.__DEBUG):
                toSend = '{"opcode" : "22", "notifications" : ""}'
                print ('\nDatabase Error %s:' % e.args[0])
        return toSend

def main():
    DEBUG = True
    dbServer = DatabaseServer(1000, 1100,'192.168.137.77', DEBUG)
    while True:
        data = dbServer.receive()
        if (data ==  None):
            continue
        else:
            if (data.get('opcode') == "1"):
                msg = dbServer.retrieveUserLoginInfo(data.get('username'))
                dbServer. sendAppMsg(msg)
            if (data.get('opcode') == "3"):
                msg = dbServer.register(data.get('username'), data.get('password'), data.get('physician'))
                dbServer. sendAppMsg(msg)
            if (data.get('opcode') == "5"):
                msg = dbServer.addRegularSubscribers(data.get('currentUser'), data.get('contactOne'), data.get('contactTwo'), data.get('contactThree'))
                dbServer. sendAppMsg(msg)
            if (data.get('opcode') == "6"):
                msg = dbServer.addPhysicianSubscriber(data.get('currentUser'), data.get('physician'))
                dbServer. sendAppMsg(msg)
            if (data.get('opcode') == "9"):
                stoveID = dbServer.retrieveStoveNumber(data.get('username'))
                msg = dbServer.retrieveVideoList(stoveID)
                dbServer. sendAppMsg(msg)
            if (data.get('opcode') == "11"):
                msg = dbServer.addStoveNumber(data.get('currentUser'), data.get('stoveID'))
                dbServer. sendAppMsg(msg)
            if (data.get('opcode') == "13"):
                msg = dbServer.getStoveNumber(data.get('currentUser'))
                dbServer. sendAppMsg(msg)
            if (data.get('opcode') == "15"):
                msg = dbServer.getSubscribers(data.get('currentUser'))
                dbServer. sendAppMsg(msg)
            if (data.get('opcode') == "17"):
                msg = dbServer.retrieveAnalysisTableData("2", data.get('username'), data.get('tableName'))
                dbServer. sendAppMsg(msg)
            if (data.get('opcode') == "19"):
                dbServer.updateContactNotifications(data.get('username'), data.get('UserNotifications'), data.get('ContactNotifications'))
            if (data.get('opcode') == "20"):
                dbServer.clearNotifications(data.get('username'))
            if (data.get('opcode') == "21"):
                msg = dbServer.getNotifications(data.get('username'))
                dbServer. sendAppMsg(msg)
            if (data.get('opcode') == "23"):
                dbServer.updatePhysicianNotifications(data.get('username'), data.get('UserNotifications'), data.get('PhysicianNotifications'))

    self.__soc_recv.shutdown(1)
    self.__soc_send.shutdown(1)
    self.__cursor.close()
    return
    
if __name__== "__main__":
    main()
