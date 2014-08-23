EpoxideLPA
==========

This package is the Implementation for the Epoxide Persistence API

Subpackages
-----------

### src

Common utilitys used by the backends

Dependencies:
- `guava-14.0.1.jar` (google guava)
- [kryo](https://github.com/EsotericSoftware/kryo)
- [reflectasm](https://github.com/EsotericSoftware/reflectasm)

### src-memcachedb

A MemcacheDB backend (supports anything that supports the memcached Protocol, including memcached itself)

Dependencies:
- `src`
- spymemcached

### src-mongodb

A Mongodb Backend (using the official Java-Driver for Mongodb)

Dependencies:
- `src`
- `mongodb-java-driver-2.12.3.jar` or newer

### src-rdbms

A SQL/jdbc backend.

Known bugs:
it does not check the field names, so they can conflict with sql key words.

Dependencies:
- `src`
