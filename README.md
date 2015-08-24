# [WIP] scala2ch

Sample BBS application  implemented in Scala

## Run

```
sbt
> testMigrate
> run
```

## Usage

### Create a user

```sh
curl -X POST \
--header Content-Type:"application/json" \
-d "{\n  \"userName\": \"John\",\n  \"password\": \"passwd\",\n  \"email\": \"john@example.com\"\n}" \
 http://localhost:8080/v1/users
```

### Get a user

```sh
curl http://localhost:8080/v1/users?id=1
```

### Get an access token

```sh
curl -X POST --header Content-Type:"application/json" \
-d "{\n  \"userName\": \"John\",\n  \"password\": \"passwd\"\n}" \
 http://localhost:8080/v1/login
```

### Create a thread

```sh
curl -X POST --header Content-Type:"application/json" \
--header "X-AUTH-TOKEN: ACCESS_TOKEN" \
-d "{\"title\": \"title\",\"tags\": [\"tag1\", \"tag2\", \"tag3\"]}" \
http://localhost:8080/v1/thread
```

### Get a thread

```sh
curl http://localhost:8080/v1/thread/1
```

### Delete a thread

```sh
curl -X DELETE \
--header "X-AUTH-TOKEN: ACCESS_TOKEN" \
http://localhost:8080/v1/thread/1
```

## Known issues
+ it:test command does not invoke test after migration
+ AutoRollback is not working
+ selectFlow does not select expected tags
