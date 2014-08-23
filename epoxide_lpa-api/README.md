EpoxideLPA Api
==========

This package is the Interface and API specification

Example
-------

```java
import epoxide.lpa.annotation.*;

public class Entity1 {
	@Id public String name; // the field "name" is the Primary Key
	public String surname;
	public Integer somenumber;
}
```


```java
import epoxide.lpa.Database;
import epoxide.lpa.RecordSet;

// create a database and access the "Table" Entity1

Database db = ...

RecordSet<Entity1> rs = db.getRecordSet(Entity1.class);

// insert/update/put/delete an entity (or an row)

{
	Entity1 entity = new Entity1();
	entity.name="key1";
	entity.surname="heo";
	entity.somenumber=100;
	rs.insert(entity);
	// rs.update(entity);
	// rs.put(entity);
	// rs.delete(entity);
}


// read that entity

{
	RecordSet<Entity1> rs = db.getRecordSet(Entity1.class);
	Entity1 entity = rs.getEntity("key1");
	System.out.println(entity);
	if(entity!=null){
		System.out.println(entity.name);
		System.out.println(entity.surname);
		System.out.println(entity.somenumber);
	}
}

// iterate over the table

{
	QueryableRecordSet<Entity1> rs = db.getRecordSet(Entity1.class);
	IterableIterator<Entity1> iter = rs.getEntities();
	for(Entity1 entity:iter){
		System.out.println("-----------------------------------");
		System.out.println(entity.name);
		System.out.println(entity.surname);
		System.out.println(entity.somenumber);
	}
	iter.close();
}

```

