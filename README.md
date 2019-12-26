# Rc522ForPi4j

## Table of content

- [Rc522ForPi4j](#rc522forpi4j)
  - [Table of content](#table-of-content)
  - [Introduction](#introduction)
  - [Usage](#usage)
  - [Example](#example)
  - [Files](#files)
    - [RC522 package](#rc522-package)
    - [Model package - Auth](#model-package---auth)
    - [Model package - Card](#model-package---card)
    - [Model package - Communication](#model-package---communication)
    - [Raspberry package](#raspberry-package)
    - [RC522 package](#rc522-package-1)
    - [Util package](#util-package)

## Introduction

It's a java based implementation for the RC522 RFID module using Pi4j on Raspberry Pi. The original code has been made in python and then someone converted it into C and now finally I created the Java version! Not only converting, but extending the original library with more, easy to use features and models. The target java version is 8, because the raspbian distros default java version is also 8 at the moment.
Currently only read functions are implemented.

## Usage

You should include the library jar file from the releases folder in your pi4j project. This library works with that library by default, but you can replace the ```RaspberryPiAdapterImpl``` class with your own solution, so it's not tied to that library! The recommended entry point is the ```RC522ClientImpl```. You should create an instance of this class using the static instance generator method, (```RC522ClientImpl.createInstance()```) which will take care of the dependencies and the initializations for you! Every adapter and client class has an interface, so you can replace any part of this library using dependency injection or making your own instance generator method. Beware, you should call the init methods in the adapter class, otherwise it won't work at all!

## Example

The library has some example classes which can be used to start creating your own implementation. You can find these classes under the ```hu.whiterabbit.rc522forpi4j.example``` package. You can also generate these example codes into runnable jars using the shadowJar gradle plugin: (the ```exampleClass``` parameter is determining the entry point for your jar file, default value: 'ReadData')

```bash
./gradlew shadowJar -PexampleClass=WriteExample
```

The most basic implementation for reading the whole card data:

```java
public class TestClass {

    // Some basic logger instance. You can use anything instead of this
    private static final Logger logger = LoggerFactory.getLogger(TestClass.class);

    // The main entry point for the command line application
	public static void main(String[] args) {

        // Creating a RC522Client instance using the static instance generator method
        final RC522Client rc522Client = RC522ClientImpl.createInstance();

        try {
            // Search for card
            while (true) {
                // Reading card data using the client
                Card card = rc522Client.readCardData();

                // If card is present, print it's content into the log
                if (card != null) {
                    logger.info("Card data: \n{}", card);

                    Thread.sleep(2000);
                }

                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
```

## Files

### RC522 package

- ```RC522Adapter```: Adapter interface, it manages the communication between the raspberry pi and the RC522 module. The default implementation is called ```RC522AdapterImpl```.
- ```RC522AdapterImpl```: Default implementation for the ```RC522Adapter``` interface. It can salact and authenticate your card and read/write data. You shouldn't use this class directly, there is an other abstraction layer, which has more sophisticated methods and approach for handling cards.
- ```RC522Client```: Client interface, it has more detailed and easy to use methods to handle the communication between your reader and your card.  The default implementation is called ```RC522ClientImpl```.
- ```RC522ClientImpl```: Ideally this is your only service/handler class that you need in your project. You can just create a new instance and start using it's methods. If you need, you can replace or extend this implementation with your own.

### Model package - Auth

- ```AuthKeyType```: Your key type in an enum format. Only two types are exist for the MIFARE type of RFID cards: AUTH_A, AUTH_B. It determines the used keyset for a certain block
- ```BlockAuthKey```: Block level authentication key. Depending on it's type it can be an A or B type key. This class contains the factory default key, with brand new cards you can use it to access your blocks.
- ```SectorAuthKey```: Sector level authentication object. It should contain 4 ```BlockAuthKey``` (every sector has exactly 4 blocks) instance with the corresponding keys. You also can get the factory keys for your sector!
- ```CardAuthKey```: This object contains all key values for a specific card. All card has 16 sectors, therefore every ```CardAuthKey``` should contains 16 ```SectorAuthKey``` instance. The ```getFactoryDefaultKey``` method returns the factory default key instance.

### Model package - Card

- ```Block```: Interface for the different types of block classes. The block is the fundamental building element of a card, every card has 16*4 blocks. It can have a different purpose depending on the position on the card.
- ```BlockAccessMode```: This is a low level access mode model. It contains 3 access flag and it defines a block access properties. Depending on the combination of these flags and the type of the block it can determine the level of encryption of a certain block.
- ```BlockType```: It's an enum and defines the existing block types. The following types are available in every cards:
  - ```BlockType.MANUFACTURER```: This is a special block type, it marks the first block in the first sector and it contains the card uid and the manufacturers id. This is an unique block and by default you cannot change it. (There are some special cards, which cards manufacturer's block can be altered, but most of the RFID cards made this block read only)
  - ```BlockType.SECTOR_TRAILER```: Another special block type. Every sector's last block is a sector trailer block and it contains the credential informations for the actual sector and also the A and B authentication keys. (You cannot read the A key and B is readable in certain cases!) Also there is an extra byte that can be used for storing data.
  - ```BlockType.DATA```: This is the default block type, you can store and read any data if you have the right access and you know the required authentication key (A or B)
  - ```BlockType.VALUE```: This is a special block data. It stores a numeric value with error detection. It can be used for electronic purses to store, increase or decrease amounts on it.
- ```Card```: This is the root object, it stores all card data. Every card contains 16 sectors and every sector 4 blocks. If you filled an instance with the block data, then you shouyld run the ```recalculateAccessModes()``` method. This method calculates the access modes frem the sector trailer blocks
- ```DataBlock```: The most basic type of block. It stores your data and can be readed if you authenticated with the corresponding auth key. The full block area can be used to store your data, the authorization config stored in the sector trailer block. Currently only the "free form" data block supported by the library, but you can use these blocks to store "values" manualy. A value block has some special functions: increment, decrement, restore, transfer. You can create a digital wallet using this type of block.
- ```ManufacturerBlock```: A special type of block, every card's first sectors first block is a manufacturer block. It contains the card ID and the manufacturer's ID. It has two format, the first one has a 4bit long (0-3) so called NUID and a 12 block long (4-15) manufacturer's ID, the second one has a 7 bit (0-6) UID and a 9 bit (7-15) manufacturer's ID. This library is using the second format.
- ```Sector```: Every 1K card has 16 sectors and every sector has 4 blocks. This is a logical unit, the card itself stores it's data using only blocks, but in the library you have to use this fragmentation. (this used in every datasheet and documentations also) The content of the sectors are the following:
  - Sector 0: Index 0: Manufacturer's block, index 1-2: data block and index 3 is a sector trailer block
  - Sector 1-15: Index 0-2 data block and index 3 is a sector trailer block
- ```SectorTrailerBlock```: This is a special block type, this block stores all the sector related security informations and keys. The library take care of this block type, so you don't need to manualy read, interpret and update these bytes! The structure of a sector trailer block is the following:
  - 0-5 bytes contains the security Key A. (It's not readable, only writeable when the access bytes allows it)
  - 6-8 bytes is the access bytes for the whole sector. It has a redundant format, it stores every value in inverted format also. It determines the access modes for all 4 blocks in the sector, including the sector trailer block itself! Available levels: (read, write, read/write using Key A, Key B or Key A/B)
  - 9 byte is a data byte, you can store anything in it.
  - 10-15 bytes contains the security Key B. This key is readable/writable if the access bytes allows it.

### Model package - Communication

- ```CommunicationResult```: This is a low level result object. It used in the RC522Adapter to store the response of the direct RFID reader communication. It has a status and a data block if the communication needs to return some data from the reader.
- ```CommunicationStatus```: Basic enum, it indicates the communication status.

### Raspberry package

- ```RaspberryPiAdapter```: A lightweight adapter for the raspberry SPI communication. By default it's using the Pi4J spi library to communicate with the RC522 reader, but you can replace this adapter with your own implementation if needed! Also it helps with mocking in unit tests.
- ```RaspberryPiAdapterImpl```: The default implementation of the ```RaspberryPiAdapter``` interface. This class is using the Pi4J library to communicate with the RC522 reader. The main purpose of this class is to implement a more general solution for handling the SPI communication itself.

### RC522 package

- ```RC522Adapter```: This is the low level adapter used to communicate with the RC522 unit with the help of the RaspberryPiAdapter. It send and request raw data to/from the RC522 module via SPI interface. The ```CommunicationResult``` class contains the result of certain actions which involves communication between the raspberry pi and the RC522 reader.
- ```RC522AdapterImpl```: The default implementation of the ```RC522Adapter``` interface. It handles all the low level communications between the raspberry pi and the RC522 reader. The difference between this class and the ```RaspberryPiAdapter``` implementation is that the RC522Adapter is a more specific, RC522 related codebase and the RaspberryPiAdapter has more general methods, mostly handling the SPI communication itself. You can use this implementation directly to handle all the card related communications, but it's much easier to use the high level and more detailed ```RC522Client``` instead.
- ```RC522Client```: A high level, easy to use interface to handle all the necessary card related communications. This interface and it's implementation should be your entry point to this library.
- ```RC522ClientImpl```: A default implementation for the ```RC522Client``` interface. This is the highest level entry point to this library. It has a list of methods which can be used to handle all the necessary card related communications. It's strongly adviced to use the ```RC522ClientImpl.createInstance()``` for creating a new instance of this client, because it creates and initialize the required adapters.
- ```RC522CommandTable```: This class contains the low level instruction set of the RC522 module. You don't need to use it directly if you use the ```RC522ClientImpl``` as entry point, because that implementation will take care of all the low level communications. I tried to solve all the available commands using the publicly available documentations, but some of the features may differ.

### Util package

- ```AccessModeBit```: This is the logical representation of the sector trailer block's access bytes. This enum only used to convert the access mode data between the binary format and the ```BlockAccessMode``` class.
- ```CardUtil```: Card related util methods. It helps to transform and process the card data. (access mode or displaying card related data)
- ```DataUtil```: Data and value related util methods. It helps to convert between the different data types. (bit to bytes, bytes to hex, etc)