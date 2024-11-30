 Let's pretend we want to log into `Billy`. First, you'll need to fetch the User's ID if you don't have it already:

```
FETCH
CLASS:User
SEARCH:username
TERM:Billy
```

|Key   |Definition                                                                                                       |
--------------------------------------------------------------------------------------------------------------------------
|CLASS |The table in which to search for the items requested. For the list of classes, refer to [classes.md](classes.md).|
|SEARCH|The SQL Column to look for.                                                                                      |
|TERM  |The search term.                                                                                                 |

The server, if it has found the requested user, will return the following:
```
200

username:Billy
UID:u1234
privatekey:REDACTED
publickey:12345678990
pronouns:he/him
```
In a `FETCH` request, it will return everything that is stored in the SQL Row. For security reasons, some information,
such as the users private key, is redacted before responding. The user will be able to choose whether some things can be
redacted, such as their public key and pronouns.


```
AUTHCHALLENGE

UID:{UID}
```
