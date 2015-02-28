General purpose event tracking system
=====================================

Events are key/value pairs linked to the origin IP and received timestamp. Events are stored in the specified cassandra database. To send events to the tracking system, just sent an HTTP POST request to the base URL : /track or /track/chosenEventCategory.


Simple example requests :

```bash
curl --data "hello=world" http://192.168.1.129:9000/track

curl --data "testeduser=root&by=10.10.10.10" http://192.168.1.129:9000/track/authfail
```



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

 id                                   | timestamp                | category | entries                                     | inet
--------------------------------------+--------------------------+----------+---------------------------------------------+---------------
 b8ab15e4-e7f7-46ae-9b2e-418f41e8cc23 | 2015-02-28 12:47:34+0000 | authfail | {'by': '10.10.10.10', 'testeduser': 'root'} | 192.168.2.222
 28f61501-7fcc-4442-bc1d-93d08151d180 | 2015-02-28 12:47:32+0000 |  default |                          {'hello': 'world'} | 192.168.2.222

(2 rows)

 
```
