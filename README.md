### Back on IDE after a break


```bash
mvn clean package spring-boot:run
```

#### Example Payload :

```bash
giris@grishma MINGW64 ~
$ curl -iSs -X GET "http://localhost:8080/prompt/result?source=dummy&prompt=who_am_i"
HTTP/1.1 200
Content-Type: application/json
Transfer-Encoding: chunked
Date: Fri, 29 May 2026 18:19:54 GMT

{"source":"dummy","agentName":"strange-ollama","result":"This is a test result"}

```

