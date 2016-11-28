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

        #port = 4168;
        #host = "169.254.203.51"
        #host = "169.254.203.33"
        bc = SimpleClient(host, port)
        bc.startSession()
        # bc.join(name)
        #so = socket.socket()
        #so.connect((host, port))

        fileDir = os.path.dirname(os.path.realpath('__file__'))
        print fileDir
        inputDir = os.path.join(fileDir, '../input/')
        inputDir = os.path.abspath(os.path.realpath(inputDir))
        print inputDir
        outputDir = os.path.join(fileDir, '../output/')
        outputDir = os.path.abspath(os.path.realpath(outputDir))
        print outputDir


        #choice1 = raw_input("Please enter the choice.\n")

        forever = True;
        while (forever):
            print("\n");
            print("\n");
            print("------------------------------------------ \n");
            print("Menu: \n");
            print("1.) POST \n");
            print("2.) PUT \n");
            print("3.) GET \n");
            print("4.) DELETE \n");
            print("5.) PING \n");
            print("6.) exit - end session\n");
            print("\n")
            choice1 = raw_input("Please enter the choice.\n")
            if (choice1 == None):
                continue;
            elif choice1 == "6":
                print("Bye")
                bc.stopSession()
                forever = False
            elif choice1 == "1":
                # POST
                print("Performing POST operation!!!")
                print("Enter the complete name of your file: ")
                filenameComplete = raw_input();
                path = os.path.join(inputDir, filenameComplete)
                print path
                print("Enter the name of your file: ")
                filename = raw_input();
                print filename
                #filename = "nasa2"
                #print("Enter qualified pathname of file to be uploded: ");
                # path = raw_input();
                #path = inputDir
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
                #chunkid = 0;
                #req = bc.genChunkInfoMsg(filename, noofchunks, chunkid);
                #print req
                print("PMC 83")
                print("PMC 84 Sending Chunk info")
                #bc.sendData(req, host, port)
                chunkid = 0;
                # Create & send chunk info

                for chunk in chunks:
                    print("PMC 87")
                    print chunkid
                    req = bc.genChunkedMsg(filename, chunk, noofchunks, chunkid);
                    print("PMC 89")
                    chunkid += 1
                    print("PMC 91")

                    bc.sendData(req, host, port)
                    print("PMC 93")

                #forever = False

            elif choice1 == "2":
                # PUT
                print("Performing PUT operation!!!")
                print("Enter the complete name of the file: ")
                filenameComplete = raw_input();
                path = os.path.join(inputDir, filenameComplete)
                print path
                print("Enter the name of your file: ")
                filename = raw_input();
                print filename
                # print("Enter qualified pathname of file to be uploded: ");
                # path = raw_input();
                #path = inputDir
                print path
                print("PMC 115 ")
                chunks = bc.chunkFile(path)
                print("PMC 115 Chunking called")
                noofchunks = len(chunks)
                print noofchunks
                print("PMC 120 Calling genChunkInfoMsg")
                #chunkid = 0;
                #req = bc.genChunkInfoPutMsg(filename, noofchunks, chunkid);
                #print req
                print("PMC 124")
                print("PMC 125 Sending Chunk info")
                #bc.sendData(req, host, port)
                print("PMC 127")

                chunkid = 0;
                for chunk in chunks:
                    print("PMC 130")
                    req = bc.genChunkedPutMsg(filename, chunk, noofchunks, chunkid);
                    print("PMC 132")
                    print chunkid
                    chunkid += 1
                    bc.sendData(req, host, port)
                    print("PMC 136")
                #forever = False
                print("PMC 138")

            elif choice1 == "3":
                # GET
                print("Performing GET operation!!!")
                print("Enter the complete name of the file: ")
                filenameComplete = raw_input();
                path = os.path.join(outputDir, filenameComplete)
                print path
                print("Enter the name of your file: ")
                filename = raw_input();
                print filename


                print("PMC Get the file: ")
                name = filename
                print name
                path = outputDir
                print path
                req = bc.getFile(name)
                print req
                my_array = bc.sendDataGet(req, host, port, path)
                print("PMC 98 Printing the results\n")
                #forever = False

                # TODO write the file
                #continue

            elif choice1 == "4":
                # DELETE
                print("Performing DELETE operation!!!")
                print("Enter the complete name of the file: ")
                filenameComplete = raw_input();
                #path = os.path.join(outputDir, filenameComplete)
                #print path
                print("Enter the name of your file: ")
                filename = raw_input();
                print filename
                name = filename
                print name
                req = bc.deleteFile(name)
                print req
                bc.sendData(req, host, port)
                print("PMC 179")
                #forever = False
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
                #forever = False
            else:
                print("Wrong Selection");
        print("\nGoodbye\n");



if __name__ == '__main__':

    #str(sys.argv)
    #host = "127.0.0.1"
    print("HOST")
    print sys.argv[1]
    print("PORT")
    print sys.argv[2]
    host = str(sys.argv[1])
    print host
    port = int(str(sys.argv[2]))
    print port
    ca = PythonMessageClient();
    ca.run();
