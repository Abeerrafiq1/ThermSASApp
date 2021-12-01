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

        dbpath = 'C:/Users/amanp/OneDrive/Desktop/SYSC 4907/loginDB.db'
        self.__dbconnect = sqlite3.connect(dbpath); 
        self.__dbconnect.row_factory = sqlite3.Row;
        self.__cursor = self.__dbconnect.cursor()        

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

    def retrieveUserLoginInfo(self, enteredUsername):
        try:
            mysql = """SELECT """ + """password FROM User_Login_Info WHERE username = '""" + str(enteredUsername) +"""'"""
            myresult = self.__cursor.execute(mysql).fetchall()
            password = [dict(i) for i in myresult]
            if (len(password) > 0):
                DBpassword = password[0].get('password')
                toSend = '{"opcode" : "2", "password" : "' + str(DBpassword) + '"}'
            else:
                toSend = '{"opcode" : "2", "password" : ""}'
        except (sqlite3.Error, e):
            if (self.__DEBUG):
                print ('\nDatabase Error %s:' % e.args[0])
            toSend = '{"opcode" : "2", "password" : ""}'
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
    self.__soc_recv.shutdown(1)
    self.__soc_send.shutdown(1)
    self.__cursor.close()
    return
    
if __name__== "__main__":
    main()
