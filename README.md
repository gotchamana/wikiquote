# wikiquote

Quote of the day from wikiquote

## Enviroment

- Java 11
- GraalVM 20.3.0
- Notification program
  - Linux: zenity
  - Windows: PowerShell script with Windows 10 toast

## Compile/Run

`./mvnw`

## Build/Run as uber jar

Build: `./mvnw package`  
Run: `java -jar target/wikiquote-X.X.X.jar`

## Build/Run as native image

Build: `./mvnw package -Pnative-image`  
Run: `target/wikiquote`

Windows user may also want to run `editbin /SUBSYSTEM:WINDOWS target\wikiquote.exe`
to prevent the console from appearing.
