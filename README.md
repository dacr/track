General purpose event tracking system
=====================================

Events are key/value pairs linked to the origin IP and received timestamp. Events are stored in the specified cassandra database. To send events to the tracking system, just sent an HTTP POST request to the base URL : /track or /track/chosenEventCategory.


Simple example requests :

- curl --data "hello=world" http://192.168.1.129:9000/track
- curl --data "testeduser=root&by=10.10.10.10" http://192.168.1.129:9000/track/authfail



Quick start a cassandra database :
```bash
docker run \
   -d \
   --name mycassandra \
   -p 127.0.0.1:9160:9160 \
   -p 127.0.0.1:9042:9042 \
    spotify/cassandra
```


Quick start the tracking system (from the project directory once cloned), requires to have [activator](https://www.playframework.com/download) :
```
activator run
```


Check stored data inside cassandra :
```bash
docker exec -it mycassandra cqlsh
cqlsh> USE tracking ;                    
cqlsh:tracking> 
cqlsh:tracking> select * from tracked;

 
```
