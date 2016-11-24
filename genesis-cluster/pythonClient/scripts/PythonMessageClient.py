import common_pb2
import socket               
import time
import struct
import pipe_pb2
import shlex
import subprocess
import sys
import clientMessage_pb2
import cPickle
import sys
import os
from SimpleClient import SimpleClient



class PythonMessageClient:
    def run(self):
        print("Enter the IP of Server with which you want to connect: ");
        # host = raw_input();
        print("Enter the port of Server: ");
        # port = int(raw_input());

        port = 4668;
        #host = "169.254.203.33"
        host = "127.0.0.1"
        bc = SimpleClient(host, port)
        bc.startSession()
        # bc.join(name)
        #so = socket.socket()
        #so.connect((host, port))

        fileDir = os.path.dirname(os.path.realpath('__file__'))
        print fileDir
        inputDir = os.path.join(fileDir, '../input/nasa1.jpg')
        inputDir = os.path.abspath(os.path.realpath(inputDir))
        print inputDir
        outputDir = os.path.join(fileDir, '../output/nasa1.jpg')
        outputDir = os.path.abspath(os.path.realpath(outputDir))
        print outputDir

        print("Menu: \n");
        print("1.) POST \n");
        print("2.) PUT \n");
        print("3.) GET \n");
        print("4.) DELETE \n");
        print("5.) PING \n");
        print("6.) exit - end session\n");
        print("\n")
        choice1 = raw_input("Please enter the choice.\n")

        forever = True;
        while (forever):

            if (choice1 == None):
                continue;
            elif choice1 == "6":
                print("Bye")
                bc.stopSession()
                forever = False
            elif choice1 == "1":
                # POST
                print("Enter the name of your file: ")

                # filename = raw_input();
                print("SEND")
                filename = "nasa1"
                #print("Enter qualified pathname of file to be uploded: ");
                # path = raw_input();
                path = inputDir
                chunks = bc.chunkFile(path)
                noofchunks = len(chunks)
                chunkid = 0;
                req = bc.genChunkInfoMsg(filename, noofchunks, chunkid);
                result = bc.sendData(req, host, port)
                chunkid = 1;
                # Create & send chunk info

                for chunk in chunks:
                    req = bc.genChunkedMsg(filename, chunk, noofchunks, chunkid);
                    chunkid += 1
                    result = bc.sendData(req, host, port)


            elif choice1 == "2":
                # PUT
                print("Puting the file: ")

                print("Enter the name of your file: ")

                # filename = raw_input();
                print("SEND")
                filename = "nasa1"
                # print("Enter qualified pathname of file to be uploded: ");
                # path = raw_input();
                path = inputDir
                chunks = bc.chunkFile(path)
                noofchunks = len(chunks)

                chunkid = 1;
                for chunk in chunks:
                    req = bc.genChunkedPutMsg(filename, chunk, noofchunks, chunkid);
                    chunkid += 1
                    result = bc.sendData(req, host, port)

            elif choice1 == "3":
                # GET
                print("Get the file: ")
                name = "nasa1"
                print name
                path = outputDir
                print path
                req = bc.getFile(name)
                result = bc.sendData(req, host, port)
                print("98 Printing the results\n")
                print result

                #if (result.resMsg.key == name):
                noofchuncks = result.chunkInfo.noofchuncks

                print("104 Printing no of chumks")
                print noofchuncks

                while noofchuncks != 0:
                    print("108 Printing the key")
                    print result.resMsg.key
                    print("110 Printing the name")
                    print name
                    if (result.resMsg.key == name):
                        with open(path, "w") as outfile:
                            outfile.write(result.resMsg.data)

                # TODO write the file
                continue

            elif choice1 == "4":
                # DELETE
                print("Delete the file: ")
                name = "nasa1"
                path = outputDir
                req = bc.deleteFile(name)
                result = bc.sendData(req, host, port)
                print result

                if (result.resMsg.key == name):
                    noofchuncks = result.chunkInfo.noofchuncks

                    print noofchuncks

                while noofchuncks != 0:

                    if (result.resMsg.key == name):
                        with open(path, "w") as outfile:
                            outfile.write(result.resMsg.data)

                # TODO write the file
                continue

            elif choice1 == "5":
                # PING
                print("Ping operation start")
                bc.genPing()
                print("Ping operation end")
            else:
                print("Wrong Selection");
        print("\nGoodbye\n");


    def request_ping(self):
        print "Start request_ping() executing"

        cm = pipe_pb2.CommandMessage()
        cm.header.node_id=1
        cm.header.time=10000
        cm.header.destination=2
        cm.ping = True
        print "request_ping() executing"

        pingr = cm.SerializeToString()

        packed_len = struct.pack('>L', len(pingr))
        # Sending Ping Request to the server's public port
        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        # host = socket.gethostname() # Testing on own computer
        # port = 5570 # Public Port
        s.connect(("127.0.0.1", 4668))
        # Prepending the length field and sending
        s.sendall(packed_len + pingr)
        s.close()




    def sendMsg(msgStr, msgInt, msg_out, port, host):
        s = socket.socket()
        #    host = socket.gethostname()
        #    host = "192.168.0.87"

        s.connect((host, port))
        msg_len = struct.pack('>L', len(msg_out))

        cm = pipe_pb2.CommandMessage()
        cm.header.node_id = 2
        cm.header.time = 10000
        cm.header.destination = 6
        cm.reqMsg.operation = 'POST'
        cm.reqMsg.key = msgStr
        cm.reqMsg.seq_no = msgInt
        cm.reqMsg.data = msg_out


        s.sendall(cm)
        #len_buf = receiveMsg(s, 4)
        #msg_in_len = struct.unpack('>L', len_buf)[0]
        #msg_in = receiveMsg(s, msg_in_len)

        #cm = pipe_pb2.CommandMessage()
        #cm.ParseFromString(msg_in)
        #    print msg_in
        #    print r.body.job_status
        #    print r.header.reply_msg
        #    print r.body.job_op.data.options
        s.close
        return "OK"


    def receiveMsg(socket, n):
        buf = ''
        while n > 0:
            data = socket.recv(n)
            if data == '':
                raise RuntimeError('data not received!')
            buf += data
            n -= len(data)
        return buf


    def getBroadcastMsg(port):
        # listen for the broadcast from the leader"

        sock = socket.socket(socket.AF_INET,  # Internet
                             socket.SOCK_DGRAM)  # UDP

        sock.bind(('', port))

        data = sock.recv(1024)  # buffer size is 1024 bytes
        return data



if __name__ == '__main__':

    #host = "169.254.203.33"
    host = "127.0.0.1"
    port = 4668
    ca = PythonMessageClient();
    ca.run();
