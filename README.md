# Project Neon

Neon stands for Number Engineer Object Notation
It's main aim is to support polymorphism inside java,

## Features

- Serialize and deserialize objects with Neon
- Supports a wide range of types for serialization
- Advanced features like deep cloning, lazy comparison, and experimental multi-threaded serialization

## Usage

Here's a quick example on how to use Neon for various serialization tasks:

```java
private static void example() throws InvalidHeader, InvalidNeonException, IOException {
    Object object = new Object();
    
    // Serialize object to String
    String example = Neon.writeObjectToString(object);
    
    // Serialize object to StringBuilder
    StringBuilder stringBuilder = Neon.writeObjectToStringBuilder(object);
    
    // Serialize object to file
    Neon.writeObjectToFile(object, new File("./object1.neon"));
    
    // Serialize object to OutputStream
    Neon.writeObjectToStream(object, Files.newOutputStream(Paths.get("./object2.neon")));
    
    // Serialize object with internal buffering
    Neon.writeObjectToBufferedStream(object, Files.newOutputStream(Paths.get("./object3.neon")));
    
    // Experimental dual-threaded serialization
    Neon.writeObjectToThreadedStream(object, Files.newOutputStream(Paths.get("./object4.neon")), (finished) -> {});
    
    // Deserialize object from String
    object = Neon.readObject(example);
    
    // Deserialize object from file
    object = Neon.readObject(new File("./object1.neon"));
    
    // Deserialize object from InputStream
    object = Neon.readObject(Files.newInputStream(Paths.get("./object2.neon")));
    
    // Deep clone objects
    System.out.println(object); // Original reference
    object = Neon.deepClone(object);
    System.out.println(object); // New reference after cloning
    
    // Deep compare objects
    System.out.println("DeepCompare: " + Neon.deepCompare(new Object(), new Object()));
    
    // Advanced usage examples like Zombie Cast and Deep Down Cast
    final Zombie zombie = Neon.zombieCast(new Vegetable(), Zombie.class); // Example of Zombie Cast
    final Vegetable vegetable = Neon.deepDownCast(new Carrot()); // Example of Deep Down Cast
}
```

## Design Rules

To ensure compatibility with Neon, a class must:

- Have a zero-argument constructor (either explicitly defined or implied by default).
- Implement `Neon.PostInit` for custom post-initialization logic, if needed.

## What Can Be Serialized

- Non-transient and non-static fields
- Final fields (yes, they can somehow be serialized)
- Primitives directly via `Neon.writeObjectToString(1)`
- Arrays, Maps, Collections (limitations apply)
- Classes with generics
- Inner classes, enums, records, and more

## Limitations

- Serialization does not support interfaces or abstract classes.
- Null values in arrays, collections, or maps cause serialization exceptions.

## Known Issues

- Null elements in data structures cause exceptions during serialization.
- Object fields created by Neon revert to default field values if set to null previously.


