# genesis-cluster
The main objective of our project is to design and implement a cost effective framework for a robust scalable and distributed data store that adapts to the dynamic scheduling needs with efficient work sharing for inter cluster communication and storage operation with heterogeneous systems. 
The main idea is conceived as cluster of Nodes and then cluster of clusters as the specified in the requirements to design a system which is decentralized, large, and heterogeneous platform. We have kept the system independent of the type of data storage used and file type which is being stored. 
One of the core issues to be addressed in this project is Scalability, how to be able to serve an increasing number of client requests, and still minimise the latency in a distributed system keeping in mind the huge load of client requests.

RING Architecuture
	A ring topology has all the nodes lined up in a ring forming a closed circuit. Every node in the ring has one inbound and one outbound edge. A Node is also known as Edge or Server. 


Functionalities of Ring circuit

Circuit startup
	
	All the nodes within the cluster starts up with the basic more of ORPHAN which becomes Follower if they have an inbound edge formed and once it starts receiving heartbeat.
	A thread keeps a constant check on leader health, in case it didn’t find the leader within 4 heartbeat clicks that node changes it’s state to Candidate and initiates leader election. This process is repeated until a leader is elected.

Heart beat

	Heart beat is the backbone component within the server architecture and it’s main responsibility is to keep the ring intact and assure the other nodes that they are connected to others through a simple mechanism called Heart Beat. It consists of current time stamp that is sent to other node. Nodes constantly check the current time and the last heart beat time. If the difference is more than tolerant level, it raises a flag and declares the node as dead. One of the primary tasks that a node a would do in case of failure is to heal the broken ring. It makes use of it’s local routing table and contacts the concerned node and circumvents the failed node. Heartbeats also come with a term value. Terms are used to differentiate a stale leader and a new leader. Leader with lower term value will become a Follower.

Leader election

	Leader election is done using RAFT Algorithm. If the failed node is a leader, the nodes immediately alerts the ring with a message LeaderIsDead and initiates leader election by changing it’s status from Follower to Candidate. Nodes who receive a voting request directly sends their verdict to the candidate node and candidate node keeps on counting the number of votes it received with the total number of nodes in the circuit. As soon as the voting acceptance crosses 50% mark, it claims it self as a leader. In failed situations or tie break scenarios Election is called off and nodes participate in leader election again.

Circuit healing

	The circuit incorporates a new node into the existing loop by creating an edge space for it between 2 nodes. A similar approach is followed during node removal which takes the routing tables into consideration and establishes an outbound channel to the node which is an outbound edge to the failed node. It’s the inbound edge’s responsibility is to make sure it establishes a communication channel to the subsequent outbound nodes of the failed ones. 

Node registration

	Any new node that wishes to join the network builds a newbie message to the leader node and waits for the inbound edge to get established while it secures an inbound spot for the leader. Leader makes contact it’s inbound node and asks it to modify it’s existing outbound edge to point to the new node in the network. In this way, new node is absorbed into the network with our topology intact.


Dragon Beat
		
	Dragon beat is one of it’s kind to update the routing tables of all the nodes within the circuit, nodes take links within dragon beat and use them to traverse the failed nodes during failure scenarios. Dragon beats are always initiated by leader but updated by other nodes in the circuit, in this way, we minimise the background noise traffic as well.
 
Inbound edge work stealing

	Every node can steal tasks from its inbound edge provided it satisfy the threshold work-load condition, else, it results in a steal failure. We have kept 50% as the threshold for a node to allow some other node to steal from it. And, in order to steal from another node, a node should have its own tasks queue on a low workload, which is kept as 2 in the system. A heart beat has the parameters like enqueued and processed which is easy for the other node to calculate load of the one sending heart beat. If the node assumes the incoming request has heavy load, it sends a steal request and the overloaded node can respond back with a task.

Outbound edge work stealing

	Every node irrespective of it’s status has the ability to steal requests from their outbound edges. As soon as the load on one node drops below the threshold, a separate Stealer Thread broadcast steal requests to it’s outbound edges. If the recipient node has more tasks in it’s inbound queue, it honours the request and responds back with a task.

Dynamic thread allocation to queues

	Worker threads are allocated with queues dynamically throughout the life cycle of the node. Administrator can specify the number of worker within the routing conf to ensure maximum reusability of code. A queue monitor encapsulates both inbound and outbound queue loads and return the queue which requires immediate processing, based on the amount of task loads it has. If the node is idle workers are given Lazy Queue, whose sole responsibility is to store tasks for replication across the cluster.

Lazy Queues for replication

	Lazy Queues are part of the circuit architecture whose primary responsibility is to replicate the data chunks across it’s cluster. As soon as a worker completes processing a PUT or POST request which involves Database write operation, it is moved to Lazy Queue and a task type called Lazy is put up. This task will be passed on to the next nodes within ring. Every node checks the processed nodes values in the work message header. In the presence of such tag it terminates the execution and doesn’t pass it along. The absence of such a node value in the header inherently mean that node has not processed the chunk yet and puts it up in it’s Lazy Queue. A worker would pick up the task, writes into its database and sends it across the other outbound nodes.

