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
        self.sd.connect(("127.0.0.1", 4668))
        print("Connected to Host:", self.host, "@ Port:", self.port)


    def stopSession(self):
        #builder = MessageBuilder()
        #msg = builder.encode(MessageType.leave, self.name,'', '')
        #print(msg)
        #self.sd.send(msg)
        self.sd.close()
        self.sd = None


    def getFile(self,name):
        cm = pipe_pb2.CommandMessage()
        cm.header.node_id = 6
        cm.header.origin.id = 6
        cm.header.origin.host = "127.0.0.1"
        cm.header.origin.port = 4668
        cm.header.time = 10000
        #cm.message = name
        clmsg2 = clientMessage_pb2.Operation.Value("GET")
        # b = self.chunkFile()

        #cm.reqMsg.data = filecontent
        cm.reqMsg.key = "Key"
        #cm.reqMsg.seq_no = msgInt
        cm.reqMsg.operation = clmsg2
        # cm.reqMsg = clmsg
        # cm.reqMsg.Value(clmsg)
        var = cm.SerializeToString()

        return var

    def deleteFile(self,name):
        cm = pipe_pb2.CommandMessage()
        cm.header.node_id = 6
        cm.header.origin.id = 6
        cm.header.origin.host = "127.0.0.1"
        cm.header.origin.port = 4668
        cm.header.time = 10000
        #cm.message = name
        clmsg2 = clientMessage_pb2.Operation.Value("DELETE")
        # b = self.chunkFile()

        #cm.reqMsg.data = filecontent
        cm.reqMsg.key = "Key"
        #cm.reqMsg.seq_no = msgInt
        cm.reqMsg.operation = clmsg2
        # cm.reqMsg = clmsg
        # cm.reqMsg.Value(clmsg)
        var = cm.SerializeToString()

        return var

    def chunkFile(self,file):
        fileChunk = []
        with open(file, "rb") as fileContent:
            data = fileContent.read(1024*1024)
            while data:
                fileChunk.append(data)
                data = fileContent.read(1024*1024)
        return fileChunk

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
        cm.header.node_id = 1
        cm.header.time = 10000
        cm.header.destination = 2
        cm.ping = True
        print "request_ping() executing"
        pingr = cm.SerializeToString()
        #packed_len = struct.pack('>L', len(pingr))
        return pingr;

    def genChunkInfoMsg(self,filename, noofchunks,chunkid):

        current_time = datetime.now().time()
        cm = pipe_pb2.CommandMessage()
        cm.header.node_id = 2
        cm.header.origin.id = 1
        cm.header.origin.host = "127.0.0.1"
        cm.header.origin.port = 4668
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

    def genChunkedMsg(self,filename,filecontent, noofchunks,chunkid):

        cm = pipe_pb2.CommandMessage()
        cm.header.node_id = 2
        cm.header.origin.id = 1
        cm.header.origin.host = "127.0.0.1"
        cm.header.origin.port = 4668
        cm.header.time = 10000
        cm.header.destination = 6
        clmsg2 = clientMessage_pb2.Operation.Value("POST")
        #b = self.chunkFile()
        cm.reqMsg.data = filecontent
        cm.reqMsg.key = filename
        cm.reqMsg.seq_no = chunkid
        cm.reqMsg.operation = clmsg2
        # cm.reqMsg = clmsg
        # cm.reqMsg.Value(clmsg)
        var = cm.SerializeToString()

        return var

    def genChunkedPutMsg(self,filename,filecontent, noofchunks,chunkid):

        msgStr = "Key"
        msgInt = 1
        cm = pipe_pb2.CommandMessage()
        cm.header.node_id = 2
        cm.header.origin.id = 1
        cm.header.origin.host = "127.0.0.1"
        cm.header.origin.port = 4668
        cm.header.time = 10000
        cm.header.destination = 6
        clmsg2 = clientMessage_pb2.Operation.Value("PUT")
        #b = self.chunkFile()
        cm.reqMsg.data = filecontent
        cm.reqMsg.key = msgStr
        cm.reqMsg.seq_no = msgInt
        cm.reqMsg.operation = clmsg2
        # cm.reqMsg = clmsg
        # cm.reqMsg.Value(clmsg)
        var = cm.SerializeToString()

        return var

    def sendData(self,data,host,port):

        msg_len = struct.pack('>L', len(data))

        self.sd.sendall(msg_len + data)

        len_buf = self.receiveMsg(self.sd, 4)

        msg_in = self.receiveMsg(self.sd, len_buf)

        r =  pipe_pb2.CommandMessage();

        r.ParseFromString(msg_in)

        self.sd.close

        return r

    def receiveMsg(self, socket, waittime):

        socket.setblocking(0)

        finaldata = []

        data = ''

        start_time = time.time()

        while 1:

            if data and time.time() - start_time > waittime:

                break

            elif time.time() - start_time > waittime * 2:

                break

        try:

            data = socket.recv(8192)

            if data:

                finaldata.append(data)

                begin = time.time()

            else:

                time.sleep(0.1)

        except:

            pass

        print(finaldata)

        return ''.join(finaldata)
