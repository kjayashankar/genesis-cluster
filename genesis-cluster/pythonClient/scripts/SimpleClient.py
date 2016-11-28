import socket
import common_pb2
import pipe_pb2
import pbd
from protobuf_to_dict import protobuf_to_dict
from asyncore import read
import struct
import clientMessage_pb2
import time
import sys
import os
import time
from array import *
from datetime import datetime

class SimpleClient:
    def __init__(self,host,port):
        self.host = host
        self.port = port
        self.sd  = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    def setName(self, name):
        self.name = name

    def getName(self):
        return self.name

    def startSession(self):
        #print("Value of IP   "+self.host)
        #print("Value of port   "+self.port)
        #self.sd.connect((self.host, self.port))
        #self.sd.connect(("169.254.203.33", 4168))
        self.sd.connect((self.host, self.port))
        print("Connected to Host:", self.host, "@ Port:", self.port)


    def stopSession(self):
        #builder = MessageBuilder()
        #msg = builder.encode(MessageType.leave, self.name,'', '')
        #print(msg)
        #self.sd.send(msg)
        self.sd.close()
        self.sd = None


    def getFile(self,name):
        print("SC 45")
        cm = pipe_pb2.CommandMessage()
        print("SC 47")
        cm.header.node_id = 6
        cm.header.origin.id = 6
        cm.header.origin.host = self.host
        cm.header.origin.port = self.port
        cm.header.time = 10000
        #cm.message = name
        clmsg2 = clientMessage_pb2.Operation.Value("GET")
        # b = self.chunkFile()

        #cm.reqMsg.data = filecontent
        cm.reqMsg.key = name
        #cm.reqMsg.seq_no = msgInt
        cm.reqMsg.operation = clmsg2
        # cm.reqMsg = clmsg
        # cm.reqMsg.Value(clmsg)
        print cm
        var = cm.SerializeToString()
        print("SC 63")

        return var

    def deleteFile(self,name):
        cm = pipe_pb2.CommandMessage()
        cm.header.node_id = 6
        cm.header.origin.id = 6
        cm.header.origin.host = self.host
        cm.header.origin.port = self.port
        cm.header.time = 10000
        #cm.message = name
        clmsg2 = clientMessage_pb2.Operation.Value("DELETE")
        # b = self.chunkFile()

        #cm.reqMsg.data = filecontent
        cm.reqMsg.key = name
        #cm.reqMsg.seq_no = msgInt
        cm.reqMsg.operation = clmsg2
        # cm.reqMsg = clmsg
        # cm.reqMsg.Value(clmsg)
        var = cm.SerializeToString()

        return var

    def chunkFile(self,file):
        fileChunk = []
        print("SC 87 Chunk Input File size : ")
        #len123 = os.path.getsize(file)
        #print len123
        with open(file, "rb") as fileContent:
            data = fileContent.read(1024*1024)
            print("SC 91 Chunk size : ")
            #len123 = os.path.getsize(data)
            #print len123
            while data:
                fileChunk.append(data)
                data = fileContent.read(1024*1024)
                print("SC 96 Chunk size : ")
                #len123 = os.path.getsize(data)
                #print len123
        return fileChunk

    def getSize(fileobject):
        fileobject.seek(0, 2)  # move the cursor to the end of the file
        size = fileobject.tell()
        return size

    def sendMessage(self, message):
        if len(message) > 1024:
            print('message exceeds 1024 size')

        print('something')
        #builder = MessageBuilder()
        #msg = builder.encode(MessageType.msg, self.name, message, '')
        #self.sd.send(msg)



    def genPing(self):
        print "Start request_ping() executing"
        cm = pipe_pb2.CommandMessage()
        cm.header.node_id = 6
        cm.header.origin.id = 1
        cm.header.origin.host = self.host
        cm.header.origin.port = self.port
        cm.header.time = 10000
        cm.header.destination = 6
        cm.ping = True
        print "request_ping() executing"
        print cm
        pingr = cm.SerializeToString()
        print "SC 133"
        #packed_len = struct.pack('>L', len(pingr))
        return pingr;

    def genChunkInfoMsg(self,filename, noofchunks,chunkid):

        print("SC 144 noofchunks")
        print noofchunks
        current_time = datetime.now().time()
        cm = pipe_pb2.CommandMessage()
        cm.header.node_id = 6
        cm.header.origin.id = 1
        cm.header.origin.host = self.host
        cm.header.origin.port = self.port
        cm.header.time = 10000
        cm.header.destination = 6
        clmsg2 = clientMessage_pb2.Operation.Value("POST")
        #b = self.chunkFile()
        #cm.reqMsg.data = filecontent
        cm.reqMsg.key = filename
        cm.reqMsg.seq_no = chunkid
        cm.reqMsg.operation = clmsg2
        cm.reqMsg.chunkInfo.no_of_chunks = noofchunks
        cm.reqMsg.chunkInfo.seq_size = 1
        cm.reqMsg.chunkInfo.time = 123
        #cm.reqMsg.chunkInfo.time = current_time
        # cm.reqMsg = clmsg
        # cm.reqMsg.Value(clmsg)
        var = cm.SerializeToString()

        return var

    def genChunkInfoPutMsg(self,filename, noofchunks,chunkid):

        print("SC 172 noofchunks")
        print noofchunks
        current_time = datetime.now().time()
        cm = pipe_pb2.CommandMessage()
        cm.header.node_id = 2
        cm.header.origin.id = 1
        cm.header.origin.host = self.host
        cm.header.origin.port = self.port
        cm.header.time = 10000
        cm.header.destination = 6
        clmsg2 = clientMessage_pb2.Operation.Value("PUT")
        #b = self.chunkFile()
        #cm.reqMsg.data = filecontent
        cm.reqMsg.key = filename
        cm.reqMsg.seq_no = chunkid
        cm.reqMsg.operation = clmsg2
        cm.reqMsg.chunkInfo.no_of_chunks = noofchunks
        cm.reqMsg.chunkInfo.seq_size = 1
        cm.reqMsg.chunkInfo.time = 123
        #cm.reqMsg.chunkInfo.time = current_time
        # cm.reqMsg = clmsg
        # cm.reqMsg.Value(clmsg)
        var = cm.SerializeToString()

        return var

    def genChunkedMsg(self,filename,filecontent, noofchunks,chunkid):

        print("SC 200 noofchunks")
        print noofchunks
        cm = pipe_pb2.CommandMessage()
        cm.header.node_id = 2
        cm.header.origin.id = 1
        cm.header.origin.host = self.host
        cm.header.origin.port = self.port
        cm.header.time = 10000
        cm.header.destination = 6
        clmsg2 = clientMessage_pb2.Operation.Value("POST")
        #b = self.chunkFile()
        cm.reqMsg.data = filecontent
        cm.reqMsg.key = filename
        cm.reqMsg.seq_no = chunkid
        cm.reqMsg.operation = clmsg2
        cm.reqMsg.chunkInfo.no_of_chunks = noofchunks
        # cm.reqMsg = clmsg
        # cm.reqMsg.Value(clmsg)
        var = cm.SerializeToString()

        return var

    def genChunkedPutMsg(self,filename,filecontent, noofchunks,chunkid):

        cm = pipe_pb2.CommandMessage()
        cm.header.node_id = 2
        cm.header.origin.id = 1
        cm.header.origin.host = self.host
        cm.header.origin.port = self.port
        cm.header.time = 10000
        cm.header.destination = 6
        clmsg2 = clientMessage_pb2.Operation.Value("PUT")
        #b = self.chunkFile()
        cm.reqMsg.data = filecontent
        cm.reqMsg.key = filename
        cm.reqMsg.seq_no = chunkid
        cm.reqMsg.operation = clmsg2
        cm.reqMsg.chunkInfo.no_of_chunks = noofchunks
        # cm.reqMsg = clmsg
        # cm.reqMsg.Value(clmsg)
        var = cm.SerializeToString()

        return var

    def sendData(self,data,host,port):

        print("SC 204")
        msg_len = struct.pack('>L', len(data))
        print("SC 206")
        self.sd.sendall(msg_len + data)
        print("SC 208")

        print("SC 216")
        #self.sd.close
        print("SC 218")
        return

    def sendDataGet(self,data,host,port, path):

        print("SC 249")
        msg_len = struct.pack('>L', len(data))
        print("SC 251")
        self.sd.sendall(msg_len + data)
        print("SC 253 Send the get request")

        r = pipe_pb2.CommandMessage();
        #my_array = array('i', r)
        my_array = [] * 6
        iter = 0
        noChunk = 2
        while iter<noChunk:
            iter+=1
            len_buf = self.receiveMsg(self.sd, 4)
            print("SC 255 len_buf")
            print len_buf
            msg_len = struct.unpack('>L', len_buf)[0]
            msg_in = self.receiveMsg(self.sd, msg_len)
            print("SC 259 msg_in")
            print len(msg_in)
            print("SC 257")

            print("SC 259")
            r.ParseFromString(msg_in)
            #self.sd.close
            print("SC 262 Getting the chunk id")
            print r.resMsg.chunk_no
            index = int(r.resMsg.chunk_no)
            my_array.insert(index, r.resMsg.data)
            print("SC 277 Getting the total number of chunks")
            noChunk = r.resMsg.no_of_chunks
            print noChunk
            #totalCount = 2
            #self.sd.close
            #if totalCount <2:
            #    break
            print("SC 264 printing the value of r")
            #totalCount -= 1
            #print r
        #fileDir = os.path.dirname(os.path.realpath('__file__'))
        #path = os.path.join(fileDir, '../output/nasa2.jpg')
        for r in my_array:
            with open(self.path, "a") as outfile:
                outfile.write(r)

        return my_array


    def receiveMsg(self, socket, n):
        buf = ''
        print("SC 286")
        print("SC 296: Value of n;")
        print n
        while n > 0:
            data = socket.recv(n)
            buf += data
            n -= len(data)
        return buf
