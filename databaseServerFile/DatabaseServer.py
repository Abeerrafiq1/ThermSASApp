import json
import time
import socket
import sys
import sqlite3
from datetime import datetime, date

class DatabaseServer:
    def __init__(self, portReceive, appSendport, app_ip_addrs, debug):
        self.__port = int(portReceive)
        self.__DEBUG = debug
        self.__soc_recv = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        recv_address = ('', self.__port)
        self.__soc_recv.bind(recv_address)
        self.__soc_send =  socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        self.__app_addrs = (app_ip_addrs, appSendport)     

        dbpath = 'C:/Users/amanp/Documents/SYSC 4907/loginDB.db'
        self.__dbconnect = sqlite3.connect(dbpath); 
        self.__dbconnect.row_factory = sqlite3.Row;
        self.__cursor = self.__dbconnect.cursor()    


        dbpath_cooking = 'C:/Users/amanp/Documents/SYSC 4907/ThermalCookingDB.db'
        self.__dbconnectCooking = sqlite3.connect(dbpath_cooking); 
        self.__dbconnectCooking.row_factory = sqlite3.Row;
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

    def send_App_Msg(self, strToString):   
        self.__soc_send.sendto(strToString.encode('utf-8'), self.__app_addrs)  
        return
     
    def retrieveStoveNumber(self, enteredUsername):
        mysql = """SELECT """ + """stoveID FROM User_Login_Info WHERE username = '""" + str(enteredUsername) +"""'"""
        try:
            myresult = self.__cursor.execute(mysql).fetchall()
            stoveInfo = [dict(i) for i in myresult]
            stoveID = stoveInfo[0].get('stoveID')
        except (sqlite3.Error, e):
            if (self.__DEBUG):
                print ('\nDatabase Error %s:' % e.args[0])
        print(stoveID)  
        return stoveID  

    def retrieveVideoList(self, stoveID):
        mysql = """SELECT """ + """id, tb_nm FROM videos"""
        try:
            myresult = self.__cursorCooking.execute(mysql).fetchall()
            list = [dict(i) for i in myresult]
        except (sqlite3.Error, e):
            if (self.__DEBUG):
                print ('\nDatabase Error %s:' % e.args[0])
        toSend = '{"opcode" : "10", "videoList" : "' + str(list) +'"}'
        print(toSend)
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
        except (sqlite3.Error, e):
            if (self.__DEBUG):
                print ('\nDatabase Error %s:' % e.args[0])
            toSend = '{"opcode" : "2", "password" : "", "notifications" : "''"}' 
        print(toSend)  
        return toSend  




    def usernameExists(self, username, defaultValue):
        mysql = "SELECT COUNT(*) FROM User_Login_Info WHERE username = '" + username + "'"
        try:
            myresult = self.__cursor.execute(mysql).fetchall()
            count = [dict(i) for i in myresult]
            userCount = count[0].get('COUNT(*)')
        except (sqlite3.Error, e):
            if (self.__DEBUG):
                print ('\nDatabase Error %s:' % e.args[0])
                userCount = defaultValue
        return userCount 
        
    def register(self, username, password, isPhysician):
        if (self.usernameExists(username, 1) != 0):
            toSend = '{"opcode" : "4", "valid" : "no"}'
        else:
            if (isPhysician == True):
                mysql = "INSERT INTO User_Login_Info VALUES ('" + username + "', '" + password + "', '', '', '', '', '', '', 'Yes')"                    
            else:
                mysql = "INSERT INTO User_Login_Info VALUES ('" + username + "', '" + password + "', '', '', '', '', '', '', 'No')"    
            try:
                self.__cursor.execute(mysql)
                self.__dbconnect.commit();
                toSend = '{"opcode" : "4", "valid" : "yes"}'
            except (sqlite3.Error, e):
                if (self.__DEBUG):
                    print ('\nDatabase Error %s:' % e.args[0])
                    toSend = '{"opcode" : "4", "valid" : "no"}'
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
                self.__dbconnect.commit();
            except (sqlite3.Error, e):
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
        except (sqlite3.Error, e):
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
        print(toSend)
        if (validity == 4 or validity == 5):
            mysql = "UPDATE User_Login_Info SET physicianContact = '" + physician + "' WHERE username = '" + currentUser + "'"   
            try:
                self.__cursor.execute(mysql)
                self.__dbconnect.commit();
            except (sqlite3.Error, e):
                if (self.__DEBUG):
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
                dbServer.send_App_Msg(msg)
            if (data.get('opcode') == "3"):
                msg = dbServer.register(data.get('username'), data.get('password'), data.get('physician'))
                dbServer.send_App_Msg(msg)
            if (data.get('opcode') == "5"):
                msg = dbServer.addRegularSubscribers(data.get('currentUser'), data.get('contactOne'), data.get('contactTwo'), data.get('contactThree'))
                dbServer.send_App_Msg(msg)
            if (data.get('opcode') == "6"):
                msg = dbServer.addPhysicianSubscriber(data.get('currentUser'), data.get('physician'))
                dbServer.send_App_Msg(msg)
            if (data.get('opcode') == "9"):
                stoveID = dbServer.retrieveStoveNumber(data.get('username'))
                msg = dbServer.retrieveVideoList(stoveID)
                dbServer.send_App_Msg(msg)
    self.__soc_recv.shutdown(1)
    self.__soc_send.shutdown(1)
    self.__cursor.close()
    return
    
if __name__== "__main__":
    main()
