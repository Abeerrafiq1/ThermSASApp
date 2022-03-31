# Author: Abeer Rafiq
# Purpose of this class: This class facilitates communication between the app and the database
# to constantly send a user's notifications/messages so the app can poll user messages
# If there is a stove risk for another member added to the user's notification, 
# then the app can look for this every couple of seconds.
# If this stove risk is found, app can show pop ups and phone notifications.

# Import required packages
import json
import time
import socket
import sqlite3

# Create MessageRetriever class.
class MessageRetriever:
    # Constructor for Message Retriever
    def __init__(self, portReceive, appSendport, app_ip_addrs, debug):
        # To show print statements on console
        self.__DEBUG = debug
        # Set port and socke
        # To receive requests from app
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
        self.__ack_endTime = 5
        # Set number of times to resend a packet
        self.__numRetries = 5
        # Set up connection to loginDB database
        dbpath = 'C:/Users/amanp/Documents/SYSC 4907/loginDB.db'
        self.__dbconnect = sqlite3.connect(dbpath); 
        self.__dbconnect.row_factory = sqlite3.Row
        self.__cursor = self.__dbconnect.cursor()      
        # Show initialization msg if debug
        if (self.__DEBUG):
            print("\nMessageRetriever Initialized")

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
        if  (self.__DEBUG) : 
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

# The main function that continuously receives requests for user 
# messages as json packets. It retrieves user messages when requested
# and sends them back to the app.
def main():
    DEBUG = True
    # Instantiate a MessageRetriever (receive port, port to send info to app, app's IP address, debug mode)
    dbServer = MessageRetriever(2000, 2100,'192.168.137.77', DEBUG)
    while True:
        # Receive json packets
        data = dbServer.receive()
        if (data ==  None):
            # If nothing receives, keep trying to receive
            continue
        else:
            # Retrieve user messages if opcode = 1 and send the string with messages to app
            if (data.get('opcode') == "1"):
                # When user presses login button
                msg = dbServer.getMessages(data.get('username'))
                dbServer.sendAppMsg(msg)        

    self.__soc_recv.shutdown(1)
    self.__soc_send.shutdown(1)
    self.__cursor.close()
    return
    
if __name__== "__main__":
    main()
