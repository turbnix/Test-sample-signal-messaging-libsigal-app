# Sample Signal App

This repository is a sample app (server & client) that uses `libsignal` to encrypt messages
The code is just a PoC and should be treated as such.

## Server

Server requires `mysql` database to run. </br> 
I recommend using `server/docker-compose.yml`, where everything is set up. </br>
Simply go to `server` folder and run
```bash
docker compose up -d
```
**Note:** Newer docker API uses `docker compose` instead `docker-compose`

After that, you can run the server from repository root folder with with
```bash
./gradlew server:bootRun
```

All the required schemas will be created automatically during server startup.

## Client
After server is running you can run the client from repository root folder with

```bash
./gradlew client:run --console=plain
```

After start-up you should see:
```
Enter your username:
```

Enter username, and you should be able to send messages to other users (they must be registered too).

When running other client, use username to send messages to each other.
To read messages just press enter when you see
```
Enter the username of the person you want to send a message to, or leave empty to read the inbox:
```

**Note: After running client username is saved in the server, and it's not possible to run the client with the same username unless you deleted entries from `users` table** </br>
**You can also clean up the whole db by removing `mysql-data` docker volume and rerunning db and server**

