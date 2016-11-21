import common_pb2
import socket               
import time
import struct
import pipe_pb2


def request_ping():

    cmsg = common_pb2.Header()
    cmsg.Header.node_id = 1
    cmsg.Header.time = 1000
    cmsg.Header.destination = -1
    cm = pipe_pb2.CommandMessage()
    cm.Header = cmsg
    cm.ping = "true"
    print "request_ping() executing"

    pingr = cm.SerializeToString()

    packed_len = struct.pack('>L', len(pingr))
    # Sending Ping Request to the server's public port
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    # host = socket.gethostname() # Testing on own computer
    # port = 5570 # Public Port
    s.connect(("127.0.0.1", "4168"))
    # Prepending the length field and sending
    s.sendall(packed_len + pingr)
    s.close()



   
if __name__ == '__main__':
    print "main() executing"
    request_ping()
    # UDP_PORT = 8080
    # serverPort = getBroadcastMsg(UDP_PORT) 


#    name_space = "competition"
#    ownerId = 123;
#    listcourseReq = buildListCourse(name_space, comm_pb2.JobOperation.ADDJOB, ownerId)
#    sendMsg(listcourseReq, 5573)




