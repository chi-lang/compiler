# Building chi native image

## Requirements

- Visual Studio developer tools. Use the "Developer PowerShell for VS 2022".
- GraalVM 22

## Building

> Run ALL of the commands in the repository root directory!

We first need an uber jar containing all the libraries and natives:

```powershell 
./gradlew shadowJar
```

It will generate the jar in `build/libs/chi-all.jar`.

Then you'll also need to generate the configurations. The easiest way is to use the
GraalVM native image agent. Just run

```powershell 
native\run_with_agent.bat
```

and use a REPL few times. Once you exit the repl witn `.exit` command, it should 
generate the `config` directory.

Finally you have all the pieces to build the image:

```powershell
native\build.bat
```

It should generate the `chi.exe` file.