# IoSL Group 2

## Requirements

- Java 8+ ([How to install](https://openjdk.java.net/install/))
- Maven ([How to install](https://maven.apache.org/install.html))

## How to run the simulator

1. Clone the repository and `cd` to the created directory <br>
`git clone https://github.com/imolcean/iosl-group2.git` <br>
`cd iosl-group2`

2. Build the project running <br>
`mvn package` <br><br>
Maven will download  all dependencies and pack them together with the compiled code into a single `.jar` file.

3. Run the simulator <br>
`java -jar target/simulation-0.1-jar-with-dependencies.jar`

#### Note
All the files located in the `resources` folder are being packaged into the JAR.
This means that if you make a change to any of these files, you will need to rebuild the whole project by running
`mvn clean package`
