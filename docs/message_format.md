

|key             |Definition                                                                                                       |
-----------------------------------------------------------------------------------------------------------------------------------
|START_BYTE      |A special byte that tells the server to start listening. This and `END_BYTE` are the only things sent unencrypted|
|END_BYTE        |A special byte that tells the server to stop listening and start executing the request                           |
|uid             |User ID                                                                                                          |
|amount_of_chunks|The amount of chunks encrypted in RSA.                                                                           |
|chunk_size      |The uniform size of each chunk. This is determined by the key that is generated server-side.                     |

```
{START_BYTE}MESSAGE
TO:{uid}
CHUNKS:{amount_of_chunks}
CHUNKSIZE:{chunk_size}
FILES:{files}


{MESSAGE_DATA}

[Data of the first file sent if there is.]
[Data of the second file sent if there is.]
(...){END_BYTE}
```