Workers architecture

Workers are part of the thread pool which is individual to each node. One can specify the total number of workers in the configuration file. Below snippet shows the instantiation of worker pool with an Array of workers whose size is taken from config file. Every worker is loaded with state and wait time but not queues. Queues would be allocated dynamically.
		

Message handlers architecture
		
	State pattern was followed here with state varying among one of VOTED, FOLLOWER, LEADER, CANDIDATE, ORPHAN. Only calls specific to the state are handled here.

 
Python client
 
Python client was developed to interact with the Java server. It interacted with the server synchronously. The data is transferred into chunks. The maximum chunk size is 1 Mb.

Various operations were performed by the python client:
a.     POST
b.     PUT
c.     GET
d.     DELETE
e.     PING
3.     POST: A new file is posted at the server
a.     Get the file from the local machine to perform POST operation.
b.     Considering the input file size, its divided into the number of chunk by
Number of chunks = Total input file size / Maximum size of the chunks
c.  As per our proto files, the objects are initialized
d.  The chunks are then sent to the server individually.
4.     PUT. An existing file is updated on the server with the new contend from the input file
a.     Get the file from the local machine to perform PUT operation.
b.     Considering the input file size, its divided into the number of chunk by
Number of chunks = Total input file size / Maximum size of the chunks
c.  As per our proto files, the objects are initialized
d.     The chunks are then sent to the server individually
5.     GET. On the basis of key of file, the file is searched from the database at server.
a.     Get the key of the file to be obtained from the server.
b.  Create the object to be send to the server with a GET request
c.  Send the request and wait for the response
6.     DELETE. Deletes the file at the server on the basis of the key in the server.
7.     PING. Pings the server 

Global API

List of use cases that are supported:

Node has the file, Node knows the client
	In this scenario, the client initiates a request to the cluster which has the file. Tasks are created and file chunks are sent back to client.

Node doesn’t has the file, Node doesn’t know the client 
	In this situation, node simply forwards the request it received to the other clusters.

Node has the file, Node doesn’t know the client
	Global Handler checks for the incoming message type and if it is of type request, cluster see’s whether it has the file name in it’s database or not. If it has, it immediately retrieves all the chunks and push it to GlobalOutboundQueue. A global out worker is assigned with this queue and he manages to push the chunks to the other cluster. Since it is an uni-directional circuit.

Node doesn’t has the file, Node knows the client
	In this case, the client directly requests the cluster with a file which is not available within it’s database. If my node doesn’t find the file in it’s local database it wraps the command message into global message and forwards the request among other clusters. A moderator map is used to hold the UUID tagged with the client request and the global message and the channel of the client. Other clusters are adhered to append the same request id to the response and pass it on in the loop. One of the other cluster serves the client request with the corresponding chunks, changes the global message type to response and pass it along in the loop. Once it is received to the originator, current node checks if it has the String UUID in the moderator map. If it exists it get’s the channel and pushes it to the client.

Moderator map:
	A moderator map is a hash map with String as a key and Client Object as value. It is used to map the incoming client requests and the client channel. As soon as the node puts some key and channel into map, it is also the node’s responsibility to share this key and Client information among the other members of the loop. Receiver does this by initiated moderator chain through which all the nodes get the information of the client and the uuid generated to serve the client request.
	In this way, even if the other node get’s the request, they are fully equipped with information about the client and the request id’s the cluster has processed.


Data Flow and Message Passing
We have defined a proto file (clientMessage.proto) for sending and receiving messages of file chunks given by the client.
We have implemented all CRUD operations to be perfomed with the files. The supported operations by the client are defined by the enumeration Operation below: 




Client API
Using the client API a request can be sent to our server which listens in the command port for any work to be done for external client. Operations supported as shown above can be POST a file on the server, GET, UPDATE, DELETE. It is the responsibility of the client to give the file in a  format which is can be stored directly to our database. When Client has a big file it will chunk that file into 1 MB chunks and and send them to the server along with the sequence no, and the metadata message to be able to assemble them again. 

The File Conversion Utility does the reading of file into bytes and then creating small byte strings for each chunk. A file chunk is created by reading 1024*1024 bytes in a byte array and a Request message created.


While Performing GET operation, we have to only send the operation Type (“GET” in this case) and the Key-Name to be retrieved. Then, if the client has the file it will send all the chunks corresponding to that file and the Chunk Info Record. From this, we will retrieve the total no. of messages that a client should wait for. And when it has received all messages, it assembles them in a sequential manner. We have used LinkedHashMap to preserve the sorted sequence for the file, and the comparator keeps all the messages sorted based on the seq number for the chunk. 
