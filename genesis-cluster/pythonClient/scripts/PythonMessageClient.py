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

        port = 4168;
        host = "169.254.203.51"
        #host = "127.0.0.1"
        bc = SimpleClient(host, port)
        bc.startSession()
        # bc.join(name)
        #so = socket.socket()
        #so.connect((host, port))

        fileDir = os.path.dirname(os.path.realpath('__file__'))
        print fileDir
        inputDir = os.path.join(fileDir, '../input/nasa2.jpg')
        inputDir = os.path.abspath(os.path.realpath(inputDir))
        print inputDir
        outputDir = os.path.join(fileDir, '../output/nasa2.jpg')
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
                filename = "nasa2"
                #print("Enter qualified pathname of file to be uploded: ");
                # path = raw_input();
                path = inputDir
                print("Input File size : ")
                #len123 = ca.getSize(path)
                #len123 = os.path.getsize(path)
                #print len123
                #print "Folder = %0.1f MB" % (path / (1024 * 1024.0))
                chunks = bc.chunkFile(path)
                print("Chunks created : ")
                print len(chunks)
                noofchunks = len(chunks)
                print("PMC 80 Calling genChunkInfoMsg")
                chunkid = 0;
                req = bc.genChunkInfoMsg(filename, noofchunks, chunkid);
                print req
                print("PMC 83")
                print("PMC 84 Sending Chunk info")
                bc.sendData(req, host, port)
                chunkid = 1;
                # Create & send chunk info

                for chunk in chunks:
                    print("PMC 87")
                    req = bc.genChunkedMsg(filename, chunk, noofchunks, chunkid);
                    print("PMC 89")
                    chunkid += 1
                    print("PMC 91")
                    print chunkid
                    bc.sendData(req, host, port)
                    print("PMC 93")

                forever = False

            elif choice1 == "2":
                # PUT
                print("Puting the file: ")

                print("Enter the name of your file: ")

                # filename = raw_input();
                print("PUT")
                filename = "nasa2"
                # print("Enter qualified pathname of file to be uploded: ");
                # path = raw_input();
                path = inputDir
                print path
                print("PMC 115 ")
                chunks = bc.chunkFile(path)
                print("PMC 115 Chunking called")
                noofchunks = len(chunks)
                print noofchunks
                print("PMC 120 Calling genChunkInfoMsg")
                chunkid = 0;
                req = bc.genChunkInfoPutMsg(filename, noofchunks, chunkid);
                print req
                print("PMC 124")
                print("PMC 125 Sending Chunk info")
                bc.sendData(req, host, port)
                print("PMC 127")

                chunkid = 1;
                for chunk in chunks:
                    print("PMC 130")
                    req = bc.genChunkedPutMsg(filename, chunk, noofchunks, chunkid);
                    print("PMC 132")
                    print chunkid
                    chunkid += 1
                    bc.sendData(req, host, port)
                    print("PMC 136")
                forever = False
                print("PMC 138")

            elif choice1 == "3":
                # GET
                print("PMC Get the file: ")
                name = "nasa2"
                print name
                path = outputDir
                print path
                req = bc.getFile(name)
                print req
                my_array = bc.sendDataGet(req, host, port)
                print("PMC 98 Printing the results\n")
                '''r = pipe_pb2.CommandMessage();
                #while cnt >= 0:
                r = pipe_pb2.CommandMessage();
                for r in my_array:
                    #print my_array
                    print("PMC result.resMsg.key: ")
                    print r.resMsg.key
                    print("PMC name: ")
                    print name
                    noofchuncks = 0
                    if (r.resMsg.key == name):
                        noofchuncks = r.resMsg.chunkInfo.no_of_chunks
                        print noofchuncks

                    print("PMC 104 Printing no of chumks")

                #noofchuncks = 2
                #while noofchuncks != 0:
                    print("PMC 108 Printing the key")
                    print r.resMsg.key
                    print("PMC 110 Printing the name")
                    print name
                    if (r.resMsg.key == name):
                        with open(path, "w") as outfile:
                            outfile.write(r.resMsg.data)
                    print("PMC 171")
                    noofchuncks -= 1
                '''
                forever = False

                # TODO write the file
                #continue

            elif choice1 == "4":
                # DELETE
                print("Delete the file: ")
                name = "nasa2"
                print name
                req = bc.deleteFile(name)
                print req
                bc.sendData(req, host, port)
                print("PMC 179")
                forever = False
                # TODO write the file
                #continue

            elif choice1 == "5":
                # PING
                print("Ping operation start")
                req = bc.genPing()
                print req
                print("PMC 189")
                bc.sendData(req, host, port)
                print("PMC 191")
                print("Ping operation end")
                forever = False
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

    def getSize(fileobject):
        fileobject.seek(0, 2)  # move the cursor to the end of the file
        size = fileobject.tell()
        return size

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
