General purpose event tracking system
=====================================

Events are key/value pairs linked to the origin IP and received timestamp. Events are stored in the specified cassandra database. To send events to the tracking system, just sent an HTTP POST request to the base URL : /track or /track/chosenEventCategory.


Simple example requests :

- curl --data "user=me&location=here" http://localhost:9000/track
- curl --data "user=me&location=here" http://localhost:9000/track/authfail


To start a cassandra database :
```bash
docker run \
   -d \
   --name mycassandra \
   -p 127.0.0.1:9160:9160 \
   -p 127.0.0.1:9042:9042 \
    spotify/cassandra
```

Check stored data inside cassandra :
```bash
docker exec -it mycassandra cqlsh
cqlsh> USE tracking ;                    
cqlsh:tracking> 
cqlsh:tracking> select * from tracked;

 id                                   | timestamp                | category | entries                            | inet
--------------------------------------+--------------------------+----------+------------------------------------+------
 e4ca11e6-2d3b-4aa5-a8d2-e14b9697ee7a | 2015-02-26 22:27:10+0000 |     year | {'location': 'here', 'user': 'me'} |  ::1

(1 rows)

cqlsh:tracking> 
 
```
